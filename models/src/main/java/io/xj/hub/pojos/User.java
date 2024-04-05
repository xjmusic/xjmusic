package io.xj.hub.pojos;


import java.io.Serializable;
import java.util.UUID;


@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID    id;
    private String  name;
    private String  email;
    private String  avatarUrl;
    private Boolean isAdmin;

    public User() {}

    public User(User value) {
        this.id = value.id;
        this.name = value.name;
        this.email = value.email;
        this.avatarUrl = value.avatarUrl;
        this.isAdmin = value.isAdmin;
    }

    public User(
        UUID    id,
        String  name,
        String  email,
        String  avatarUrl,
        Boolean isAdmin
    ) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.avatarUrl = avatarUrl;
        this.isAdmin = isAdmin;
    }

    /**
     * Getter for <code>xj.user.id</code>.
     */
    public UUID getId() {
        return this.id;
    }

    /**
     * Setter for <code>xj.user.id</code>.
     */
    public void setId(UUID id) {
        this.id = id;
    }

    /**
     * Getter for <code>xj.user.name</code>.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Setter for <code>xj.user.name</code>.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Getter for <code>xj.user.email</code>.
     */
    public String getEmail() {
        return this.email;
    }

    /**
     * Setter for <code>xj.user.email</code>.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Getter for <code>xj.user.avatar_url</code>.
     */
    public String getAvatarUrl() {
        return this.avatarUrl;
    }

    /**
     * Setter for <code>xj.user.avatar_url</code>.
     */
    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    /**
     * Getter for <code>xj.user.is_admin</code>.
     */
    public Boolean getIsAdmin() {
        return this.isAdmin;
    }

    /**
     * Setter for <code>xj.user.is_admin</code>.
     */
    public void setIsAdmin(Boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("User (");

        sb.append(id);
        sb.append(", ").append(name);
        sb.append(", ").append(email);
        sb.append(", ").append(avatarUrl);
        sb.append(", ").append(isAdmin);

        sb.append(")");
        return sb.toString();
    }
}
