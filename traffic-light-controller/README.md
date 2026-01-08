
# Traffic Light Controller API (Java 21, Spring Boot)

An API to control traffic light systems at one or more intersections.

## Features
- Manage light state transitions (RED, YELLOW, GREEN) for multiple directions.
- Commands to **pause**, **resume**, **advance**, and **change sequence**.

## Build & Run
```bash
mvn clean package
java -jar target/traffic-light-controller-0.1.0-SNAPSHOT.jar
```

## API (v1)
- `POST /api/v1/intersections` — create an intersection (optional custom phases)
- `GET  /api/v1/intersections/{id}/state` — current state
- `GET  /api/v1/intersections/{id}/history?limit=100` — recent state changes
- `POST /api/v1/intersections/{id}/commands/pause` — pause automation
- `POST /api/v1/intersections/{id}/commands/resume` — resume automation
- `POST /api/v1/intersections/{id}/commands/advance` — manually advance one step (useful for tests)
- `POST /api/v1/intersections/{id}/sequence` — replace the phase sequence (validated)

### Example: Create default intersection
```bash
curl -X POST http://localhost:8080/api/v1/intersections   -H 'Content-Type: application/json'   -d '{"id":"int-1"}'
```

### Example: State
```bash
curl http://localhost:8080/api/v1/intersections/int-1/state
```

### Example: Pause / Resume
```bash
curl -X POST http://localhost:8080/api/v1/intersections/int-1/commands/pause
curl -X POST http://localhost:8080/api/v1/intersections/int-1/commands/resume
```

### Example: Custom sequence (phases)
```bash
curl -X POST http://localhost:8080/api/v1/intersections/int-1/sequence  -H 'Content-Type: application/json'  -d '{
   "phases": [
     {"name":"NS_GREEN","greens":["NORTH","SOUTH"],"greenMs":8000,"yellowMs":3000,"allRedMs":1000},
     {"name":"EW_GREEN","greens":["EAST","WEST"],"greenMs":8000,"yellowMs":3000,"allRedMs":1000}
   ]
 }'
```

## Design Notes
- **Phases** drive the controller: each phase defines which directions are green and durations for green/yellow/all-red.
- Opposite directions can be green together (e.g., NORTH & SOUTH). Perpendicular directions conflict.



