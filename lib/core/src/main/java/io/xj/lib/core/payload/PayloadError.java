// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.core.payload;

import com.google.common.collect.Maps;
import io.xj.lib.core.util.Text;

import java.util.Map;

/**
 [#167276586] JSON API facilitates complex transactions
 */
public class PayloadError {
  public static final String KEY_ABOUT = "about";
  public static final String KEY_LINKS = "links";
  public static final String KEY_CODE = "code";
  public static final String KEY_TITLE = "title";
  public static final String KEY_DETAIL = "detail";
  public static final String KEY_ID = "id";
  private final Map<String, String> links = Maps.newHashMap();
  private String code;
  private String title;
  private String detail;
  private String id;

  /**
   Create a PayloadError of an exception

   @param exception to get error of
   @return payload error
   */
  public static PayloadError of(Exception exception) {
    return new PayloadError()
      .setCode(Text.getSimpleName(exception))
      .setTitle(exception.getMessage());
  }

  /**
   get Code

   @return Code
   */
  public String getCode() {
    return code;
  }

  /**
   Get detail

   @return detail
   */
  public String getDetail() {
    return detail;
  }

  /**
   get Id

   @return Id
   */
  public String getId() {
    return id;
  }

  /**
   get Links

   @return Links
   */
  public Map<String, String> getLinks() {
    return links;
  }

  /**
   Get title

   @return title
   */
  public String getTitle() {
    return title;
  }

  /**
   set about Link

   @param aboutLink to set
   @return this PayloadError (for chaining methods)
   */
  public PayloadError setAboutLink(String aboutLink) {
    links.clear();
    links.put(KEY_ABOUT, aboutLink);
    return this;
  }

  /**
   set Code

   @param code to set
   @return this PayloadError (for chaining methods)
   */
  public PayloadError setCode(String code) {
    this.code = code;
    return this;
  }

  /**
   set detail

   @param detail to set
   @return this PayloadError (for chaining methods)
   */
  public PayloadError setDetail(String detail) {
    this.detail = detail;
    return this;
  }

  /**
   set Id

   @param id to set
   @return this PayloadError (for chaining methods)
   */
  public PayloadError setId(String id) {
    this.id = id;
    return this;
  }

  /**
   set title

   @param title to set
   @return this PayloadError (for chaining methods)
   */
  public PayloadError setTitle(String title) {
    this.title = title;
    return this;
  }
}
