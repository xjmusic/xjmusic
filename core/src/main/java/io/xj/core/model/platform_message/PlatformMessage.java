package io.xj.core.model.platform_message;

import io.xj.core.exception.BusinessException;
import io.xj.core.model.message.Message;

import org.jooq.Field;
import org.jooq.Record;

import com.google.api.client.util.Maps;

import java.util.Map;
import java.util.Objects;

import static io.xj.core.Tables.PLATFORM_MESSAGE;

/**
 Entity for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an instance of this object
 <p>
 Business logic ought to be performed beginning with an instance of this object,
 to implement common methods.
 <p>
 NOTE: There can only be ONE of any getter/setter (with the same # of input params)
 */
public class PlatformMessage extends Message {
  /**
   For use in maps.
   */
  public static final String KEY_ONE = "platformMessage";
  public static final String KEY_MANY = "platformMessages";

  private static final int BODY_LENGTH_LIMIT = 65535;
  private static final String BODY_TRUNCATE_SUFFIX = " (truncated to fit character limit)";

  @Override
  public void validate() throws BusinessException {
    super.validate();
    if (null == body || body.isEmpty()) {
      throw new BusinessException("Body is required.");
    }
    if (BODY_LENGTH_LIMIT < body.length()) {
      body = body.substring(0, BODY_LENGTH_LIMIT - BODY_TRUNCATE_SUFFIX.length()) + BODY_TRUNCATE_SUFFIX;
    }
  }

  @Override
  public PlatformMessage setFromRecord(Record record) {
    if (Objects.isNull(record)) {
      return null;
    }
    id = record.get(PLATFORM_MESSAGE.ID);
    type = record.get(PLATFORM_MESSAGE.TYPE);
    body = record.get(PLATFORM_MESSAGE.BODY);
    createdAt = record.get(PLATFORM_MESSAGE.CREATED_AT);
    updatedAt = record.get(PLATFORM_MESSAGE.UPDATED_AT);
    return this;
  }

  @Override
  public Map<Field, Object> updatableFieldValueMap() {
    Map<Field, Object> fieldValues = Maps.newHashMap();
    fieldValues.put(PLATFORM_MESSAGE.BODY, body);
    fieldValues.put(PLATFORM_MESSAGE.TYPE, type);
    return fieldValues;
  }

  @Override
  public PlatformMessage setBody(String body) {
    this.body = body;
    return this;
  }

  @Override
  public PlatformMessage setType(String type) {
    super.setType(type);
    return this;
  }

}
