package org.example.greeter.impl;

import org.example.greeter.api.Greeter;

public class GreeterImpl implements Greeter {
    @Override
    public String getGreeting(String name) {
        return "Hello, " + name + "!";
    }
}
