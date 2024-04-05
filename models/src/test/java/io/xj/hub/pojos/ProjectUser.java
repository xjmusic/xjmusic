package io.xj.hub.tables.pojos;


import io.xj.hub.enums.ProjectUserRole;

import java.io.Serializable;
import java.util.UUID;


@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class ProjectUser implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID            id;
    private UUID            userId;
    private UUID            projectId;
    private ProjectUserRole role;

    public ProjectUser() {}

    public ProjectUser(ProjectUser value) {
        this.id = value.id;
        this.userId = value.userId;
        this.projectId = value.projectId;
        this.role = value.role;
    }

    public ProjectUser(
        UUID            id,
        UUID            userId,
        UUID            projectId,
        ProjectUserRole role
    ) {
        this.id = id;
        this.userId = userId;
        this.projectId = projectId;
        this.role = role;
    }

    /**
     * Getter for <code>xj.project_user.id</code>.
     */
    public UUID getId() {
        return this.id;
    }

    /**
     * Setter for <code>xj.project_user.id</code>.
     */
    public void setId(UUID id) {
        this.id = id;
    }

    /**
     * Getter for <code>xj.project_user.user_id</code>.
     */
    public UUID getUserId() {
        return this.userId;
    }

    /**
     * Setter for <code>xj.project_user.user_id</code>.
     */
    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    /**
     * Getter for <code>xj.project_user.project_id</code>.
     */
    public UUID getProjectId() {
        return this.projectId;
    }

    /**
     * Setter for <code>xj.project_user.project_id</code>.
     */
    public void setProjectId(UUID projectId) {
        this.projectId = projectId;
    }

    /**
     * Getter for <code>xj.project_user.role</code>.
     */
    public ProjectUserRole getRole() {
        return this.role;
    }

    /**
     * Setter for <code>xj.project_user.role</code>.
     */
    public void setRole(ProjectUserRole role) {
        this.role = role;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ProjectUser (");

        sb.append(id);
        sb.append(", ").append(userId);
        sb.append(", ").append(projectId);
        sb.append(", ").append(role);

        sb.append(")");
        return sb.toString();
    }
}
