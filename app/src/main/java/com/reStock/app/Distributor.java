package com.reStock.app;

public class Distributor {
    private String name;
    private String email;
    private String profile;

    public Distributor(){

    }

    public Distributor(String name, String email, String profile)
    {
        this.name = name;
        this.email = email;
        this.profile = profile;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
