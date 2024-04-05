package io.xj.hub.tables.pojos;


import java.io.Serializable;
import java.util.UUID;


@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class ProgramSequenceChord implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID   id;
    private UUID   programId;
    private UUID   programSequenceId;
    private String name;
    private Double position;

    public ProgramSequenceChord() {}

    public ProgramSequenceChord(ProgramSequenceChord value) {
        this.id = value.id;
        this.programId = value.programId;
        this.programSequenceId = value.programSequenceId;
        this.name = value.name;
        this.position = value.position;
    }

    public ProgramSequenceChord(
        UUID   id,
        UUID   programId,
        UUID   programSequenceId,
        String name,
        Double position
    ) {
        this.id = id;
        this.programId = programId;
        this.programSequenceId = programSequenceId;
        this.name = name;
        this.position = position;
    }

    /**
     * Getter for <code>xj.program_sequence_chord.id</code>.
     */
    public UUID getId() {
        return this.id;
    }

    /**
     * Setter for <code>xj.program_sequence_chord.id</code>.
     */
    public void setId(UUID id) {
        this.id = id;
    }

    /**
     * Getter for <code>xj.program_sequence_chord.program_id</code>.
     */
    public UUID getProgramId() {
        return this.programId;
    }

    /**
     * Setter for <code>xj.program_sequence_chord.program_id</code>.
     */
    public void setProgramId(UUID programId) {
        this.programId = programId;
    }

    /**
     * Getter for <code>xj.program_sequence_chord.program_sequence_id</code>.
     */
    public UUID getProgramSequenceId() {
        return this.programSequenceId;
    }

    /**
     * Setter for <code>xj.program_sequence_chord.program_sequence_id</code>.
     */
    public void setProgramSequenceId(UUID programSequenceId) {
        this.programSequenceId = programSequenceId;
    }

    /**
     * Getter for <code>xj.program_sequence_chord.name</code>.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Setter for <code>xj.program_sequence_chord.name</code>.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Getter for <code>xj.program_sequence_chord.position</code>.
     */
    public Double getPosition() {
        return this.position;
    }

    /**
     * Setter for <code>xj.program_sequence_chord.position</code>.
     */
    public void setPosition(Double position) {
        this.position = position;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ProgramSequenceChord (");

        sb.append(id);
        sb.append(", ").append(programId);
        sb.append(", ").append(programSequenceId);
        sb.append(", ").append(name);
        sb.append(", ").append(position);

        sb.append(")");
        return sb.toString();
    }
}
