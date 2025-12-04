package com.Transpo.transpo.service;

import com.Transpo.transpo.exception.NotFoundException;
import com.Transpo.transpo.model.Bus;
import com.Transpo.transpo.repository.BusRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BusService {

    private final BusRepository busRepository;

    public BusService(BusRepository busRepository) {
        this.busRepository = busRepository;
    }

    public Bus addBus(Bus bus) {
        return busRepository.save(bus);
    }

    public List<Bus> getAllBuses() {
        return busRepository.findAll();
    }

    public Bus getBusById(Long id) {
        return busRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Bus not found with id: " + id));
    }

    public Bus updateBus(Long id, Bus updatedBus) {
        Bus bus = getBusById(id);

        bus.setBusNumber(updatedBus.getBusNumber());
        bus.setBusName(updatedBus.getBusName());
        bus.setTotalSeats(updatedBus.getTotalSeats());

        return busRepository.save(bus);
    }

    public void deleteBus(Long id) {
        if (!busRepository.existsById(id)) {
            throw new NotFoundException("Bus not found with id: " + id);
        }
        busRepository.deleteById(id);
    }
}
