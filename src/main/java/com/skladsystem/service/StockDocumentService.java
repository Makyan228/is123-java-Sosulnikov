package com.skladsystem.service;

import com.skladsystem.model.StockDocument;
import com.skladsystem.repository.StockDocumentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StockDocumentService {

    private final StockDocumentRepository stockDocumentRepository;

    public StockDocumentService(StockDocumentRepository stockDocumentRepository) {
        this.stockDocumentRepository = stockDocumentRepository;
    }

    public List<StockDocument> findAllReceipts() {
        return stockDocumentRepository.findAllReceipts();
    }

    public StockDocument findReceiptById(Long id) {
        return stockDocumentRepository.findReceiptById(id);
    }

    public void saveReceipt(StockDocument document) {
        stockDocumentRepository.saveReceipt(document);
    }

    public void updateReceipt(StockDocument document) {
        stockDocumentRepository.updateReceipt(document);
    }
}