
package com.example.traffic.service;

import com.example.traffic.model.Direction;
import com.example.traffic.model.LightColor;
import com.example.traffic.util.SignalValidator;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class SignalValidatorTest {
    @Test
    void rejects_conflicting_greens() {
        Map<Direction, LightColor> lights = new EnumMap<>(Direction.class);
        lights.put(Direction.NORTH, LightColor.GREEN);
        lights.put(Direction.SOUTH, LightColor.RED);
        lights.put(Direction.EAST, LightColor.GREEN);
        lights.put(Direction.WEST, LightColor.RED);
        assertThrows(IllegalStateException.class, () -> SignalValidator.validateNoConflictingGreens(lights));
    }

    @Test
    void allows_ns_green_together() {
        Map<Direction, LightColor> lights = new EnumMap<>(Direction.class);
        lights.put(Direction.NORTH, LightColor.GREEN);
        lights.put(Direction.SOUTH, LightColor.GREEN);
        lights.put(Direction.EAST, LightColor.RED);
        lights.put(Direction.WEST, LightColor.RED);
        assertDoesNotThrow(() -> SignalValidator.validateNoConflictingGreens(lights));
    }
}
