package dev.senna.model.enums;

public enum UserRole {

    OFFICER("External Officer"),
    ADMIN("Administrator"),;

    private String roleName;

    UserRole(String roleName) {
        this.roleName = roleName;
    }

    public String getRoleName() {
        return roleName;
    }
}
