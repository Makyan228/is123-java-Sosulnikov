package com.skladsystem.model;

public class AppUser {

    private Long id;
    private String username;
    private String fullName;
    private String roleName;
    private Boolean active;

    public AppUser() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getDisplayName() {
        if (fullName != null && !fullName.isBlank()) {
            return fullName;
        }
        return username != null ? username : "Пользователь";
    }

    public String getRoleLabelRu() {
        if (roleName == null || roleName.isBlank()) {
            return "Неизвестная роль";
        }

        return switch (roleName.toUpperCase()) {
            case "ADMIN" -> "Администратор";
            case "RECEIPT_OPERATOR" -> "Оператор поступлений";
            case "SHIPMENT_OPERATOR" -> "Оператор отгрузок";
            case "LOADER" -> "Грузчик";
            default -> roleName;
        };
    }
}