package io.xj.hub.enums;


import io.xj.hub.Xj;

@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public enum UserAuthType implements EnumType {

    Google("Google");

    private final String literal;

    private UserAuthType(String literal) {
        this.literal = literal;
    }

    @Override
    public Catalog getCatalog() {
        return getSchema().getCatalog();
    }

    @Override
    public Schema getSchema() {
        return Xj.XJ;
    }

    @Override
    public String getName() {
        return "user_auth_type";
    }

    @Override
    public String getLiteral() {
        return literal;
    }
}
