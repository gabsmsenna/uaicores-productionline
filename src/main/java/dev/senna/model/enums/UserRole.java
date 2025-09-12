package dev.senna.model.enums;

public enum UserRole {

    OFFICER("External Officer"),
    ADMIN("Administrator"),
    DEV("Developer");

    private String roleName;

    UserRole(String roleName) {
        this.roleName = roleName;
    }

    public String getRoleName() {
        return roleName;
    }
}
