package com.example.traffic.service;


import com.example.traffic.model.Direction;
import com.example.traffic.model.Phase;
import com.example.traffic.model.SignalState;
import com.example.traffic.model.StateChange;
import com.example.traffic.repo.IntersectionRegistry;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Service
public class TrafficService {
    private final IntersectionRegistry registry = new IntersectionRegistry();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);
    private final Clock clock = Clock.systemUTC();

    public IntersectionRunner create(String id) {
        if (registry.exists(id)) throw new IllegalArgumentException("intersection already exists: " + id);
        List<Phase> phases = defaultPhases();
        IntersectionRunner runner = new IntersectionRunner(id, phases, scheduler, clock, 200);
        registry.put(id, runner);
        runner.start();
        return runner;
    }

    public Optional<IntersectionRunner> get(String id) { return registry.get(id); }

    public void replaceSequence(String id, List<Phase> phases) {
        IntersectionRunner r = registry.get(id).orElseThrow(() -> new IllegalArgumentException("not found"));
        r.replacePhases(phases);
    }

    public SignalState state(String id) {
        return registry.get(id).orElseThrow(() -> new IllegalArgumentException("not found")).snapshot();
    }

    public List<StateChange> history(String id, int limit) {
        return registry.get(id).orElseThrow(() -> new IllegalArgumentException("not found")).history(limit);
    }

    public void pause(String id) {
        registry.get(id).orElseThrow(() -> new IllegalArgumentException("not found")).pause();
    }
    public void resume(String id) {
        registry.get(id).orElseThrow(() -> new IllegalArgumentException("not found")).resume();
    }
    public void advanceOnce(String id) {
        registry.get(id).orElseThrow(() -> new IllegalArgumentException("not found")).advanceOnce();
    }

    private List<Phase> defaultPhases() {
        List<Phase> phases = new ArrayList<>();
        phases.add(new Phase("NS_GREEN", Set.of(Direction.NORTH, Direction.SOUTH), 8000, 3000, 1000));
        phases.add(new Phase("EW_GREEN", Set.of(Direction.EAST, Direction.WEST), 8000, 3000, 1000));
        return phases;
    }
}


