// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.lib.jsonapi;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.xj.lib.entity.Entities;

import java.util.HashMap;
import java.util.Map;

/**
 * Error for a Payload sent/received to/from a XJ Music REST JSON:API service
 * <p>
 * Created by Charney Kaye on 2020/03/05
 * <p>
 * Payloads are serialized & deserialized with custom Jackson implementations.
 * Much of the complexity of serializing and deserializing stems of the fact that
 * the JSON:API standard uses a data object for One record, and a data array for Many records.
 */
@JsonSerialize(using = PayloadErrorSerializer.class)
public class PayloadError {
  public static final String KEY_ABOUT = "about";
  public static final String KEY_LINKS = "links";
  public static final String KEY_CODE = "code";
  public static final String KEY_TITLE = "title";
  public static final String KEY_DETAIL = "detail";
  public static final String KEY_ID = "id";
  final Map<String, String> links = new HashMap<>();
  String code;
  String title;
  String detail;
  String id;

  /**
   * Create a PayloadError of an exception
   *
   * @param exception to get error of
   * @return payload error
   */
  public static PayloadError of(Exception exception) {
    return new PayloadError()
      .setCode(Entities.getSimpleName(exception))
      .setTitle(exception.getMessage());
  }

  /**
   * get Code
   *
   * @return Code
   */
  public String getCode() {
    return code;
  }

  /**
   * set Code
   *
   * @param code to set
   * @return this PayloadError (for chaining methods)
   */
  public PayloadError setCode(String code) {
    this.code = code;
    return this;
  }

  /**
   * Get detail
   *
   * @return detail
   */
  public String getDetail() {
    return detail;
  }

  /**
   * set detail
   *
   * @param detail to set
   * @return this PayloadError (for chaining methods)
   */
  public PayloadError setDetail(String detail) {
    this.detail = detail;
    return this;
  }

  /**
   * get Id
   *
   * @return Id
   */
  public String getId() {
    return id;
  }

  /**
   * set Id
   *
   * @param id to set
   * @return this PayloadError (for chaining methods)
   */
  public PayloadError setId(String id) {
    this.id = id;
    return this;
  }

  /**
   * get Links
   *
   * @return Links
   */
  public Map<String, String> getLinks() {
    return links;
  }

  /**
   * Get title
   *
   * @return title
   */
  public String getTitle() {
    return title;
  }

  /**
   * set title
   *
   * @param title to set
   * @return this PayloadError (for chaining methods)
   */
  public PayloadError setTitle(String title) {
    this.title = title;
    return this;
  }

  /**
   * set about Link
   *
   * @param aboutLink to set
   * @return this PayloadError (for chaining methods)
   */
  public PayloadError setAboutLink(String aboutLink) {
    links.clear();
    links.put(KEY_ABOUT, aboutLink);
    return this;
  }
}
