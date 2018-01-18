package io.xj.core.model.link_message;

import io.xj.core.exception.BusinessException;
import io.xj.core.model.message.Message;

import java.math.BigInteger;
import java.util.Objects;

/**
 POJO for persisting data in memory while performing business logic,
or decoding messages received by JAX-RS resources.
 a.k.a. JSON input will be stored into an instance of this object
 <p>
 Business logic ought to be performed beginning with an instance of this object,
 to implement common methods.
 <p>
 NOTE: There can only be ONE of any getter/setter (with the same # of input params)
 */
public class LinkMessage extends Message {
  /**
   For use in maps.
   */
  public static final String KEY_ONE = "linkMessage";
  public static final String KEY_MANY = "linkMessages";

  private static final int BODY_LENGTH_LIMIT = 65535;
  private static final String BODY_TRUNCATE_SUFFIX = " (truncated to fit character limit)";

  protected BigInteger linkId;

  @Override
  public BigInteger getParentId() {
    return linkId;
  }

  @Override
  public void validate() throws BusinessException {
    super.validate();
    if (Objects.isNull(linkId)) {
      throw new BusinessException("Link ID is required.");
    }
    if (Objects.isNull(body) || body.isEmpty()) {
      throw new BusinessException("Body is required.");
    }
    if (BODY_LENGTH_LIMIT < body.length()) {
      body = body.substring(0, BODY_LENGTH_LIMIT - BODY_TRUNCATE_SUFFIX.length()) + BODY_TRUNCATE_SUFFIX;
    }
  }

  @Override
  public LinkMessage setBody(String body) {
    this.body = body;
    return this;
  }

  public BigInteger getLinkId() {
    return linkId;
  }

  public LinkMessage setLinkId(BigInteger linkId) {
    this.linkId = linkId;
    return this;
  }

  @Override
  public LinkMessage setType(String type) {
    super.setType(type);
    return this;
  }

}
