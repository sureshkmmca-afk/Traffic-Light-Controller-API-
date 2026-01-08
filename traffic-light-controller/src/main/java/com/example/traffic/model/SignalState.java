
package com.example.traffic.model;

import java.time.Instant;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public class SignalState {
    private final Map<Direction, LightColor> lights = new EnumMap<>(Direction.class);
    private final String phaseName;
    private final Instant timestamp;

    public SignalState(Map<Direction, LightColor> lights, String phaseName, Instant timestamp) {
        this.lights.putAll(Objects.requireNonNull(lights));
        this.phaseName = phaseName;
        this.timestamp = timestamp;
    }

    public Map<Direction, LightColor> getLights() { return new EnumMap<>(lights); }
    public String getPhaseName() { return phaseName; }
    public Instant getTimestamp() { return timestamp; }
}
