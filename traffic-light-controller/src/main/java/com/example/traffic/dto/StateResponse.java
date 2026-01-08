
package com.example.traffic.dto;

import com.example.traffic.model.Direction;
import com.example.traffic.model.LightColor;

import java.time.Instant;
import java.util.Map;

public record StateResponse(String id, String phase, Instant at, Map<Direction, LightColor> lights) {}
