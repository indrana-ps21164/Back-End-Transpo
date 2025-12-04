package com.Transpo.transpo.mapper;

import com.Transpo.transpo.dto.RouteDTO;
import com.Transpo.transpo.model.Route;

public class RouteMapper {
    public static RouteDTO toDto(Route r) {
        if (r == null) return null;
        RouteDTO d = new RouteDTO();
        d.setId(r.getId());
        d.setOrigin(r.getOrigin());
        d.setDestination(r.getDestination());
        return d;
    }

    public static Route toEntity(RouteDTO d) {
        if (d == null) return null;
        Route r = new Route();
        r.setId(d.getId());
        r.setOrigin(d.getOrigin());
        r.setDestination(d.getDestination());
        return r;
    }
}
