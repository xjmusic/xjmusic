/*
 * This file is generated by jOOQ.
 */
package io.xj.hub.enums;


import io.xj.hub.Xj;
import org.jooq.Catalog;
import org.jooq.EnumType;
import org.jooq.Schema;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public enum TemplateType implements EnumType {

    Preview("Preview"),

    Production("Production");

    private final String literal;

    private TemplateType(String literal) {
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
        return "template_type";
    }

    @Override
    public String getLiteral() {
        return literal;
    }
}