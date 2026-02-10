package com.Transpo.transpo.controller;

import com.Transpo.transpo.dto.BusStopDTO;
import com.Transpo.transpo.dto.RouteWithStopsDTO;
import com.Transpo.transpo.model.BusStop;
import com.Transpo.transpo.model.Route;
import com.Transpo.transpo.repository.BusStopRepository;
import com.Transpo.transpo.repository.RouteRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/map")
public class MapController {

    private final RouteRepository routeRepository;
    private final BusStopRepository busStopRepository;
    // Keep it simple for read-only dashboard

    public MapController(RouteRepository routeRepository,
                         BusStopRepository busStopRepository) {
        this.routeRepository = routeRepository;
        this.busStopRepository = busStopRepository;
    }

    /**
     * Return routes with ordered stops including name and coordinates for dashboard view.
     */
    @GetMapping("/routes-with-stops")
    public ResponseEntity<List<RouteWithStopsDTO>> routesWithStops() {
        List<Route> routes = routeRepository.findAll();
        List<BusStop> stops = busStopRepository.findAll();

        // Group stops by routeId; ensure order by sequence/index if present
        Map<Long, List<BusStop>> byRoute = stops.stream()
                .filter(s -> s.getRoute() != null && s.getRoute().getId() != null)
                .collect(Collectors.groupingBy(s -> s.getRoute().getId()));

        List<RouteWithStopsDTO> result = new ArrayList<>();
        for (Route r : routes) {
            RouteWithStopsDTO dto = new RouteWithStopsDTO();
            dto.setId(r.getId());
            dto.setOrigin(r.getOrigin());
            dto.setDestination(r.getDestination());
            List<BusStop> rs = byRoute.getOrDefault(r.getId(), List.of());
            // Sort by sequence/order if field exists; else by name
            rs = rs.stream()
                    .sorted(Comparator.comparingInt(s -> {
                        try {
                            // attempt to read a sequence/order via reflection if present
                            var f = s.getClass().getDeclaredField("sequence");
                            f.setAccessible(true);
                            Object v = f.get(s);
                            return v instanceof Integer ? (Integer) v : Integer.MAX_VALUE;
                        } catch (Exception ignore) { return Integer.MAX_VALUE; }
                    }))
                    .collect(Collectors.toList());

            List<BusStopDTO> stopDtos = rs.stream().map(s -> {
                BusStopDTO sd = new BusStopDTO();
                sd.setId(s.getId());
                sd.setName(s.getName());
                sd.setLatitude(s.getLatitude());
                sd.setLongitude(s.getLongitude());
                return sd;
            }).collect(Collectors.toList());
            dto.setStops(stopDtos);
            result.add(dto);
        }

        return ResponseEntity.ok(result);
    }
}