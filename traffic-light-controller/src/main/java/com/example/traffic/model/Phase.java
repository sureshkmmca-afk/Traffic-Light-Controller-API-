
package com.example.traffic.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Phase {
    private final String name;
    private final Set<Direction> greens;
    private final long greenMs;
    private final long yellowMs;
    private final long allRedMs;

    public Phase(String name, Set<Direction> greens, long greenMs, long yellowMs, long allRedMs) {
        this.name = Objects.requireNonNull(name);
        this.greens = Collections.unmodifiableSet(new HashSet<>(Objects.requireNonNull(greens)));
        this.greenMs = greenMs;
        this.yellowMs = yellowMs;
        this.allRedMs = allRedMs;
    }

    public String getName() { return name; }
    public Set<Direction> getGreens() { return greens; }
    public long getGreenMs() { return greenMs; }
    public long getYellowMs() { return yellowMs; }
    public long getAllRedMs() { return allRedMs; }
}
