package com.example.physiocare.models.user;

import com.example.physiocare.models.BaseResponse;

public class UserResponse extends BaseResponse {
    private User result;

    public User getUser() {
        return result;
    }

    public void setUser(User user) {
        this.result = user;
    }
}
