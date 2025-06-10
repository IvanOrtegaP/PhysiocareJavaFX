package com.example.physiocare.models.user;

import com.google.gson.annotations.SerializedName;

public class User {
    @SerializedName("_id")
    private String id;
    private String login;
    private String password;
    private String email;
    private String rol;
    private String avatar;

    public User() {
    }

    public User(String id, String login, String password, String email, String rol) {
        this.id = id;
        this.login = login;
        this.password = password;
        this.email = email;
        this.rol = rol;
    }

    //    public User(String login, String password, String email, String rol, String avatar) {
//        this.login = login;
//        this.password = password;
//        this.email = email;
//        this.rol = rol;
//        this.avatar = avatar;
//    }


    public User(String id, String login,String email, String rol) {
        this.id = id;
        this.login = login;
        this.email = email;
        this.rol = rol;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
