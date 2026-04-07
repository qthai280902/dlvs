package com.dlvs.model;

public class UserSession {

    private static UserSession instance;

    private String username;
    private String fullName;
    private String role;

    private UserSession(String username, String fullName, String role) {
        this.username = username;
        this.fullName = fullName;
        this.role = role;
    }

    public static UserSession getInstance() {
        return instance;
    }

    public static void setInstance(String username, String fullName, String role) {
        instance = new UserSession(username, fullName, role);
    }

    public static void cleanUserSession() {
        instance = null;
    }

    public String getUsername() {
        return username;
    }

    public String getFullName() {
        return fullName;
    }

    public String getRole() {
        return role;
    }
}
