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
    d.setStop01(r.getStop01());
    d.setStop02(r.getStop02());
    d.setStop03(r.getStop03());
    d.setStop04(r.getStop04());
    d.setStop05(r.getStop05());
    d.setStop06(r.getStop06());
    d.setStop07(r.getStop07());
    d.setStop08(r.getStop08());
    d.setStop09(r.getStop09());
    d.setStop10(r.getStop10());
        return d;
    }

    public static Route toEntity(RouteDTO d) {
        if (d == null) return null;
        Route r = new Route();
        r.setId(d.getId());
        r.setOrigin(d.getOrigin());
        r.setDestination(d.getDestination());
    r.setStop01(d.getStop01());
    r.setStop02(d.getStop02());
    r.setStop03(d.getStop03());
    r.setStop04(d.getStop04());
    r.setStop05(d.getStop05());
    r.setStop06(d.getStop06());
    r.setStop07(d.getStop07());
    r.setStop08(d.getStop08());
    r.setStop09(d.getStop09());
    r.setStop10(d.getStop10());
        return r;
    }
}
