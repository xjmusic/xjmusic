package io.xj.model.pojos;


import io.xj.model.enums.UserAuthType;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;


@SuppressWarnings({"all", "unchecked", "rawtypes"})
public class UserAuth implements Serializable, Comparable<UserAuth> {

  private static final long serialVersionUID = 1L;

  private UUID id;
  private UserAuthType type;
  private String externalAccessToken;
  private String externalRefreshToken;
  private String externalAccount;
  private UUID userId;

  public UserAuth() {
  }

  public UserAuth(UserAuth value) {
    this.id = value.id;
    this.type = value.type;
    this.externalAccessToken = value.externalAccessToken;
    this.externalRefreshToken = value.externalRefreshToken;
    this.externalAccount = value.externalAccount;
    this.userId = value.userId;
  }

  public UserAuth(
    UUID id,
    UserAuthType type,
    String externalAccessToken,
    String externalRefreshToken,
    String externalAccount,
    UUID userId
  ) {
    this.id = id;
    this.type = type;
    this.externalAccessToken = externalAccessToken;
    this.externalRefreshToken = externalRefreshToken;
    this.externalAccount = externalAccount;
    this.userId = userId;
  }

  /**
   Getter for <code>xj.user_auth.id</code>.
   */
  public UUID getId() {
    return this.id;
  }

  /**
   Setter for <code>xj.user_auth.id</code>.
   */
  public void setId(UUID id) {
    this.id = id;
  }

  /**
   Getter for <code>xj.user_auth.type</code>.
   */
  public UserAuthType getType() {
    return this.type;
  }

  /**
   Setter for <code>xj.user_auth.type</code>.
   */
  public void setType(UserAuthType type) {
    this.type = type;
  }

  /**
   Getter for <code>xj.user_auth.external_access_token</code>.
   */
  public String getExternalAccessToken() {
    return this.externalAccessToken;
  }

  /**
   Setter for <code>xj.user_auth.external_access_token</code>.
   */
  public void setExternalAccessToken(String externalAccessToken) {
    this.externalAccessToken = externalAccessToken;
  }

  /**
   Getter for <code>xj.user_auth.external_refresh_token</code>.
   */
  public String getExternalRefreshToken() {
    return this.externalRefreshToken;
  }

  /**
   Setter for <code>xj.user_auth.external_refresh_token</code>.
   */
  public void setExternalRefreshToken(String externalRefreshToken) {
    this.externalRefreshToken = externalRefreshToken;
  }

  /**
   Getter for <code>xj.user_auth.external_account</code>.
   */
  public String getExternalAccount() {
    return this.externalAccount;
  }

  /**
   Setter for <code>xj.user_auth.external_account</code>.
   */
  public void setExternalAccount(String externalAccount) {
    this.externalAccount = externalAccount;
  }

  /**
   Getter for <code>xj.user_auth.user_id</code>.
   */
  public UUID getUserId() {
    return this.userId;
  }

  /**
   Setter for <code>xj.user_auth.user_id</code>.
   */
  public void setUserId(UUID userId) {
    this.userId = userId;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("UserAuth (");

    sb.append(id);
    sb.append(", ").append(type);
    sb.append(", ").append(externalAccessToken);
    sb.append(", ").append(externalRefreshToken);
    sb.append(", ").append(externalAccount);
    sb.append(", ").append(userId);

    sb.append(")");
    return sb.toString();
  }

  @Override
  public int compareTo(UserAuth o) {
    if (!Objects.equals(userId, o.userId))
      return userId.compareTo(o.userId);
    if (!Objects.equals(type, o.type))
      return type.compareTo(o.type);
    return id.compareTo(o.id);

  }
}
