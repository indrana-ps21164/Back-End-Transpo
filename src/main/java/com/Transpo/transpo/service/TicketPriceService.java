package com.Transpo.transpo.service;

import com.Transpo.transpo.model.Route;
import com.Transpo.transpo.model.TicketPrice;
import com.Transpo.transpo.repository.BusStopRepository;
import com.Transpo.transpo.repository.RouteRepository;
import com.Transpo.transpo.repository.TicketPriceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class TicketPriceService {
    private final TicketPriceRepository ticketPriceRepository;
    private final RouteRepository routeRepository;
    private final BusStopRepository busStopRepository;
    public TicketPriceService(TicketPriceRepository ticketPriceRepository, RouteRepository routeRepository, BusStopRepository busStopRepository) {
        this.ticketPriceRepository = ticketPriceRepository;
        this.routeRepository = routeRepository;
        this.busStopRepository = busStopRepository;
    }

    public List<TicketPrice> listByRoute(Long routeId) {
        return ticketPriceRepository.findByRouteId(routeId);
    }

    @Transactional
    public TicketPrice create(Long routeId, Long fromStopId, Long toStopId, BigDecimal price) {
        if (routeId == null || fromStopId == null || toStopId == null) throw new IllegalArgumentException("Missing IDs");
        if (fromStopId.equals(toStopId)) throw new IllegalArgumentException("From and To stops must differ");
        Route route = routeRepository.findById(routeId).orElseThrow(() -> new IllegalArgumentException("Route not found"));
        // Validate stops exist and belong to the given route
        boolean fromBelongs = busStopRepository.existsByIdAndRouteId(fromStopId, routeId);
        boolean toBelongs = busStopRepository.existsByIdAndRouteId(toStopId, routeId);
        if (!fromBelongs || !toBelongs) {
            throw new IllegalArgumentException("Both stops must belong to the specified route");
        }
        // Symmetric upsert: if a record exists for either direction, update its price instead of inserting
        return ticketPriceRepository.findSymmetric(routeId, fromStopId, toStopId)
                .map(existing -> {
                    existing.setPrice(price);
                    return ticketPriceRepository.save(existing);
                })
                .orElseGet(() -> {
                    TicketPrice tp = new TicketPrice();
                    tp.setRoute(route);
                    tp.setFromStopId(fromStopId);
                    tp.setToStopId(toStopId);
                    tp.setPrice(price);
                    return ticketPriceRepository.save(tp);
                });
    }

    @Transactional
    public TicketPrice update(Long id, BigDecimal price) {
        TicketPrice tp = ticketPriceRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Ticket price not found"));
        tp.setPrice(price);
        return ticketPriceRepository.save(tp);
    }

    public TicketPrice findPrice(Long routeId, Long fromStopId, Long toStopId) {
        return ticketPriceRepository.findSymmetric(routeId, fromStopId, toStopId).orElse(null);
    }

    // If Route exposes a stop list, add membership validation here.

    @Transactional
    public void delete(Long id) {
        TicketPrice tp = ticketPriceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ticket price not found"));
        ticketPriceRepository.delete(tp);
    }
}
