package io.xj.core.model.link_message;

import io.xj.core.exception.BusinessException;
import io.xj.core.model.message.Message;
import io.xj.core.model.message.MessageType;
import io.xj.core.transport.CSV;
import io.xj.core.util.Text;

import org.jooq.Field;
import org.jooq.Record;
import org.jooq.types.ULong;

import com.google.api.client.util.Maps;

import java.math.BigInteger;
import java.util.Map;
import java.util.Objects;

import static io.xj.core.Tables.LINK_MESSAGE;

/**
 * Entity for use as POJO for decoding messages received by JAX-RS resources
 * a.k.a. JSON input will be stored into an instance of this object
 * <p>
 * Business logic ought to be performed beginning with an instance of this object,
 * to implement common methods.
 * <p>
 * NOTE: There can only be ONE of any getter/setter (with the same # of input params)
 */
public class LinkMessage extends Message {
  /**
   * For use in maps.
   */
  public static final String KEY_ONE = "linkMessage";
  public static final String KEY_MANY = "linkMessages";

  private static final int BODY_LENGTH_LIMIT = 65535;
  private static final String BODY_TRUNCATE_SUFFIX = " (truncated to fit character limit)";

  @Override
  public void validate() throws BusinessException {
    if (null == linkId) {
      throw new BusinessException("Link ID is required.");
    }
    if (null == type || type.length() == 0) {
      throw new BusinessException("Type is required.");
    }
    if (!TYPES.contains(type)) {
      throw new BusinessException("'" + this.type + "' is not a valid type (" + CSV.join(TYPES) + ").");
    }
    if (null == body || body.length() == 0) {
      throw new BusinessException("Body is required.");
    }
    if (body.length() > BODY_LENGTH_LIMIT) {
      body = body.substring(0, BODY_LENGTH_LIMIT - BODY_TRUNCATE_SUFFIX.length()) + BODY_TRUNCATE_SUFFIX;
    }
  }

  @Override
  public LinkMessage setFromRecord(Record record) {
    if (Objects.isNull(record)) {
      return null;
    }
    id = record.get(LINK_MESSAGE.ID);
    linkId = record.get(LINK_MESSAGE.LINK_ID);
    type = record.get(LINK_MESSAGE.TYPE);
    body = record.get(LINK_MESSAGE.BODY);
    createdAt = record.get(LINK_MESSAGE.CREATED_AT);
    updatedAt = record.get(LINK_MESSAGE.UPDATED_AT);
    return this;
  }

  @Override
  public Map<Field, Object> updatableFieldValueMap() {
    Map<Field, Object> fieldValues = Maps.newHashMap();
    fieldValues.put(LINK_MESSAGE.LINK_ID, linkId);
    fieldValues.put(LINK_MESSAGE.BODY, body);
    fieldValues.put(LINK_MESSAGE.TYPE, type);
    return fieldValues;
  }

  @Override
  public LinkMessage setBody(String body) {
    this.body = body;
    return this;
  }

  @Override
  public LinkMessage setLinkId(BigInteger linkId) {
    this.linkId = ULong.valueOf(linkId);
    return this;
  }

  @Override
  public LinkMessage setType(MessageType type) {
    this.type = Text.toProperSlug(type.toString());
    return this;
  }

}
