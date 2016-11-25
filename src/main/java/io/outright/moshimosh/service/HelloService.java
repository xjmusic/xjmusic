package io.outright.moshimosh.service;

import io.outright.moshimosh.util.Purify;

public class HelloService implements Service {

    @Override
    public String hello(String raw) {
        String name = Purify.Properslug(raw, "moshimosh");
        return "Hello, " + name +"!";
    }
}
