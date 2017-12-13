// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.link_meme;

import io.xj.core.exception.BusinessException;
import io.xj.core.model.meme.Meme;
import io.xj.core.util.Text;

import org.jooq.Field;
import org.jooq.Record;
import org.jooq.types.ULong;

import com.google.api.client.util.Maps;

import java.math.BigInteger;
import java.util.Map;
import java.util.Objects;

import static io.xj.core.Tables.LINK_MEME;

/**
 Entity for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an instance of this object
 <p>
 Business logic ought to be performed beginning with an instance of this object,
 to implement common methods.
 <p>
 NOTE: There can only be ONE of any getter/setter (with the same # of input params)
 */
public class LinkMeme extends Meme {

  /**
   For use in maps.
   */
  public static final String KEY_ONE = "linkMeme";
  public static final String KEY_MANY = "linkMemes";

  // Link ID
  private ULong linkId;

  public ULong getLinkId() {
    return linkId;
  }

  public LinkMeme setLinkId(BigInteger linkId) {
    this.linkId = ULong.valueOf(linkId);
    return this;
  }

  public LinkMeme setName(String name) {
    this.name = Text.toProperSlug(name);
    return this;
  }

  @Override
  public void validate() throws BusinessException {
    if (null == linkId) {
      throw new BusinessException("Link ID is required.");
    }
    super.validate();
  }

  @Override
  public LinkMeme setFromRecord(Record record) {
    if (Objects.isNull(record)) {
      return null;
    }
    id = record.get(LINK_MEME.ID);
    linkId = record.get(LINK_MEME.LINK_ID);
    name = record.get(LINK_MEME.NAME);
    createdAt = record.get(LINK_MEME.CREATED_AT);
    updatedAt = record.get(LINK_MEME.UPDATED_AT);
    return this;
  }

  @Override
  public Map<Field, Object> updatableFieldValueMap() {
    Map<Field, Object> fieldValues = Maps.newHashMap();
    fieldValues.put(LINK_MEME.LINK_ID, linkId);
    fieldValues.put(LINK_MEME.NAME, name);
    return fieldValues;
  }

  public static LinkMeme of(ULong linkId, String name) {
    return
      new LinkMeme()
        .setLinkId(linkId.toBigInteger())
        .setName(name);
  }

}
