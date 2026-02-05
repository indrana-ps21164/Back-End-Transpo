package com.Transpo.transpo.mapper;

import com.Transpo.transpo.dto.DriverAssignmentDTO;
import com.Transpo.transpo.model.DriverAssignment;

public class DriverAssignmentMapper {
    
    public static DriverAssignmentDTO toDto(DriverAssignment assignment) {
        if (assignment == null) return null;
        
        DriverAssignmentDTO dto = new DriverAssignmentDTO();
        dto.setId(assignment.getId());
        dto.setDriverId(assignment.getDriver() != null ? assignment.getDriver().getId() : null);
        dto.setDriverUsername(assignment.getDriver() != null ? assignment.getDriver().getUsername() : null);
        dto.setBusId(assignment.getBus() != null ? assignment.getBus().getId() : null);
        dto.setBusNumber(assignment.getBus() != null ? assignment.getBus().getBusNumber() : null);
        dto.setAssignedAt(assignment.getAssignedAt());
        
        return dto;
    }
}