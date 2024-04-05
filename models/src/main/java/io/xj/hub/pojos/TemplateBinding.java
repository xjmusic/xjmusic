package io.xj.hub.pojos;


import io.xj.hub.enums.ContentBindingType;

import java.io.Serializable;
import java.util.UUID;


@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class TemplateBinding implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID               id;
    private ContentBindingType type;
    private UUID               templateId;
    private UUID               targetId;

    public TemplateBinding() {}

    public TemplateBinding(TemplateBinding value) {
        this.id = value.id;
        this.type = value.type;
        this.templateId = value.templateId;
        this.targetId = value.targetId;
    }

    public TemplateBinding(
        UUID               id,
        ContentBindingType type,
        UUID               templateId,
        UUID               targetId
    ) {
        this.id = id;
        this.type = type;
        this.templateId = templateId;
        this.targetId = targetId;
    }

    /**
     * Getter for <code>xj.template_binding.id</code>.
     */
    public UUID getId() {
        return this.id;
    }

    /**
     * Setter for <code>xj.template_binding.id</code>.
     */
    public void setId(UUID id) {
        this.id = id;
    }

    /**
     * Getter for <code>xj.template_binding.type</code>.
     */
    public ContentBindingType getType() {
        return this.type;
    }

    /**
     * Setter for <code>xj.template_binding.type</code>.
     */
    public void setType(ContentBindingType type) {
        this.type = type;
    }

    /**
     * Getter for <code>xj.template_binding.template_id</code>.
     */
    public UUID getTemplateId() {
        return this.templateId;
    }

    /**
     * Setter for <code>xj.template_binding.template_id</code>.
     */
    public void setTemplateId(UUID templateId) {
        this.templateId = templateId;
    }

    /**
     * Getter for <code>xj.template_binding.target_id</code>.
     */
    public UUID getTargetId() {
        return this.targetId;
    }

    /**
     * Setter for <code>xj.template_binding.target_id</code>.
     */
    public void setTargetId(UUID targetId) {
        this.targetId = targetId;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("TemplateBinding (");

        sb.append(id);
        sb.append(", ").append(type);
        sb.append(", ").append(templateId);
        sb.append(", ").append(targetId);

        sb.append(")");
        return sb.toString();
    }
}
