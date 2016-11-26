package io.outright.xj.core.service;

import io.outright.xj.core.util.Purify;

public class HelloService implements Service {

    @Override
    public String hello(String raw) {
        String name = Purify.Properslug(raw, "moshimosh");
        return "Hello, " + name +"!";
    }
}
