/*
 * This file is generated by jOOQ.
 */
package io.xj.hub.tables.interfaces;


import java.io.Serializable;
import java.util.UUID;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public interface IUserAuthToken extends Serializable {

    /**
     * Setter for <code>xj.user_auth_token.id</code>.
     */
    public void setId(UUID value);

    /**
     * Getter for <code>xj.user_auth_token.id</code>.
     */
    public UUID getId();

    /**
     * Setter for <code>xj.user_auth_token.user_auth_id</code>.
     */
    public void setUserAuthId(UUID value);

    /**
     * Getter for <code>xj.user_auth_token.user_auth_id</code>.
     */
    public UUID getUserAuthId();

    /**
     * Setter for <code>xj.user_auth_token.user_id</code>.
     */
    public void setUserId(UUID value);

    /**
     * Getter for <code>xj.user_auth_token.user_id</code>.
     */
    public UUID getUserId();

    /**
     * Setter for <code>xj.user_auth_token.access_token</code>.
     */
    public void setAccessToken(String value);

    /**
     * Getter for <code>xj.user_auth_token.access_token</code>.
     */
    public String getAccessToken();

    // -------------------------------------------------------------------------
    // FROM and INTO
    // -------------------------------------------------------------------------

    /**
     * Load data from another generated Record/POJO implementing the common
     * interface IUserAuthToken
     */
    public void from(IUserAuthToken from);

    /**
     * Copy data into another generated Record/POJO implementing the common
     * interface IUserAuthToken
     */
    public <E extends IUserAuthToken> E into(E into);
}
