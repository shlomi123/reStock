package com.reStock.app;

public class Store {
    private String _name;
    private String _email;

    public Store()
    {

    }

    Store(String name, String email)
    {
        _name = name;
        _email = email;
    }


    public String get_name() {
        return _name;
    }

    public String get_email() {
        return _email;
    }


}
