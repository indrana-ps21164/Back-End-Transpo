package com.Transpo.transpo.controller;

import com.Transpo.transpo.model.TicketPrice;
import com.Transpo.transpo.service.TicketPriceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ticket-prices")
public class TicketPriceController {
    private final TicketPriceService service;
    public TicketPriceController(TicketPriceService service) { this.service = service; }

    @GetMapping("/route/{routeId}")
    public List<TicketPrice> list(@PathVariable Long routeId) {
        return service.listByRoute(routeId);
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        try {
            Long routeId = parseLong(body.get("routeId"));
            Long fromStopId = parseLong(body.get("fromStopId"));
            Long toStopId = parseLong(body.get("toStopId"));
            BigDecimal price = new BigDecimal(String.valueOf(body.get("price")));
            TicketPrice tp = service.create(routeId, fromStopId, toStopId, price);
            return ResponseEntity.ok(tp);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        try {
            BigDecimal price = new BigDecimal(String.valueOf(body.get("price")));
            TicketPrice tp = service.update(id, price);
            return ResponseEntity.ok(tp);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private Long parseLong(Object o) { return o == null ? null : Long.valueOf(String.valueOf(o)); }
}
