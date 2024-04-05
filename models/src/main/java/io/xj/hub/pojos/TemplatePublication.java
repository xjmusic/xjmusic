package io.xj.hub.pojos;


import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;


@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class TemplatePublication implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID          id;
    private UUID          templateId;
    private UUID          userId;
    private LocalDateTime createdAt;

    public TemplatePublication() {}

    public TemplatePublication(TemplatePublication value) {
        this.id = value.id;
        this.templateId = value.templateId;
        this.userId = value.userId;
        this.createdAt = value.createdAt;
    }

    public TemplatePublication(
        UUID          id,
        UUID          templateId,
        UUID          userId,
        LocalDateTime createdAt
    ) {
        this.id = id;
        this.templateId = templateId;
        this.userId = userId;
        this.createdAt = createdAt;
    }

    /**
     * Getter for <code>xj.template_publication.id</code>.
     */
    public UUID getId() {
        return this.id;
    }

    /**
     * Setter for <code>xj.template_publication.id</code>.
     */
    public void setId(UUID id) {
        this.id = id;
    }

    /**
     * Getter for <code>xj.template_publication.template_id</code>.
     */
    public UUID getTemplateId() {
        return this.templateId;
    }

    /**
     * Setter for <code>xj.template_publication.template_id</code>.
     */
    public void setTemplateId(UUID templateId) {
        this.templateId = templateId;
    }

    /**
     * Getter for <code>xj.template_publication.user_id</code>.
     */
    public UUID getUserId() {
        return this.userId;
    }

    /**
     * Setter for <code>xj.template_publication.user_id</code>.
     */
    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    /**
     * Getter for <code>xj.template_publication.created_at</code>.
     */
    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    /**
     * Setter for <code>xj.template_publication.created_at</code>.
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("TemplatePublication (");

        sb.append(id);
        sb.append(", ").append(templateId);
        sb.append(", ").append(userId);
        sb.append(", ").append(createdAt);

        sb.append(")");
        return sb.toString();
    }
}
