package org.example.greeter.api;

import java.util.Optional;
import java.util.ServiceLoader;

public interface Greeter {
    String getGreeting(String name);

    static Optional<Greeter> getInstance() {
        return ServiceLoader.load(Greeter.class).findFirst();
    }
}
