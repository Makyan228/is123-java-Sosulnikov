package com.skladsystem.controller;

import com.skladsystem.model.Counterparty;
import com.skladsystem.service.CounterpartyService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SupplierController {

    private final CounterpartyService counterpartyService;

    public SupplierController(CounterpartyService counterpartyService) {
        this.counterpartyService = counterpartyService;
    }

    @GetMapping("/suppliers")
    public String suppliers(@RequestParam(value = "search", required = false) String search,
                            Model model) {
        model.addAttribute("suppliers", counterpartyService.searchSuppliers(search));
        model.addAttribute("search", search);
        return "suppliers";
    }

    @GetMapping("/suppliers/new")
    public String newSupplier(Model model) {
        Counterparty supplier = new Counterparty();
        supplier.setActive(true);

        model.addAttribute("supplier", supplier);
        model.addAttribute("pageTitle", "Новый поставщик");
        model.addAttribute("pageSubtitle", "Добавление поставщика в таблицу COUNTERPARTY");
        model.addAttribute("submitButtonText", "Сохранить поставщика");

        return "supplier-form";
    }

    @PostMapping("/suppliers")
    public String saveSupplier(Counterparty supplier) {
        counterpartyService.saveSupplier(supplier);
        return "redirect:/suppliers";
    }

    @GetMapping("/suppliers/{id}/edit")
    public String editSupplier(@PathVariable Long id, Model model) {
        Counterparty supplier = counterpartyService.findSupplierById(id);

        if (supplier == null) {
            return "redirect:/suppliers";
        }

        model.addAttribute("supplier", supplier);
        model.addAttribute("pageTitle", "Редактирование поставщика");
        model.addAttribute("pageSubtitle", "Изменение данных поставщика");
        model.addAttribute("submitButtonText", "Сохранить изменения");

        return "supplier-form";
    }

    @PostMapping("/suppliers/{id}")
    public String updateSupplier(@PathVariable Long id, Counterparty supplier) {
        supplier.setId(id);
        counterpartyService.updateSupplier(supplier);
        return "redirect:/suppliers";
    }

    @PostMapping("/suppliers/{id}/delete")
    public String deleteSupplier(@PathVariable Long id) {
        counterpartyService.softDeleteSupplier(id);
        return "redirect:/suppliers";
    }
}