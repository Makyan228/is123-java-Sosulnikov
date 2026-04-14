package com.skladsystem.controller;

import com.skladsystem.model.Product;
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
public class ShipmentController {

    private final StockDocumentRepository stockDocumentRepository;
    private final StockDocumentItemRepository stockDocumentItemRepository;
    private final WarehouseRepository warehouseRepository;
    private final StorageLocationRepository storageLocationRepository;
    private final ProductRepository productRepository;
    private final CurrentUserService currentUserService;

    public ShipmentController(StockDocumentRepository stockDocumentRepository,
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

    @GetMapping("/shipments")
    public String shipments(Model model, HttpSession session) {
        if (!currentUserService.canAccessShipments(session)) {
            return "redirect:/";
        }

        model.addAttribute("shipments", stockDocumentRepository.findAllShipments());
        return "shipments";
    }

    @GetMapping("/shipments/new")
    public String newShipment(Model model, HttpSession session) {
        if (!currentUserService.canAccessShipments(session)) {
            return "redirect:/";
        }

        StockDocument shipment = new StockDocument();
        shipment.setDocDate(LocalDate.now());
        shipment.setDocStatus("DRAFT");
        shipment.setDocNumber(stockDocumentRepository.getNextShipmentNumber());

        model.addAttribute("shipment", shipment);
        model.addAttribute("warehouses", warehouseRepository.findAllActive());
        model.addAttribute("pageTitle", "Новая отгрузка");
        model.addAttribute("pageSubtitle", "Создание документа отгрузки");
        model.addAttribute("submitButtonText", "Сохранить документ");

        return "shipment-form";
    }

    @PostMapping("/shipments")
    public String saveShipment(StockDocument shipment,
                               RedirectAttributes redirectAttributes,
                               HttpSession session) {
        if (!currentUserService.canAccessShipments(session)) {
            return "redirect:/";
        }

        Long currentUserId = currentUserService.getCurrentUserId(session);
        if (currentUserId != null) {
            shipment.setCreatedBy(currentUserId.intValue());
        }

        Long id = stockDocumentRepository.saveShipmentAndReturnId(shipment);
        redirectAttributes.addFlashAttribute("successMessage", "Отгрузка создана. Теперь добавь товары.");
        return "redirect:/shipments/" + id + "/items";
    }

    @GetMapping("/shipments/{id}/edit")
    public String editShipment(@PathVariable Long id, Model model, HttpSession session) {
        if (!currentUserService.canAccessShipments(session)) {
            return "redirect:/";
        }

        StockDocument shipment = stockDocumentRepository.findShipmentById(id);

        if (shipment == null) {
            return "redirect:/shipments";
        }

        model.addAttribute("shipment", shipment);
        model.addAttribute("warehouses", warehouseRepository.findAllActive());
        model.addAttribute("pageTitle", "Редактирование отгрузки");
        model.addAttribute("pageSubtitle", "Изменение шапки документа отгрузки");
        model.addAttribute("submitButtonText", "Сохранить изменения");

        return "shipment-form";
    }

    @PostMapping("/shipments/{id}")
    public String updateShipment(@PathVariable Long id,
                                 StockDocument shipment,
                                 RedirectAttributes redirectAttributes,
                                 HttpSession session) {
        if (!currentUserService.canAccessShipments(session)) {
            return "redirect:/";
        }

        shipment.setId(id);
        stockDocumentRepository.updateShipment(shipment);
        redirectAttributes.addFlashAttribute("successMessage", "Отгрузка обновлена.");
        return "redirect:/shipments/" + id + "/items";
    }

    @GetMapping("/shipments/{id}/items")
    public String shipmentItems(@PathVariable Long id, Model model, HttpSession session) {
        if (!currentUserService.canAccessShipments(session)) {
            return "redirect:/";
        }

        StockDocument shipment = stockDocumentRepository.findShipmentById(id);

        if (shipment == null) {
            return "redirect:/shipments";
        }

        StockDocumentItem newItem = new StockDocumentItem();
        newItem.setDocumentId(id);

        StorageLocation defaultLocation =
                storageLocationRepository.findFirstActiveByWarehouseId(shipment.getWarehouseId());

        var items = stockDocumentItemRepository.findByDocumentId(id);

        BigDecimal documentTotal = BigDecimal.ZERO;
        for (StockDocumentItem item : items) {
            documentTotal = documentTotal.add(item.getLineTotal());
        }

        model.addAttribute("shipment", shipment);
        model.addAttribute("items", items);
        model.addAttribute("documentTotal", documentTotal);
        model.addAttribute("newItem", newItem);
        model.addAttribute("products", productRepository.findAll());
        model.addAttribute("defaultLocation", defaultLocation);

        return "shipment-items";
    }

    @PostMapping("/shipments/{id}/items")
    public String saveShipmentItem(@PathVariable Long id,
                                   StockDocumentItem newItem,
                                   RedirectAttributes redirectAttributes,
                                   HttpSession session) {
        if (!currentUserService.canAccessShipments(session)) {
            return "redirect:/";
        }

        StockDocument shipment = stockDocumentRepository.findShipmentById(id);

        if (shipment == null) {
            return "redirect:/shipments";
        }

        StorageLocation defaultLocation =
                storageLocationRepository.findFirstActiveByWarehouseId(shipment.getWarehouseId());

        if (defaultLocation == null) {
            throw new IllegalStateException("Для выбранного склада нет активной локации хранения.");
        }

        Product product = productRepository.findById(newItem.getProductId());
        if (product == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Товар не найден.");
            return "redirect:/shipments/" + id + "/items";
        }

        BigDecimal available = product.getTotalQuantity() != null
                ? product.getTotalQuantity()
                : BigDecimal.ZERO;

        BigDecimal requested = newItem.getQuantity() != null
                ? newItem.getQuantity()
                : BigDecimal.ZERO;

        if (requested.compareTo(BigDecimal.ZERO) <= 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "Количество должно быть больше нуля.");
            return "redirect:/shipments/" + id + "/items";
        }

        if (available.compareTo(requested) < 0) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Недостаточно товара на складе. Доступно: " + available.toPlainString()
            );
            return "redirect:/shipments/" + id + "/items";
        }

        newItem.setDocumentId(id);
        newItem.setSourceLocationId(defaultLocation.getId());

        stockDocumentItemRepository.saveOutgoing(newItem);
        redirectAttributes.addFlashAttribute("successMessage", "Позиция отгрузки добавлена.");
        return "redirect:/shipments/" + id + "/items";
    }

    @PostMapping("/shipments/{shipmentId}/items/{itemId}/delete")
    public String deleteShipmentItem(@PathVariable Long shipmentId,
                                     @PathVariable Long itemId,
                                     RedirectAttributes redirectAttributes,
                                     HttpSession session) {
        if (!currentUserService.canAccessShipments(session)) {
            return "redirect:/";
        }

        stockDocumentItemRepository.deleteByIdAndDocumentId(itemId, shipmentId);
        redirectAttributes.addFlashAttribute("successMessage", "Позиция удалена.");
        return "redirect:/shipments/" + shipmentId + "/items";
    }

    @PostMapping("/shipments/{id}/delete")
    @Transactional
    public String deleteShipment(@PathVariable Long id,
                                 RedirectAttributes redirectAttributes,
                                 HttpSession session) {
        if (!currentUserService.canAccessShipments(session)) {
            return "redirect:/";
        }

        stockDocumentItemRepository.deleteByDocumentId(id);
        stockDocumentRepository.deleteShipmentById(id);
        redirectAttributes.addFlashAttribute("successMessage", "Отгрузка удалена.");
        return "redirect:/shipments";
    }
}