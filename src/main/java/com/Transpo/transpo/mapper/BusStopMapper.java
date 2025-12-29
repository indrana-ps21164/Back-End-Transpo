package com.Transpo.transpo.mapper;

import com.Transpo.transpo.dto.BusStopDTO;
import com.Transpo.transpo.model.BusStop;

public class BusStopMapper {
    
    public static BusStopDTO toDto(BusStop busStop) {
        if (busStop == null) return null;
        
        BusStopDTO dto = new BusStopDTO();
        dto.setId(busStop.getId());
        dto.setName(busStop.getName());
        dto.setLatitude(busStop.getLatitude());
        dto.setLongitude(busStop.getLongitude());
        dto.setSequence(busStop.getSequence());
        dto.setRouteId(busStop.getRoute() != null ? busStop.getRoute().getId() : null);
        
        return dto;
    }
    
    public static BusStop toEntity(BusStopDTO dto) {
        if (dto == null) return null;
        
        BusStop busStop = new BusStop();
        busStop.setId(dto.getId());
        busStop.setName(dto.getName());
        busStop.setLatitude(dto.getLatitude());
        busStop.setLongitude(dto.getLongitude());
        busStop.setSequence(dto.getSequence());
        
        return busStop;
    }
}