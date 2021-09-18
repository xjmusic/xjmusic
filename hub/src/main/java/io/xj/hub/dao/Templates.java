// Copyright (c) 1999-2021, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.hub.dao;

import com.google.common.base.Strings;
import io.xj.hub.enums.ContentBindingType;
import io.xj.hub.tables.pojos.Template;
import io.xj.hub.tables.pojos.TemplateBinding;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 Utilities for working with templates
 */
public enum Templates {
  ;
   private static final String EXTENSION_SEPARATOR = ".";

  /**
   Get the full key from a key

   @param key of which to get full key
   @return full key
   */
  public static String getFullKey(String key) {
    return String.format("%s-full", key);
  }

  /**
   Get the identifier or a Template: embed key if available, else ID

   @param template to get identifier of
   @return embed key if available, else ID
   */
  public static String getIdentifier(@Nullable Template template) {
    if (Objects.isNull(template)) return "N/A";
    return Strings.isNullOrEmpty(template.getEmbedKey()) ? template.getId().toString() : template.getEmbedKey();
  }

  /**
   Filter and map target ids of a specified type from a set of template bindings

   @param templateBindings to filter and map from
   @param type          to include
   @return set of target ids of the specified type of template binding targets
   */
  public static Set<UUID> targetIdsOfType(Collection<TemplateBinding> templateBindings, ContentBindingType type) {
    return templateBindings.stream().filter(templateBinding -> Objects.equals(templateBinding.getType(), type))
      .map(TemplateBinding::getTargetId).collect(Collectors.toSet());
  }

  /**
   Describe a collection of templatebindings like Type[id###], Type[id###}, etc

   @param templateBindings to describe
   @return description of template bindings
   */
  public static String describe(Collection<TemplateBinding> templateBindings) {
    return templateBindings.stream()
      .map(b -> String.format("%s[%s]", b.getType().toString(), b.getTargetId()))
      .collect(Collectors.joining(", "));
  }
}
