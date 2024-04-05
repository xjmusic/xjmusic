package io.xj.hub.enums;


import io.xj.hub.Xj;

@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public enum ProjectUserRole implements EnumType {

    Viewer("Viewer"),

    Editor("Editor"),

    Owner("Owner");

    private final String literal;

    private ProjectUserRole(String literal) {
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
        return "project_user_role";
    }

    @Override
    public String getLiteral() {
        return literal;
    }
}
