package io.xj.model.pojos;


import java.io.Serializable;
import java.util.UUID;


@SuppressWarnings({"all", "unchecked", "rawtypes"})
public class UserAuthToken implements Serializable {

  private static final long serialVersionUID = 1L;

  private UUID id;
  private UUID userAuthId;
  private UUID userId;
  private String accessToken;

  public UserAuthToken() {
  }

  public UserAuthToken(UserAuthToken value) {
    this.id = value.id;
    this.userAuthId = value.userAuthId;
    this.userId = value.userId;
    this.accessToken = value.accessToken;
  }

  public UserAuthToken(
    UUID id,
    UUID userAuthId,
    UUID userId,
    String accessToken
  ) {
    this.id = id;
    this.userAuthId = userAuthId;
    this.userId = userId;
    this.accessToken = accessToken;
  }

  /**
   Getter for <code>xj.user_auth_token.id</code>.
   */
  public UUID getId() {
    return this.id;
  }

  /**
   Setter for <code>xj.user_auth_token.id</code>.
   */
  public void setId(UUID id) {
    this.id = id;
  }

  /**
   Getter for <code>xj.user_auth_token.user_auth_id</code>.
   */
  public UUID getUserAuthId() {
    return this.userAuthId;
  }

  /**
   Setter for <code>xj.user_auth_token.user_auth_id</code>.
   */
  public void setUserAuthId(UUID userAuthId) {
    this.userAuthId = userAuthId;
  }

  /**
   Getter for <code>xj.user_auth_token.user_id</code>.
   */
  public UUID getUserId() {
    return this.userId;
  }

  /**
   Setter for <code>xj.user_auth_token.user_id</code>.
   */
  public void setUserId(UUID userId) {
    this.userId = userId;
  }

  /**
   Getter for <code>xj.user_auth_token.access_token</code>.
   */
  public String getAccessToken() {
    return this.accessToken;
  }

  /**
   Setter for <code>xj.user_auth_token.access_token</code>.
   */
  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("UserAuthToken (");

    sb.append(id);
    sb.append(", ").append(userAuthId);
    sb.append(", ").append(userId);
    sb.append(", ").append(accessToken);

    sb.append(")");
    return sb.toString();
  }
}
