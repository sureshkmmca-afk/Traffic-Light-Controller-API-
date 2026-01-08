
package com.example.traffic.util;

import com.example.traffic.model.Direction;
import com.example.traffic.model.LightColor;

import java.util.Map;
import java.util.Set;

public final class SignalValidator {
    private SignalValidator() {}

    private static final Set<Direction> NS = Set.of(Direction.NORTH, Direction.SOUTH);
    private static final Set<Direction> EW = Set.of(Direction.EAST, Direction.WEST);

    /**
     * Validates that no conflicting directions are GREEN simultaneously.
     * We assume NS are compatible together, and EW are compatible together, but NS vs EW conflict.
     */
    public static void validateNoConflictingGreens(Map<Direction, LightColor> lights) {
        boolean nsGreen = lights.entrySet().stream()
                .anyMatch(e -> NS.contains(e.getKey()) && e.getValue() == LightColor.GREEN);
        boolean ewGreen = lights.entrySet().stream()
                .anyMatch(e -> EW.contains(e.getKey()) && e.getValue() == LightColor.GREEN);
        if (nsGreen && ewGreen) {
            throw new IllegalStateException("Invalid signal: NS and EW cannot be GREEN at the same time");
        }
    }
}
