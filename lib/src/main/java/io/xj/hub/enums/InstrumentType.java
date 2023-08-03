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
public enum InstrumentType implements EnumType {

    Bass("Bass"),

    Drum("Drum"),

    Hook("Hook"),

    Noise("Noise"),

    Pad("Pad"),

    Percussion("Percussion"),

    Stab("Stab"),

    Sticky("Sticky"),

    Stripe("Stripe"),

    Sweep("Sweep");

    private final String literal;

    private InstrumentType(String literal) {
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
        return "instrument_type";
    }

    @Override
    public String getLiteral() {
        return literal;
    }
}
