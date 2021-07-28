// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.entity.common;

import com.google.common.collect.Lists;
import io.xj.UserRole;
import io.xj.lib.util.CSV;
import io.xj.lib.util.Text;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

public enum User {
  ;

  /**
   extract a collection of ids of a string CSV

   @param csv to parse
   @return collection of ids
   */
  public static Collection<UserRole.Type> userRoleTypesFromCsv(String csv) {
    Collection<UserRole.Type> result = Lists.newArrayList();

    if (Objects.nonNull(csv) && !csv.isEmpty()) {
      result = CSV.split(csv).stream().map(type -> UserRole.Type.valueOf(Text.toProperSlug(type))).collect(Collectors.toList());
    }

    return result;
  }

  /**
   Get CSV of a collection of user role types

   @param roleTypes to get CSV of
   @return CSV of user role types
   */
  public static String csvOfUserRoleTypes(Collection<UserRole.Type> roleTypes) {
    return CSV.join(roleTypes.stream().map(Enum::toString).collect(Collectors.toList()));
  }
}
