package com.skladsystem.service;

import com.skladsystem.model.Counterparty;
import com.skladsystem.repository.CounterpartyRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CounterpartyService {

    private final CounterpartyRepository counterpartyRepository;

    public CounterpartyService(CounterpartyRepository counterpartyRepository) {
        this.counterpartyRepository = counterpartyRepository;
    }

    public List<Counterparty> findAllSuppliers() {
        return counterpartyRepository.findAllSuppliers();
    }

    public List<Counterparty> searchSuppliers(String search) {
        return counterpartyRepository.searchSuppliers(search);
    }

    public Counterparty findSupplierById(Long id) {
        return counterpartyRepository.findSupplierById(id);
    }

    public void saveSupplier(Counterparty counterparty) {
        counterpartyRepository.saveSupplier(counterparty);
    }

    public void updateSupplier(Counterparty counterparty) {
        counterpartyRepository.updateSupplier(counterparty);
    }

    public void softDeleteSupplier(Long id) {
        counterpartyRepository.softDeleteSupplier(id);
    }
}