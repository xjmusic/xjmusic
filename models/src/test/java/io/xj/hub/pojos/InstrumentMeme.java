package io.xj.hub.tables.pojos;


import java.io.Serializable;
import java.util.UUID;


@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class InstrumentMeme implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID   id;
    private UUID   instrumentId;
    private String name;

    public InstrumentMeme() {}

    public InstrumentMeme(InstrumentMeme value) {
        this.id = value.id;
        this.instrumentId = value.instrumentId;
        this.name = value.name;
    }

    public InstrumentMeme(
        UUID   id,
        UUID   instrumentId,
        String name
    ) {
        this.id = id;
        this.instrumentId = instrumentId;
        this.name = name;
    }

    /**
     * Getter for <code>xj.instrument_meme.id</code>.
     */
    public UUID getId() {
        return this.id;
    }

    /**
     * Setter for <code>xj.instrument_meme.id</code>.
     */
    public void setId(UUID id) {
        this.id = id;
    }

    /**
     * Getter for <code>xj.instrument_meme.instrument_id</code>.
     */
    public UUID getInstrumentId() {
        return this.instrumentId;
    }

    /**
     * Setter for <code>xj.instrument_meme.instrument_id</code>.
     */
    public void setInstrumentId(UUID instrumentId) {
        this.instrumentId = instrumentId;
    }

    /**
     * Getter for <code>xj.instrument_meme.name</code>.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Setter for <code>xj.instrument_meme.name</code>.
     */
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("InstrumentMeme (");

        sb.append(id);
        sb.append(", ").append(instrumentId);
        sb.append(", ").append(name);

        sb.append(")");
        return sb.toString();
    }
}
