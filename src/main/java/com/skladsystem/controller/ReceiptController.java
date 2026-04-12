package com.skladsystem.controller;

import com.skladsystem.model.StockDocument;
import com.skladsystem.repository.WarehouseRepository;
import com.skladsystem.service.CounterpartyService;
import com.skladsystem.service.StockDocumentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.time.LocalDate;

@Controller
public class ReceiptController {

    private final StockDocumentService stockDocumentService;
    private final CounterpartyService counterpartyService;
    private final WarehouseRepository warehouseRepository;

    public ReceiptController(StockDocumentService stockDocumentService,
                             CounterpartyService counterpartyService,
                             WarehouseRepository warehouseRepository) {
        this.stockDocumentService = stockDocumentService;
        this.counterpartyService = counterpartyService;
        this.warehouseRepository = warehouseRepository;
    }

    @GetMapping("/receipts")
    public String receipts(Model model) {
        model.addAttribute("receipts", stockDocumentService.findAllReceipts());
        return "receipts";
    }

    @GetMapping("/receipts/new")
    public String newReceipt(Model model) {
        StockDocument receipt = new StockDocument();
        receipt.setDocDate(LocalDate.now());
        receipt.setDocStatus("DRAFT");

        model.addAttribute("receipt", receipt);
        model.addAttribute("suppliers", counterpartyService.findAllSuppliers());
        model.addAttribute("warehouses", warehouseRepository.findAllActive());
        model.addAttribute("pageTitle", "Новое поступление");
        model.addAttribute("pageSubtitle", "Создание документа поступления");
        model.addAttribute("submitButtonText", "Сохранить документ");

        return "receipt-form";
    }

    @PostMapping("/receipts")
    public String saveReceipt(StockDocument receipt) {
        stockDocumentService.saveReceipt(receipt);
        return "redirect:/receipts";
    }

    @GetMapping("/receipts/{id}/edit")
    public String editReceipt(@PathVariable Long id, Model model) {
        StockDocument receipt = stockDocumentService.findReceiptById(id);

        if (receipt == null) {
            return "redirect:/receipts";
        }

        model.addAttribute("receipt", receipt);
        model.addAttribute("suppliers", counterpartyService.findAllSuppliers());
        model.addAttribute("warehouses", warehouseRepository.findAllActive());
        model.addAttribute("pageTitle", "Редактирование поступления");
        model.addAttribute("pageSubtitle", "Изменение шапки документа поступления");
        model.addAttribute("submitButtonText", "Сохранить изменения");

        return "receipt-form";
    }

    @PostMapping("/receipts/{id}")
    public String updateReceipt(@PathVariable Long id, StockDocument receipt) {
        receipt.setId(id);
        stockDocumentService.updateReceipt(receipt);
        return "redirect:/receipts";
    }
}