package com.skladsystem.controller;

import com.skladsystem.model.StockDocument;
import com.skladsystem.model.StockDocumentItem;
import com.skladsystem.model.StorageLocation;
import com.skladsystem.repository.ProductRepository;
import com.skladsystem.repository.StockDocumentItemRepository;
import com.skladsystem.repository.StockDocumentRepository;
import com.skladsystem.repository.StorageLocationRepository;
import com.skladsystem.repository.WarehouseRepository;
import com.skladsystem.service.CurrentUserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;

@Controller
public class ReceiptController {

    private final StockDocumentRepository stockDocumentRepository;
    private final StockDocumentItemRepository stockDocumentItemRepository;
    private final WarehouseRepository warehouseRepository;
    private final StorageLocationRepository storageLocationRepository;
    private final ProductRepository productRepository;
    private final CurrentUserService currentUserService;

    public ReceiptController(StockDocumentRepository stockDocumentRepository,
                             StockDocumentItemRepository stockDocumentItemRepository,
                             WarehouseRepository warehouseRepository,
                             StorageLocationRepository storageLocationRepository,
                             ProductRepository productRepository,
                             CurrentUserService currentUserService) {
        this.stockDocumentRepository = stockDocumentRepository;
        this.stockDocumentItemRepository = stockDocumentItemRepository;
        this.warehouseRepository = warehouseRepository;
        this.storageLocationRepository = storageLocationRepository;
        this.productRepository = productRepository;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/receipts")
    public String receipts(Model model, HttpSession session) {
        if (!currentUserService.canAccessReceipts(session)) {
            return "redirect:/";
        }

        model.addAttribute("receipts", stockDocumentRepository.findAllReceipts());
        return "receipts";
    }

    @GetMapping("/receipts/new")
    public String newReceipt(Model model, HttpSession session) {
        if (!currentUserService.canAccessReceipts(session)) {
            return "redirect:/";
        }

        StockDocument receipt = new StockDocument();
        receipt.setDocDate(LocalDate.now());
        receipt.setDocStatus("DRAFT");
        receipt.setDocNumber(stockDocumentRepository.getNextReceiptNumber());

        model.addAttribute("receipt", receipt);
        model.addAttribute("warehouses", warehouseRepository.findAllActive());
        model.addAttribute("pageTitle", "Новое поступление");
        model.addAttribute("pageSubtitle", "Создание документа поступления");
        model.addAttribute("submitButtonText", "Сохранить документ");

        return "receipt-form";
    }

    @PostMapping("/receipts")
    public String saveReceipt(StockDocument receipt,
                              RedirectAttributes redirectAttributes,
                              HttpSession session) {
        if (!currentUserService.canAccessReceipts(session)) {
            return "redirect:/";
        }

        Long currentUserId = currentUserService.getCurrentUserId(session);
        if (currentUserId != null) {
            receipt.setCreatedBy(currentUserId.intValue());
        }

        Long id = stockDocumentRepository.saveReceiptAndReturnId(receipt);
        redirectAttributes.addFlashAttribute("successMessage", "Поступление создано. Теперь добавь товары.");
        return "redirect:/receipts/" + id + "/items";
    }

    @GetMapping("/receipts/{id}/edit")
    public String editReceipt(@PathVariable Long id, Model model, HttpSession session) {
        if (!currentUserService.canAccessReceipts(session)) {
            return "redirect:/";
        }

        StockDocument receipt = stockDocumentRepository.findReceiptById(id);

        if (receipt == null) {
            return "redirect:/receipts";
        }

        model.addAttribute("receipt", receipt);
        model.addAttribute("warehouses", warehouseRepository.findAllActive());
        model.addAttribute("pageTitle", "Редактирование поступления");
        model.addAttribute("pageSubtitle", "Изменение шапки документа поступления");
        model.addAttribute("submitButtonText", "Сохранить изменения");

        return "receipt-form";
    }

    @PostMapping("/receipts/{id}")
    public String updateReceipt(@PathVariable Long id,
                                StockDocument receipt,
                                RedirectAttributes redirectAttributes,
                                HttpSession session) {
        if (!currentUserService.canAccessReceipts(session)) {
            return "redirect:/";
        }

        receipt.setId(id);
        stockDocumentRepository.updateReceipt(receipt);
        redirectAttributes.addFlashAttribute("successMessage", "Поступление обновлено.");
        return "redirect:/receipts/" + id + "/items";
    }

    @GetMapping("/receipts/{id}/items")
    public String receiptItems(@PathVariable Long id, Model model, HttpSession session) {
        if (!currentUserService.canAccessReceipts(session)) {
            return "redirect:/";
        }

        StockDocument receipt = stockDocumentRepository.findReceiptById(id);

        if (receipt == null) {
            return "redirect:/receipts";
        }

        StockDocumentItem newItem = new StockDocumentItem();
        newItem.setDocumentId(id);

        StorageLocation defaultLocation =
                storageLocationRepository.findFirstActiveByWarehouseId(receipt.getWarehouseId());

        var items = stockDocumentItemRepository.findByDocumentId(id);

        BigDecimal documentTotal = BigDecimal.ZERO;
        for (StockDocumentItem item : items) {
            documentTotal = documentTotal.add(item.getLineTotal());
        }

        model.addAttribute("receipt", receipt);
        model.addAttribute("items", items);
        model.addAttribute("documentTotal", documentTotal);
        model.addAttribute("newItem", newItem);
        model.addAttribute("products", productRepository.findAll());
        model.addAttribute("defaultLocation", defaultLocation);

        return "receipt-items";
    }

    @PostMapping("/receipts/{id}/items")
    public String saveReceiptItem(@PathVariable Long id,
                                  StockDocumentItem newItem,
                                  RedirectAttributes redirectAttributes,
                                  HttpSession session) {
        if (!currentUserService.canAccessReceipts(session)) {
            return "redirect:/";
        }

        StockDocument receipt = stockDocumentRepository.findReceiptById(id);

        if (receipt == null) {
            return "redirect:/receipts";
        }

        StorageLocation defaultLocation =
                storageLocationRepository.findFirstActiveByWarehouseId(receipt.getWarehouseId());

        if (defaultLocation == null) {
            throw new IllegalStateException("Для выбранного склада нет активной локации хранения.");
        }

        newItem.setDocumentId(id);
        newItem.setTargetLocationId(defaultLocation.getId());

        stockDocumentItemRepository.saveIncoming(newItem);
        redirectAttributes.addFlashAttribute("successMessage", "Позиция поступления добавлена.");
        return "redirect:/receipts/" + id + "/items";
    }

    @PostMapping("/receipts/{receiptId}/items/{itemId}/delete")
    public String deleteReceiptItem(@PathVariable Long receiptId,
                                    @PathVariable Long itemId,
                                    RedirectAttributes redirectAttributes,
                                    HttpSession session) {
        if (!currentUserService.canAccessReceipts(session)) {
            return "redirect:/";
        }

        stockDocumentItemRepository.deleteByIdAndDocumentId(itemId, receiptId);
        redirectAttributes.addFlashAttribute("successMessage", "Позиция удалена.");
        return "redirect:/receipts/" + receiptId + "/items";
    }

    @PostMapping("/receipts/{id}/delete")
    @Transactional
    public String deleteReceipt(@PathVariable Long id,
                                RedirectAttributes redirectAttributes,
                                HttpSession session) {
        if (!currentUserService.canAccessReceipts(session)) {
            return "redirect:/";
        }

        stockDocumentItemRepository.deleteByDocumentId(id);
        stockDocumentRepository.deleteReceiptById(id);
        redirectAttributes.addFlashAttribute("successMessage", "Поступление удалено.");
        return "redirect:/receipts";
    }
}