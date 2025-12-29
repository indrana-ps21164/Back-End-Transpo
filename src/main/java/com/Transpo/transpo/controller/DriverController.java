package com.Transpo.transpo.controller;

import com.Transpo.transpo.model.Bus;
import com.Transpo.transpo.model.BusStop;
import com.Transpo.transpo.model.Route;
import com.Transpo.transpo.service.DriverService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/driver")
public class DriverController {
    
    private final DriverService driverService;
    
    public DriverController(DriverService driverService) {
        this.driverService = driverService;
    }
    
    // Get driver's assigned bus
    @GetMapping("/my-bus")
    public ResponseEntity<Bus> getMyBus() {
        Bus bus = driverService.getAssignedBus();
        return ResponseEntity.ok(bus);
    }
    
    // Change driver's assigned bus
    @PutMapping("/my-bus")
    public ResponseEntity<Bus> changeMyBus(@RequestBody Map<String, Long> request) {
        Long busId = request.get("busId");
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
}