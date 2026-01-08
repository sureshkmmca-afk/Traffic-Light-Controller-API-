
package com.example.traffic.repo;

import com.example.traffic.service.IntersectionRunner;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class IntersectionRegistry {

    private final Map<String, IntersectionRunner> map = new ConcurrentHashMap<>();

    public Optional<IntersectionRunner> get(String id) {
        return Optional.ofNullable(map.get(id));
    }

    public boolean exists(String id) {
        return map.containsKey(id);
    }

    public void put(String id, IntersectionRunner runner) {
        map.put(id, runner);
    }

    public void remove(String id) {
        map.remove(id);
    }
}
