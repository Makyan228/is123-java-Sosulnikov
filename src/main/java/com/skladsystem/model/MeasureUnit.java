package com.skladsystem.model;

public class MeasureUnit {

    private Long id;
    private String name;
    private String shortName;

    public MeasureUnit() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getDisplayLabelRu() {
        if (shortName == null || shortName.isBlank()) {
            return name;
        }

        return switch (shortName.toLowerCase()) {
            case "pcs" -> "шт — Штука";
            case "kg" -> "кг — Килограмм";
            case "l" -> "л — Литр";
            default -> shortName + " — " + name;
        };
    }
}