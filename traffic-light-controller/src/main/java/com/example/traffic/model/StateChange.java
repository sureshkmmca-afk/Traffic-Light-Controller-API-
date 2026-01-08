package com.example.traffic.model;


import java.time.Instant;

public record StateChange(Instant at, String phase, String description) {}

