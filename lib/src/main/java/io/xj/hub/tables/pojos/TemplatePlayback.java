/*
 * This file is generated by jOOQ.
 */
package io.xj.hub.tables.pojos;


import io.xj.hub.tables.interfaces.ITemplatePlayback;

import java.time.LocalDateTime;
import java.util.UUID;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class TemplatePlayback implements ITemplatePlayback {

    private static final long serialVersionUID = 1L;

    private UUID          id;
    private UUID          templateId;
    private UUID          userId;
    private LocalDateTime createdAt;

    public TemplatePlayback() {}

    public TemplatePlayback(ITemplatePlayback value) {
        this.id = value.getId();
        this.templateId = value.getTemplateId();
        this.userId = value.getUserId();
        this.createdAt = value.getCreatedAt();
    }

    public TemplatePlayback(
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
     * Getter for <code>xj.template_playback.id</code>.
     */
    @Override
    public UUID getId() {
        return this.id;
    }

    /**
     * Setter for <code>xj.template_playback.id</code>.
     */
    @Override
    public void setId(UUID id) {
        this.id = id;
    }

    /**
     * Getter for <code>xj.template_playback.template_id</code>.
     */
    @Override
    public UUID getTemplateId() {
        return this.templateId;
    }

    /**
     * Setter for <code>xj.template_playback.template_id</code>.
     */
    @Override
    public void setTemplateId(UUID templateId) {
        this.templateId = templateId;
    }

    /**
     * Getter for <code>xj.template_playback.user_id</code>.
     */
    @Override
    public UUID getUserId() {
        return this.userId;
    }

    /**
     * Setter for <code>xj.template_playback.user_id</code>.
     */
    @Override
    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    /**
     * Getter for <code>xj.template_playback.created_at</code>.
     */
    @Override
    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    /**
     * Setter for <code>xj.template_playback.created_at</code>.
     */
    @Override
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("TemplatePlayback (");

        sb.append(id);
        sb.append(", ").append(templateId);
        sb.append(", ").append(userId);
        sb.append(", ").append(createdAt);

        sb.append(")");
        return sb.toString();
    }

    // -------------------------------------------------------------------------
    // FROM and INTO
    // -------------------------------------------------------------------------

    @Override
    public void from(ITemplatePlayback from) {
        setId(from.getId());
        setTemplateId(from.getTemplateId());
        setUserId(from.getUserId());
        setCreatedAt(from.getCreatedAt());
    }

    @Override
    public <E extends ITemplatePlayback> E into(E into) {
        into.from(this);
        return into;
    }
}