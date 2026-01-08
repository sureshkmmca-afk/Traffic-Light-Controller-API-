
package com.example.traffic.dto;

import java.time.Instant;

public record HistoryItem(Instant at, String phase, String description) {}
