/*
 * This file is generated by jOOQ.
 */
package io.xj.hub.tables.pojos;


import io.xj.hub.tables.interfaces.IProgramSequencePatternEvent;

import java.util.UUID;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class ProgramSequencePatternEvent implements IProgramSequencePatternEvent {

    private static final long serialVersionUID = 1L;

    private UUID   id;
    private UUID   programId;
    private UUID   programSequencePatternId;
    private UUID   programVoiceTrackId;
    private Float  velocity;
    private Float  position;
    private Float  duration;
    private String tones;

    public ProgramSequencePatternEvent() {}

    public ProgramSequencePatternEvent(IProgramSequencePatternEvent value) {
        this.id = value.getId();
        this.programId = value.getProgramId();
        this.programSequencePatternId = value.getProgramSequencePatternId();
        this.programVoiceTrackId = value.getProgramVoiceTrackId();
        this.velocity = value.getVelocity();
        this.position = value.getPosition();
        this.duration = value.getDuration();
        this.tones = value.getTones();
    }

    public ProgramSequencePatternEvent(
        UUID   id,
        UUID   programId,
        UUID   programSequencePatternId,
        UUID   programVoiceTrackId,
        Float  velocity,
        Float  position,
        Float  duration,
        String tones
    ) {
        this.id = id;
        this.programId = programId;
        this.programSequencePatternId = programSequencePatternId;
        this.programVoiceTrackId = programVoiceTrackId;
        this.velocity = velocity;
        this.position = position;
        this.duration = duration;
        this.tones = tones;
    }

    /**
     * Getter for <code>xj.program_sequence_pattern_event.id</code>.
     */
    @Override
    public UUID getId() {
        return this.id;
    }

    /**
     * Setter for <code>xj.program_sequence_pattern_event.id</code>.
     */
    @Override
    public void setId(UUID id) {
        this.id = id;
    }

    /**
     * Getter for <code>xj.program_sequence_pattern_event.program_id</code>.
     */
    @Override
    public UUID getProgramId() {
        return this.programId;
    }

    /**
     * Setter for <code>xj.program_sequence_pattern_event.program_id</code>.
     */
    @Override
    public void setProgramId(UUID programId) {
        this.programId = programId;
    }

    /**
     * Getter for
     * <code>xj.program_sequence_pattern_event.program_sequence_pattern_id</code>.
     */
    @Override
    public UUID getProgramSequencePatternId() {
        return this.programSequencePatternId;
    }

    /**
     * Setter for
     * <code>xj.program_sequence_pattern_event.program_sequence_pattern_id</code>.
     */
    @Override
    public void setProgramSequencePatternId(UUID programSequencePatternId) {
        this.programSequencePatternId = programSequencePatternId;
    }

    /**
     * Getter for
     * <code>xj.program_sequence_pattern_event.program_voice_track_id</code>.
     */
    @Override
    public UUID getProgramVoiceTrackId() {
        return this.programVoiceTrackId;
    }

    /**
     * Setter for
     * <code>xj.program_sequence_pattern_event.program_voice_track_id</code>.
     */
    @Override
    public void setProgramVoiceTrackId(UUID programVoiceTrackId) {
        this.programVoiceTrackId = programVoiceTrackId;
    }

    /**
     * Getter for <code>xj.program_sequence_pattern_event.velocity</code>.
     */
    @Override
    public Float getVelocity() {
        return this.velocity;
    }

    /**
     * Setter for <code>xj.program_sequence_pattern_event.velocity</code>.
     */
    @Override
    public void setVelocity(Float velocity) {
        this.velocity = velocity;
    }

    /**
     * Getter for <code>xj.program_sequence_pattern_event.position</code>.
     */
    @Override
    public Float getPosition() {
        return this.position;
    }

    /**
     * Setter for <code>xj.program_sequence_pattern_event.position</code>.
     */
    @Override
    public void setPosition(Float position) {
        this.position = position;
    }

    /**
     * Getter for <code>xj.program_sequence_pattern_event.duration</code>.
     */
    @Override
    public Float getDuration() {
        return this.duration;
    }

    /**
     * Setter for <code>xj.program_sequence_pattern_event.duration</code>.
     */
    @Override
    public void setDuration(Float duration) {
        this.duration = duration;
    }

    /**
     * Getter for <code>xj.program_sequence_pattern_event.tones</code>.
     */
    @Override
    public String getTones() {
        return this.tones;
    }

    /**
     * Setter for <code>xj.program_sequence_pattern_event.tones</code>.
     */
    @Override
    public void setTones(String tones) {
        this.tones = tones;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ProgramSequencePatternEvent (");

        sb.append(id);
        sb.append(", ").append(programId);
        sb.append(", ").append(programSequencePatternId);
        sb.append(", ").append(programVoiceTrackId);
        sb.append(", ").append(velocity);
        sb.append(", ").append(position);
        sb.append(", ").append(duration);
        sb.append(", ").append(tones);

        sb.append(")");
        return sb.toString();
    }

    // -------------------------------------------------------------------------
    // FROM and INTO
    // -------------------------------------------------------------------------

    @Override
    public void from(IProgramSequencePatternEvent from) {
        setId(from.getId());
        setProgramId(from.getProgramId());
        setProgramSequencePatternId(from.getProgramSequencePatternId());
        setProgramVoiceTrackId(from.getProgramVoiceTrackId());
        setVelocity(from.getVelocity());
        setPosition(from.getPosition());
        setDuration(from.getDuration());
        setTones(from.getTones());
    }

    @Override
    public <E extends IProgramSequencePatternEvent> E into(E into) {
        into.from(this);
        return into;
    }
}
