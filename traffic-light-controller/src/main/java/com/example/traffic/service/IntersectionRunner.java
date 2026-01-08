
package com.example.traffic.service;



import com.example.traffic.model.Direction;
import com.example.traffic.model.LightColor;
import com.example.traffic.model.Phase;
import com.example.traffic.model.SignalState;
import com.example.traffic.model.StateChange;
import com.example.traffic.util.SignalValidator;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Runs a single intersection's signal cycle using provided phases.
 * Concurrency: guarded by RW lock; scheduling via ScheduledExecutorService.
 */
public class IntersectionRunner {
    private final String id;
    private final ScheduledExecutorService scheduler;
    private final Clock clock;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private List<Phase> phases;
    private int phaseIndex = 0;
    private ScheduledFuture<?> currentTask;
    private boolean paused = false;

    private final Map<Direction, LightColor> lights = new EnumMap<>(Direction.class);
    private final Deque<StateChange> history = new ArrayDeque<>();
    private final int historyLimit;

    public IntersectionRunner(String id, List<Phase> phases, ScheduledExecutorService scheduler, Clock clock, int historyLimit) {
        this.id = Objects.requireNonNull(id);
        this.scheduler = Objects.requireNonNull(scheduler);
        this.clock = Objects.requireNonNull(clock);
        this.phases = new ArrayList<>(Objects.requireNonNull(phases));
        this.historyLimit = historyLimit;
        for (Direction d : Direction.values()) lights.put(d, LightColor.RED);
    }

    public String getId() { return id; }

    public void start() {
        lock.writeLock().lock();
        try {
            paused = false;
            schedulePhaseStart(0, 0);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void pause() {
        lock.writeLock().lock();
        try {
            paused = true;
            if (currentTask != null) currentTask.cancel(false);
            appendHistory("paused");
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void resume() {
        lock.writeLock().lock();
        try {
            if (!paused) return;
            paused = false;
            schedulePhaseStart(phaseIndex, 0);
            appendHistory("resumed");
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Manual step to the next phase: sets next phase to GREEN and stays paused (manual mode).
     */
    public void advanceOnce() {
        lock.writeLock().lock();
        try {
            if (currentTask != null) currentTask.cancel(false);
            paused = true;
            phaseIndex = (phaseIndex + 1) % phases.size();
            setGreens(currentPhase().getGreens());
            appendHistory("manual advance -> GREEN of " + currentPhase().getName());
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void replacePhases(List<Phase> newPhases) {
        lock.writeLock().lock();
        try {
            validatePhases(newPhases);
            this.phases = new ArrayList<>(newPhases);
            this.phaseIndex = 0;
            if (!paused) {
                if (currentTask != null) currentTask.cancel(false);
                schedulePhaseStart(0, 0);
            }
            appendHistory("sequence replaced");
        } finally {
            lock.writeLock().unlock();
        }
    }

    public SignalState snapshot() {
        lock.readLock().lock();
        try {
            return new SignalState(lights, currentPhase().getName(), Instant.now(clock));
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<StateChange> history(int limit) {
        lock.readLock().lock();
        try {
            return history.stream().limit(limit).toList();
        } finally {
            lock.readLock().unlock();
        }
    }

    // --- internal scheduling ---

    private void schedulePhaseStart(int index, long delayMs) {
        if (paused) return;
        phaseIndex = index % phases.size();
        currentTask = scheduler.schedule(this::runPhaseCycleOnce, delayMs, TimeUnit.MILLISECONDS);
    }

    private void runPhaseCycleOnce() {
        lock.writeLock().lock();
        try {
            if (paused) return;
            Phase phase = currentPhase();
            setGreens(phase.getGreens());
            appendHistory("phase:" + phase.getName() + " -> GREEN");
            scheduler.schedule(() -> toYellowThenAllRedAndNext(phaseIndex), phase.getGreenMs(), TimeUnit.MILLISECONDS);
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void toYellowThenAllRedAndNext(int startingIndex) {
        lock.writeLock().lock();
        try {
            if (paused) return;
            if (startingIndex != phaseIndex) return; // phase replaced
            Phase phase = currentPhase();
            setYellow(phase.getGreens());
            appendHistory("phase:" + phase.getName() + " -> YELLOW");
            scheduler.schedule(() -> toAllRedAndScheduleNext(startingIndex), phase.getYellowMs(), TimeUnit.MILLISECONDS);
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void toAllRedAndScheduleNext(int startingIndex) {
        lock.writeLock().lock();
        try {
            if (paused) return;
            if (startingIndex != phaseIndex) return;
            allRed();
            appendHistory("ALL_RED");
            int next = (phaseIndex + 1) % phases.size();
            Phase phase = phases.get(startingIndex);
            scheduler.schedule(() -> schedulePhaseStart(next, 0), phase.getAllRedMs(), TimeUnit.MILLISECONDS);
        } finally {
            lock.writeLock().unlock();
        }
    }

    // --- helpers ---

    private void setGreens(Set<Direction> greens) {
        for (Direction d : Direction.values()) {
            if (greens.contains(d)) lights.put(d, LightColor.GREEN);
            else lights.put(d, LightColor.RED);
        }
        SignalValidator.validateNoConflictingGreens(lights);
    }

    private void setYellow(Set<Direction> greensInPhase) {
        for (Direction d : Direction.values()) {
            if (greensInPhase.contains(d)) lights.put(d, LightColor.YELLOW);
            else lights.put(d, LightColor.RED);
        }
    }

    private void allRed() {
        for (Direction d : Direction.values()) lights.put(d, LightColor.RED);
    }

    private Phase currentPhase() { return phases.get(phaseIndex); }

    private void appendHistory(String desc) {
        history.addFirst(new StateChange(Instant.now(clock), currentPhase().getName(), desc));
        while (history.size() > historyLimit) history.removeLast();
    }

    private void validatePhases(List<Phase> phases) {
        if (phases == null || phases.isEmpty()) throw new IllegalArgumentException("phases must not be empty");
        for (Phase p : phases) {
            if (p.getGreenMs() < 0 || p.getYellowMs() < 0 || p.getAllRedMs() < 0)
                throw new IllegalArgumentException("durations must be non-negative");
            if (p.getGreens().isEmpty()) throw new IllegalArgumentException("phase greens must not be empty");
            boolean hasNS = p.getGreens().stream().anyMatch(d -> d == Direction.NORTH || d == Direction.SOUTH);
            boolean hasEW = p.getGreens().stream().anyMatch(d -> d == Direction.EAST || d == Direction.WEST);
            if (hasNS && hasEW) throw new IllegalArgumentException("phase has conflicting greens (NS with EW)");
        }
    }
}
