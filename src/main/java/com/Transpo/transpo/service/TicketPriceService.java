package com.Transpo.transpo.service;

import com.Transpo.transpo.model.Route;
import com.Transpo.transpo.model.TicketPrice;
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
    public TicketPriceService(TicketPriceRepository ticketPriceRepository, RouteRepository routeRepository) {
        this.ticketPriceRepository = ticketPriceRepository;
        this.routeRepository = routeRepository;
    }

    public List<TicketPrice> listByRoute(Long routeId) {
        return ticketPriceRepository.findByRouteId(routeId);
    }

    @Transactional
    public TicketPrice create(Long routeId, Long fromStopId, Long toStopId, BigDecimal price) {
        if (routeId == null || fromStopId == null || toStopId == null) throw new IllegalArgumentException("Missing IDs");
        if (fromStopId.equals(toStopId)) throw new IllegalArgumentException("From and To stops must differ");
        Route route = routeRepository.findById(routeId).orElseThrow(() -> new IllegalArgumentException("Route not found"));
    // Assume stops IDs are valid and belong to the route; if Route exposes stop IDs, validate here.
        // Prevent duplicates using symmetric lookup
        ticketPriceRepository.findSymmetric(routeId, fromStopId, toStopId).ifPresent(tp -> { throw new IllegalArgumentException("Ticket price already exists for this stop pair"); });
        TicketPrice tp = new TicketPrice();
        tp.setRoute(route);
    tp.setFromStopId(fromStopId);
    tp.setToStopId(toStopId);
        tp.setPrice(price);
        return ticketPriceRepository.save(tp);
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
}
