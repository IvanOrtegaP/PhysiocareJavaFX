package com.example.physiocare.models.user;

public class User {
    private String login;
    private String password;
    private String email;
    private String rol;
    private String lat;
    private String avatar;

    public User() {
    }

    public User(String login, String password, String email, String rol, String lat, String avatar) {
        this.login = login;
        this.password = password;
        this.email = email;
        this.rol = rol;
        this.lat = lat;
        this.avatar = avatar;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
