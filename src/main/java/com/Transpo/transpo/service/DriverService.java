package com.Transpo.transpo.service;

import com.Transpo.transpo.exception.BadRequestException;
import com.Transpo.transpo.exception.NotFoundException;
import com.Transpo.transpo.model.*;
import com.Transpo.transpo.repository.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class DriverService {
    
    private final DriverAssignmentRepository driverAssignmentRepo;
    private final UserRepository userRepository;
    private final BusRepository busRepository;
    private final BusStopRepository busStopRepository;
    private final RouteRepository routeRepository;
    private final ReservationRepository reservationRepository;
    private final ScheduleRepository scheduleRepository;
    
    public DriverService(DriverAssignmentRepository driverAssignmentRepo,
                        UserRepository userRepository,
                        BusRepository busRepository,
                        BusStopRepository busStopRepository,
                        RouteRepository routeRepository,
                        ReservationRepository reservationRepository,
                        ScheduleRepository scheduleRepository) {
        this.driverAssignmentRepo = driverAssignmentRepo;
        this.userRepository = userRepository;
        this.busRepository = busRepository;
        this.busStopRepository = busStopRepository;
        this.routeRepository = routeRepository;
        this.reservationRepository = reservationRepository;
        this.scheduleRepository = scheduleRepository;
    }
    
    /**
     * Get current username from security context
     */
    private String getCurrentUsername() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        }
        return principal.toString();
    }
    
    /**
     * Get current driver user
     */
    private User getCurrentDriver() {
        String username = getCurrentUsername();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found: " + username));
    }
    
    /**
     * Get driver's assigned bus
     */
    public Bus getAssignedBus() {
        User driver = getCurrentDriver();
        DriverAssignment assignment = driverAssignmentRepo.findByDriverId(driver.getId())
                .orElseThrow(() -> new NotFoundException("No bus assigned to driver"));
        
        return assignment.getBus();
    }
    
    /**
     * Change driver's assigned bus
     */
    @Transactional
    public Bus changeAssignedBus(Long busId) {
        User driver = getCurrentDriver();
        Bus newBus = busRepository.findById(busId)
                .orElseThrow(() -> new NotFoundException("Bus not found: " + busId));
        
        // Check if bus is already assigned to another driver
        driverAssignmentRepo.findByBusId(busId).ifPresent(existing -> {
            if (!existing.getDriver().getId().equals(driver.getId())) {
                throw new BadRequestException("Bus is already assigned to another driver");
            }
        });
        
        DriverAssignment assignment = driverAssignmentRepo.findByDriverId(driver.getId())
                .orElse(new DriverAssignment(driver, newBus));
        
        assignment.setBus(newBus);
        driverAssignmentRepo.save(assignment);
        
        return newBus;
    }
    
    /**
     * Add bus stops to a route
     */
    @Transactional
    public List<BusStop> addBusStopsToRoute(Long routeId, List<BusStop> busStops) {
        User driver = getCurrentDriver();
        
        // Verify driver has access to this route (through bus assignments and schedules)
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new NotFoundException("Route not found: " + routeId));
        
        // Check if driver's assigned bus has schedules on this route
        DriverAssignment assignment = driverAssignmentRepo.findByDriverId(driver.getId())
                .orElseThrow(() -> new NotFoundException("Driver has no bus assignment"));
        
        boolean hasAccess = scheduleRepository.findByRouteOriginAndRouteDestination(
                route.getOrigin(), route.getDestination())
                .stream()
                .anyMatch(schedule -> schedule.getBus().getId().equals(assignment.getBus().getId()));
        
        if (!hasAccess) {
            throw new BadRequestException("Driver does not have access to this route");
        }
        
        // Add stops to route
        for (BusStop stop : busStops) {
            stop.setRoute(route);
            route.addBusStop(stop);
        }
        
        routeRepository.save(route);
        return busStops;
    }
    
    /**
     * Get all stops for a route
     */
    public List<BusStop> getRouteStops(Long routeId) {
        return busStopRepository.findByRouteIdOrderBySequenceAsc(routeId);
    }
    
    /**
     * Get route details with stops
     */
    public Map<String, Object> getRouteDetails(Long routeId) {
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new NotFoundException("Route not found: " + routeId));
        
        List<BusStop> stops = getRouteStops(routeId);
        
        Map<String, Object> details = new HashMap<>();
        details.put("route", route);
        details.put("stops", stops);
        
        return details;
    }
    
    /**
     * Get passenger pickup and drop statistics by stop
     */
    public Map<String, Object> getPassengerStats(Long routeId) {
        User driver = getCurrentDriver();
        DriverAssignment assignment = driverAssignmentRepo.findByDriverId(driver.getId())
                .orElseThrow(() -> new NotFoundException("Driver has no bus assignment"));
        
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new NotFoundException("Route not found: " + routeId));
        
        List<BusStop> stops = busStopRepository.findByRouteIdOrderBySequenceAsc(routeId);
        List<Schedule> schedules = scheduleRepository.findByRouteOriginAndRouteDestination(
                route.getOrigin(), route.getDestination());
        
        // Filter schedules for driver's bus
        List<Schedule> driverSchedules = schedules.stream()
                .filter(s -> s.getBus().getId().equals(assignment.getBus().getId()))
                .collect(Collectors.toList());
        
        Map<Long, Map<String, Object>> stopStats = new HashMap<>();
        
        // Initialize stop stats
        for (BusStop stop : stops) {
            Map<String, Object> stats = new HashMap<>();
            stats.put("stopName", stop.getName());
            stats.put("latitude", stop.getLatitude());
            stats.put("longitude", stop.getLongitude());
            stats.put("pickupCount", 0);
            stats.put("dropCount", 0);
            stats.put("pickupPassengers", new java.util.ArrayList<String>());
            stats.put("dropPassengers", new java.util.ArrayList<String>());
            stopStats.put(stop.getId(), stats);
        }
        
        // Collect reservation data
        for (Schedule schedule : driverSchedules) {
            List<Reservation> reservations = reservationRepository.findByScheduleId(schedule.getId());
            
            for (Reservation reservation : reservations) {
                if (reservation.getPickupStop() != null && stopStats.containsKey(reservation.getPickupStop().getId())) {
                    Map<String, Object> stats = stopStats.get(reservation.getPickupStop().getId());
                    stats.put("pickupCount", (Integer) stats.get("pickupCount") + 1);
                    ((java.util.List<String>) stats.get("pickupPassengers")).add(
                            reservation.getPassengerName() + " (Seat: " + reservation.getSeatNumber() + ")"
                    );
                }
                
                if (reservation.getDropStop() != null && stopStats.containsKey(reservation.getDropStop().getId())) {
                    Map<String, Object> stats = stopStats.get(reservation.getDropStop().getId());
                    stats.put("dropCount", (Integer) stats.get("dropCount") + 1);
                    ((java.util.List<String>) stats.get("dropPassengers")).add(
                            reservation.getPassengerName() + " (Seat: " + reservation.getSeatNumber() + ")"
                    );
                }
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("route", route);
        result.put("stops", stopStats.values());
        result.put("totalPassengers", stopStats.values().stream()
                .mapToInt(stats -> (Integer) stats.get("pickupCount"))
                .sum());
        
        return result;
    }
    
    /**
     * Get all routes for driver's bus
     */
    public List<Route> getDriverRoutes() {
        User driver = getCurrentDriver();
        DriverAssignment assignment = driverAssignmentRepo.findByDriverId(driver.getId())
                .orElseThrow(() -> new NotFoundException("Driver has no bus assignment"));
        
        // Get all schedules for driver's bus
        return scheduleRepository.findAll().stream()
                .filter(s -> s.getBus().getId().equals(assignment.getBus().getId()))
                .map(Schedule::getRoute)
                .distinct()
                .collect(Collectors.toList());
    }
    
    /**
     * Get map data for frontend
     */
    public Map<String, Object> getMapData(Long routeId) {
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new NotFoundException("Route not found: " + routeId));
        
        List<BusStop> stops = busStopRepository.findByRouteIdOrderBySequenceAsc(routeId);
        Map<String, Object> passengerStats = getPassengerStats(routeId);
        
        Map<String, Object> mapData = new HashMap<>();
        mapData.put("route", Map.of(
                "origin", route.getOrigin(),
                "destination", route.getDestination(),
                "originCoords", stops.isEmpty() ? null : Map.of(
                        "lat", stops.get(0).getLatitude(),
                        "lng", stops.get(0).getLongitude()
                ),
                "destinationCoords", stops.isEmpty() ? null : Map.of(
                        "lat", stops.get(stops.size() - 1).getLatitude(),
                        "lng", stops.get(stops.size() - 1).getLongitude()
                )
        ));
        
        mapData.put("stops", stops.stream()
                .map(stop -> Map.of(
                        "id", stop.getId(),
                        "name", stop.getName(),
                        "lat", stop.getLatitude(),
                        "lng", stop.getLongitude(),
                        "sequence", stop.getSequence()
                ))
                .collect(Collectors.toList()));
        
        mapData.put("passengerStats", passengerStats);
        
        return mapData;
    }
}