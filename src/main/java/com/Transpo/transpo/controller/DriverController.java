package com.Transpo.transpo.controller;

import com.Transpo.transpo.model.Bus;
import com.Transpo.transpo.model.BusStop;
import com.Transpo.transpo.model.Route;
import com.Transpo.transpo.service.DriverService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/driver")
public class DriverController {
    
    private final DriverService driverService;
    private final Map<String, Map<String, Object>> liveLocations = new ConcurrentHashMap<>();
    
    public DriverController(DriverService driverService) {
        this.driverService = driverService;
    }
    
    // Get driver's assigned bus
    @GetMapping("/my-bus")
    public ResponseEntity<Map<String, Object>> getMyBus() {
        Bus bus = driverService.getAssignedBus();
        if (bus == null) {
            return ResponseEntity.ok(Map.of("id", null, "busNumber", null, "busName", null));
        }
        return ResponseEntity.ok(Map.of(
                "id", bus.getId(),
                "busNumber", bus.getBusNumber(),
                "busName", bus.getBusName(),
                "totalSeats", bus.getTotalSeats()
        ));
    }
    
    // Change driver's assigned bus
    @PutMapping("/my-bus")
    public ResponseEntity<Bus> changeMyBus(@RequestBody Map<String, Long> request) {
        Long busId = request.get("busId");
        if (busId == null || busId <= 0) {
            return ResponseEntity.badRequest().build();
        }
        Bus bus = driverService.changeAssignedBus(busId);
        return ResponseEntity.ok(bus);
    }
    
    // Add bus stops to route
    @PostMapping("/routes/{routeId}/stops")
    public ResponseEntity<List<BusStop>> addBusStops(
            @PathVariable Long routeId,
            @RequestBody List<BusStop> busStops) {
        List<BusStop> stops = driverService.addBusStopsToRoute(routeId, busStops);
        return ResponseEntity.ok(stops);
    }
    
    // Get all stops for a route
    @GetMapping("/routes/{routeId}/stops")
    public ResponseEntity<List<BusStop>> getRouteStops(@PathVariable Long routeId) {
        List<BusStop> stops = driverService.getRouteStops(routeId);
        return ResponseEntity.ok(stops);
    }
    
    // Get route details with stops
    @GetMapping("/routes/{routeId}/details")
    public ResponseEntity<Map<String, Object>> getRouteDetails(@PathVariable Long routeId) {
        Map<String, Object> details = driverService.getRouteDetails(routeId);
        return ResponseEntity.ok(details);
    }
    
    // Get passenger pickup/drop statistics
    @GetMapping("/routes/{routeId}/passenger-stats")
    public ResponseEntity<Map<String, Object>> getPassengerStats(@PathVariable Long routeId) {
        Map<String, Object> stats = driverService.getPassengerStats(routeId);
        return ResponseEntity.ok(stats);
    }
    
    // Get all routes for driver's bus
    @GetMapping("/my-routes")
    public ResponseEntity<List<Route>> getMyRoutes() {
        List<Route> routes = driverService.getDriverRoutes();
        return ResponseEntity.ok(routes);
    }
    
    // Get map data for frontend
    @GetMapping("/routes/{routeId}/map-data")
    public ResponseEntity<Map<String, Object>> getMapData(@PathVariable Long routeId) {
        Map<String, Object> mapData = driverService.getMapData(routeId);
        return ResponseEntity.ok(mapData);
    }

    // Driver: reservations for the assigned bus
    @GetMapping("/reservations")
    public ResponseEntity<List<Map<String, Object>>> getAssignedBusReservations() {
        List<Map<String, Object>> list = driverService.getAssignedBusReservations();
        return ResponseEntity.ok(list);
    }

    // Live location: driver posts current lat/lng; stored in-memory keyed by username
    @PostMapping("/location")
    public ResponseEntity<Map<String, Object>> updateLocation(@RequestBody Map<String, Object> payload,
                                                              Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("message", "unauthenticated"));
        }
        String user = auth.getName();
        Double lat = null, lng = null;
        try {
            lat = payload.get("lat") != null ? Double.valueOf(String.valueOf(payload.get("lat"))) : null;
            lng = payload.get("lng") != null ? Double.valueOf(String.valueOf(payload.get("lng"))) : null;
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "invalid lat/lng"));
        }
        if (lat == null || lng == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "lat and lng required"));
        }
        Map<String, Object> info = new ConcurrentHashMap<>();
        info.put("lat", lat);
        info.put("lng", lng);
        info.put("updatedAt", System.currentTimeMillis());
        liveLocations.put(user, info);
        return ResponseEntity.ok(info);
    }

    // Get last known location for current driver
    @GetMapping("/location")
    public ResponseEntity<Map<String, Object>> getMyLocation(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("message", "unauthenticated"));
        }
        String user = auth.getName();
        Map<String, Object> info = liveLocations.get(user);
        if (info == null) {
            return ResponseEntity.ok(Map.of("message", "no location", "lat", null, "lng", null));
        }
        return ResponseEntity.ok(info);
    }
}