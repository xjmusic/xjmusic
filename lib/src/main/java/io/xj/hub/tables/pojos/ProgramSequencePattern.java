/*
 * This file is generated by jOOQ.
 */
package io.xj.hub.tables.pojos;


import io.xj.hub.tables.interfaces.IProgramSequencePattern;

import java.util.UUID;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class ProgramSequencePattern implements IProgramSequencePattern {

    private static final long serialVersionUID = 1L;

    private UUID   id;
    private UUID   programId;
    private UUID   programSequenceId;
    private UUID   programVoiceId;
    private String name;
    private Short  total;

    public ProgramSequencePattern() {}

    public ProgramSequencePattern(IProgramSequencePattern value) {
        this.id = value.getId();
        this.programId = value.getProgramId();
        this.programSequenceId = value.getProgramSequenceId();
        this.programVoiceId = value.getProgramVoiceId();
        this.name = value.getName();
        this.total = value.getTotal();
    }

    public ProgramSequencePattern(
        UUID   id,
        UUID   programId,
        UUID   programSequenceId,
        UUID   programVoiceId,
        String name,
        Short  total
    ) {
        this.id = id;
        this.programId = programId;
        this.programSequenceId = programSequenceId;
        this.programVoiceId = programVoiceId;
        this.name = name;
        this.total = total;
    }

    /**
     * Getter for <code>xj.program_sequence_pattern.id</code>.
     */
    @Override
    public UUID getId() {
        return this.id;
    }

    /**
     * Setter for <code>xj.program_sequence_pattern.id</code>.
     */
    @Override
    public void setId(UUID id) {
        this.id = id;
    }

    /**
     * Getter for <code>xj.program_sequence_pattern.program_id</code>.
     */
    @Override
    public UUID getProgramId() {
        return this.programId;
    }

    /**
     * Setter for <code>xj.program_sequence_pattern.program_id</code>.
     */
    @Override
    public void setProgramId(UUID programId) {
        this.programId = programId;
    }

    /**
     * Getter for <code>xj.program_sequence_pattern.program_sequence_id</code>.
     */
    @Override
    public UUID getProgramSequenceId() {
        return this.programSequenceId;
    }

    /**
     * Setter for <code>xj.program_sequence_pattern.program_sequence_id</code>.
     */
    @Override
    public void setProgramSequenceId(UUID programSequenceId) {
        this.programSequenceId = programSequenceId;
    }

    /**
     * Getter for <code>xj.program_sequence_pattern.program_voice_id</code>.
     */
    @Override
    public UUID getProgramVoiceId() {
        return this.programVoiceId;
    }

    /**
     * Setter for <code>xj.program_sequence_pattern.program_voice_id</code>.
     */
    @Override
    public void setProgramVoiceId(UUID programVoiceId) {
        this.programVoiceId = programVoiceId;
    }

    /**
     * Getter for <code>xj.program_sequence_pattern.name</code>.
     */
    @Override
    public String getName() {
        return this.name;
    }

    /**
     * Setter for <code>xj.program_sequence_pattern.name</code>.
     */
    @Override
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Getter for <code>xj.program_sequence_pattern.total</code>.
     */
    @Override
    public Short getTotal() {
        return this.total;
    }

    /**
     * Setter for <code>xj.program_sequence_pattern.total</code>.
     */
    @Override
    public void setTotal(Short total) {
        this.total = total;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ProgramSequencePattern (");

        sb.append(id);
        sb.append(", ").append(programId);
        sb.append(", ").append(programSequenceId);
        sb.append(", ").append(programVoiceId);
        sb.append(", ").append(name);
        sb.append(", ").append(total);

        sb.append(")");
        return sb.toString();
    }

    // -------------------------------------------------------------------------
    // FROM and INTO
    // -------------------------------------------------------------------------

    @Override
    public void from(IProgramSequencePattern from) {
        setId(from.getId());
        setProgramId(from.getProgramId());
        setProgramSequenceId(from.getProgramSequenceId());
        setProgramVoiceId(from.getProgramVoiceId());
        setName(from.getName());
        setTotal(from.getTotal());
    }

    @Override
    public <E extends IProgramSequencePattern> E into(E into) {
        into.from(this);
        return into;
    }
}