package com.Transpo.transpo.dto;

import java.math.BigDecimal;

public class TicketPriceDTO {
    public Long id;
    public Long routeId;
    public Long fromStopId;
    public Long toStopId;
    public BigDecimal price;

    public TicketPriceDTO() {}

    public TicketPriceDTO(Long id, Long routeId, Long fromStopId, Long toStopId, BigDecimal price) {
        this.id = id;
        this.routeId = routeId;
        this.fromStopId = fromStopId;
        this.toStopId = toStopId;
        this.price = price;
    }
}