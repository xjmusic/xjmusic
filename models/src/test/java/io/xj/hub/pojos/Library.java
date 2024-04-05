package io.xj.hub.tables.pojos;


import java.io.Serializable;
import java.util.UUID;


@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Library implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID    id;
    private String  name;
    private UUID    projectId;
    private Boolean isDeleted;
    private Long    updatedAt;

    public Library() {}

    public Library(Library value) {
        this.id = value.id;
        this.name = value.name;
        this.projectId = value.projectId;
        this.isDeleted = value.isDeleted;
        this.updatedAt = value.updatedAt;
    }

    public Library(
        UUID    id,
        String  name,
        UUID    projectId,
        Boolean isDeleted,
        Long    updatedAt
    ) {
        this.id = id;
        this.name = name;
        this.projectId = projectId;
        this.isDeleted = isDeleted;
        this.updatedAt = updatedAt;
    }

    /**
     * Getter for <code>xj.library.id</code>.
     */
    public UUID getId() {
        return this.id;
    }

    /**
     * Setter for <code>xj.library.id</code>.
     */
    public void setId(UUID id) {
        this.id = id;
    }

    /**
     * Getter for <code>xj.library.name</code>.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Setter for <code>xj.library.name</code>.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Getter for <code>xj.library.project_id</code>.
     */
    public UUID getProjectId() {
        return this.projectId;
    }

    /**
     * Setter for <code>xj.library.project_id</code>.
     */
    public void setProjectId(UUID projectId) {
        this.projectId = projectId;
    }

    /**
     * Getter for <code>xj.library.is_deleted</code>.
     */
    public Boolean getIsDeleted() {
        return this.isDeleted;
    }

    /**
     * Setter for <code>xj.library.is_deleted</code>.
     */
    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    /**
     * Getter for <code>xj.library.updated_at</code>.
     */
    public Long getUpdatedAt() {
        return this.updatedAt;
    }

    /**
     * Setter for <code>xj.library.updated_at</code>.
     */
    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Library (");

        sb.append(id);
        sb.append(", ").append(name);
        sb.append(", ").append(projectId);
        sb.append(", ").append(isDeleted);
        sb.append(", ").append(updatedAt);

        sb.append(")");
        return sb.toString();
    }
}
