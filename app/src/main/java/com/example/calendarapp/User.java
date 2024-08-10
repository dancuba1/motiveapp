package com.example.calendarapp;

import androidx.annotation.NonNull;

public class User {
    private String username, description, uid, profileImage, email, bannerImage;
    public User(String username, String uid, String email, String description, String profileImage, String bannerImage){
        this.email = email;
        this.username = username;
        this.uid = uid;
        this.description = description;
        this.profileImage = profileImage;
        this.bannerImage = bannerImage;
    }
    public User(){};

    @NonNull
    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", description='" + description + '\'' +
                ", uid='" + uid + '\'' +
                ", profileImage='" + profileImage + '\'' +
                ", email='" + email + '\'' +
                ", bannerImage='" + bannerImage + '\'' +
                '}';
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBannerImage() {
        return bannerImage;
    }

    public void setBannerImage(String bannerImage) {
        this.bannerImage = bannerImage;
    }
}



