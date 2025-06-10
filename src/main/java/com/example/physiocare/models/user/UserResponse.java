package com.example.physiocare.models.user;

import com.example.physiocare.models.BaseResponse;

public class UserResponse extends BaseResponse {
    private User user;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}