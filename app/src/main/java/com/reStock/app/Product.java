package com.reStock.app;

public class Product {
    private String name;
    private String imageUrl;
    private Double cost;
    private int units_per_package;

    public Product() {
        //empty constructor needed
    }

    public Product(String name, String imageUrl, Double cost, int units_per_package) {
        this.name = name;
        this.imageUrl = imageUrl;
        this.cost = cost;
        this.units_per_package = units_per_package;
    }

     public Double getCost() {
         return cost;
     }

     public void setCost(Double cost) {
         this.cost = cost;
     }

     public int getUnits_per_package() {
         return units_per_package;
     }

     public void setUnits_per_package(int units_per_package) {
         this.units_per_package = units_per_package;
     }

     public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
