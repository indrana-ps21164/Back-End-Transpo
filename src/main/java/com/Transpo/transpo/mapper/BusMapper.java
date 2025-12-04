package com.Transpo.transpo.mapper;

import com.Transpo.transpo.dto.BusDTO;
import com.Transpo.transpo.model.Bus;

public class BusMapper {
    public static BusDTO toDto(Bus b) {
        if (b == null) return null;
        BusDTO d = new BusDTO();
        d.setId(b.getId());
        d.setBusNumber(b.getBusNumber());
        d.setBusName(b.getBusName());
        d.setTotalSeats(b.getTotalSeats());
        return d;
    }

    public static Bus toEntity(BusDTO d) {
        if (d == null) return null;
        Bus b = new Bus();
        b.setId(d.getId());
        b.setBusNumber(d.getBusNumber());
        b.setBusName(d.getBusName());
        b.setTotalSeats(d.getTotalSeats());
        return b;
    }
}
