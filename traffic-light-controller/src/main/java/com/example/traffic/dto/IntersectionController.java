
package com.example.traffic.dto;

import com.example.traffic.model.Phase;
import com.example.traffic.model.SignalState;
import com.example.traffic.model.StateChange;
import com.example.traffic.service.TrafficService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/intersections")
@Validated
public class IntersectionController {
    private final TrafficService service = new TrafficService();

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody CreateIntersectionRequest req) {
        try {
            service.create(req.getId());
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}/state")
    public ResponseEntity<StateResponse> state(@PathVariable String id) {
        SignalState s = service.state(id);
        return ResponseEntity.ok(new StateResponse(id, s.getPhaseName(), s.getTimestamp(), s.getLights()));
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<List<HistoryItem>> history(@PathVariable String id, @RequestParam(defaultValue = "100") int limit) {
        List<StateChange> list = service.history(id, limit);
        return ResponseEntity.ok(list.stream().map(sc -> new HistoryItem(sc.at(), sc.phase(), sc.description())).collect(Collectors.toList()));
    }

    @PostMapping("/{id}/commands/pause")
    public ResponseEntity<?> pause(@PathVariable String id) {
        service.pause(id);
        return ResponseEntity.ok(Map.of("status","paused"));
    }

    @PostMapping("/{id}/commands/resume")
    public ResponseEntity<?> resume(@PathVariable String id) {
        service.resume(id);
        return ResponseEntity.ok(Map.of("status","resumed"));
    }

    @PostMapping("/{id}/commands/advance")
    public ResponseEntity<?> advance(@PathVariable String id) {
        service.advanceOnce(id);
        return ResponseEntity.ok(Map.of("status","advanced"));
    }

    @PostMapping("/{id}/sequence")
    public ResponseEntity<?> replaceSequence(@PathVariable String id, @Valid @RequestBody ReplaceSequenceRequest req) {
        List<Phase> phases = req.phases.stream()
                .map(p -> new Phase(p.name, p.greens, p.greenMs, p.yellowMs, p.allRedMs))
                .toList();
        try {
            service.replaceSequence(id, phases);
            return ResponseEntity.ok(Map.of("status","sequence replaced"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleNotFound(IllegalArgumentException e) {
        if (e.getMessage() != null && e.getMessage().equals("not found")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error","intersection not found"));
        }
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
}
