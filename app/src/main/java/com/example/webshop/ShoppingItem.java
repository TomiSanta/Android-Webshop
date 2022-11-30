package com.example.webshop;

public class ShoppingItem {
    private String id;
    private String name;
    private String info;
    private String price;
    private float rating;
    private int imageResources;
    private int cartedCount;

    public ShoppingItem() {}

    public ShoppingItem(String name, String info, String price, float rating, int imageResources, int cartedCount) {
        this.name = name;
        this.info = info;
        this.price = price;
        this.rating = rating;
        this.imageResources = imageResources;
        this.cartedCount = cartedCount;
    }

    public String getName() {
        return name;
    }

    public String getInfo() {
        return info;
    }

    public String getPrice() {
        return price;
    }

    public float getRating() {
        return rating;
    }

    public int getImageResources() {
        return imageResources;
    }

    public int getCartedCount() {
        return cartedCount;
    }

    public String _getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}