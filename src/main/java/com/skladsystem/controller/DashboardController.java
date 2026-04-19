package com.skladsystem.controller;

import com.skladsystem.repository.StockDocumentRepository;
import com.skladsystem.service.ProductService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Locale;

@Controller
public class DashboardController {

    private static final Locale RU_LOCALE = new Locale("ru", "RU");

    private final ProductService productService;
    private final StockDocumentRepository stockDocumentRepository;

    public DashboardController(ProductService productService,
                               StockDocumentRepository stockDocumentRepository) {
        this.productService = productService;
        this.stockDocumentRepository = stockDocumentRepository;
    }

    @GetMapping("/")
    public String dashboard(
            @RequestParam(value = "dateFrom", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(value = "dateTo", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            Model model) {

        BigDecimal totalQuantity = productService.sumQuantity();
        BigDecimal totalInventoryValue = productService.totalInventoryValue();

        long receiptsCount = stockDocumentRepository.countReceiptsByPeriod(dateFrom, dateTo);
        long shipmentsCount = stockDocumentRepository.countShipmentsByPeriod(dateFrom, dateTo);

        BigDecimal receiptsAmount = stockDocumentRepository.sumReceiptsAmountByPeriod(dateFrom, dateTo);
        BigDecimal shipmentsAmount = stockDocumentRepository.sumShipmentsAmountByPeriod(dateFrom, dateTo);

        model.addAttribute("productsCount", productService.countProducts());
        model.addAttribute("lowStockCount", productService.countLowStock(0));
        model.addAttribute("latestProducts", productService.findLatestFive());

        model.addAttribute("totalQuantityFormatted", formatQuantity(totalQuantity));
        model.addAttribute("totalInventoryValueFormatted", formatMoney(totalInventoryValue));

        model.addAttribute("receiptsCount", receiptsCount);
        model.addAttribute("shipmentsCount", shipmentsCount);
        model.addAttribute("receiptsAmountFormatted", formatMoney(receiptsAmount));
        model.addAttribute("shipmentsAmountFormatted", formatMoney(shipmentsAmount));

        model.addAttribute("dateFrom", dateFrom);
        model.addAttribute("dateTo", dateTo);

        return "index";
    }

    private String formatQuantity(BigDecimal value) {
        BigDecimal safeValue = value != null ? value : BigDecimal.ZERO;

        NumberFormat format = NumberFormat.getNumberInstance(RU_LOCALE);
        format.setMinimumFractionDigits(0);
        format.setMaximumFractionDigits(0);

        return format.format(safeValue);
    }

    private String formatMoney(BigDecimal value) {
        BigDecimal safeValue = value != null ? value : BigDecimal.ZERO;

        NumberFormat format = NumberFormat.getNumberInstance(RU_LOCALE);
        format.setMinimumFractionDigits(2);
        format.setMaximumFractionDigits(2);

        return format.format(safeValue) + " ₽";
    }
}