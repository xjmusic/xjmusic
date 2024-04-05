package io.xj.hub.tables.pojos;


import java.io.Serializable;
import java.util.UUID;


@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class ProgramSequencePatternEvent implements Serializable {

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

    public ProgramSequencePatternEvent(ProgramSequencePatternEvent value) {
        this.id = value.id;
        this.programId = value.programId;
        this.programSequencePatternId = value.programSequencePatternId;
        this.programVoiceTrackId = value.programVoiceTrackId;
        this.velocity = value.velocity;
        this.position = value.position;
        this.duration = value.duration;
        this.tones = value.tones;
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
    public UUID getId() {
        return this.id;
    }

    /**
     * Setter for <code>xj.program_sequence_pattern_event.id</code>.
     */
    public void setId(UUID id) {
        this.id = id;
    }

    /**
     * Getter for <code>xj.program_sequence_pattern_event.program_id</code>.
     */
    public UUID getProgramId() {
        return this.programId;
    }

    /**
     * Setter for <code>xj.program_sequence_pattern_event.program_id</code>.
     */
    public void setProgramId(UUID programId) {
        this.programId = programId;
    }

    /**
     * Getter for
     * <code>xj.program_sequence_pattern_event.program_sequence_pattern_id</code>.
     */
    public UUID getProgramSequencePatternId() {
        return this.programSequencePatternId;
    }

    /**
     * Setter for
     * <code>xj.program_sequence_pattern_event.program_sequence_pattern_id</code>.
     */
    public void setProgramSequencePatternId(UUID programSequencePatternId) {
        this.programSequencePatternId = programSequencePatternId;
    }

    /**
     * Getter for
     * <code>xj.program_sequence_pattern_event.program_voice_track_id</code>.
     */
    public UUID getProgramVoiceTrackId() {
        return this.programVoiceTrackId;
    }

    /**
     * Setter for
     * <code>xj.program_sequence_pattern_event.program_voice_track_id</code>.
     */
    public void setProgramVoiceTrackId(UUID programVoiceTrackId) {
        this.programVoiceTrackId = programVoiceTrackId;
    }

    /**
     * Getter for <code>xj.program_sequence_pattern_event.velocity</code>.
     */
    public Float getVelocity() {
        return this.velocity;
    }

    /**
     * Setter for <code>xj.program_sequence_pattern_event.velocity</code>.
     */
    public void setVelocity(Float velocity) {
        this.velocity = velocity;
    }

    /**
     * Getter for <code>xj.program_sequence_pattern_event.position</code>.
     */
    public Float getPosition() {
        return this.position;
    }

    /**
     * Setter for <code>xj.program_sequence_pattern_event.position</code>.
     */
    public void setPosition(Float position) {
        this.position = position;
    }

    /**
     * Getter for <code>xj.program_sequence_pattern_event.duration</code>.
     */
    public Float getDuration() {
        return this.duration;
    }

    /**
     * Setter for <code>xj.program_sequence_pattern_event.duration</code>.
     */
    public void setDuration(Float duration) {
        this.duration = duration;
    }

    /**
     * Getter for <code>xj.program_sequence_pattern_event.tones</code>.
     */
    public String getTones() {
        return this.tones;
    }

    /**
     * Setter for <code>xj.program_sequence_pattern_event.tones</code>.
     */
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
}
