package com.reStock.app;

import java.util.Date;

public class Order {
    private Date _date;
    private String _product;
    private int _quantity;
    private String _distributor;
    private String _url;
    private double _total_cost;
    private String _store_email;
    private String _store_name;

    public Order(){}

    Order(Date date, String product, int quantity, String distributor, String url, double total_cost, String store_email, String store_name)
    {
        _date = date;
        _product = product;
        _quantity = quantity;
        _distributor = distributor;
        _url = url;
        _total_cost = total_cost;
        _store_email = store_email;
        _store_name = store_name;
    }

    public Date get_date() {
        return _date;
    }

    public int get_quantity() {
        return _quantity;
    }

    public String get_product() {
        return _product;
    }

    public String get_distributor() {
        return _distributor;
    }

    public String get_url() {
        return _url;
    }

    public double get_total_cost() {
        return _total_cost;
    }

    public String get_store_email() {
        return _store_email;
    }

    public void set_store_email(String _store_email) {
        this._store_email = _store_email;
    }

    public String get_store_name() {
        return _store_name;
    }

    public void set_store_name(String _store_name) {
        this._store_name = _store_name;
    }
}
