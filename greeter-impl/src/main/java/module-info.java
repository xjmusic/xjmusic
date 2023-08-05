module org.example.greeter.impl {
    requires transitive org.example.greeter.api;
    provides org.example.greeter.api.Greeter with org.example.greeter.impl.GreeterImpl;
}
