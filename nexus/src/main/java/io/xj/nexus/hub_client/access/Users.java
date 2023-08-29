// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.hub_client.access;

import io.xj.hub.enums.UserRoleType;
import io.xj.hub.util.CsvUtils;
import io.xj.hub.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

public enum Users {
  ;

  /**
   * extract a collection of ids of a string CSV
   *
   * @param csv to parse
   * @return collection of ids
   */
  public static Collection<UserRoleType> userRoleTypesFromCsv(String csv) {
    Collection<UserRoleType> result = new ArrayList<>();

    if (Objects.nonNull(csv) && !csv.isEmpty()) {
      result = CsvUtils.split(csv).stream().map(type -> UserRoleType.valueOf(StringUtils.toProperSlug(type))).collect(Collectors.toList());
    }

    return result;
  }

  /**
   * Get CSV of a collection of user role types
   *
   * @param roleTypes to get CSV of
   * @return CSV of user role types
   */
  public static String csvOfUserRoleTypes(Collection<UserRoleType> roleTypes) {
    return CsvUtils.join(roleTypes.stream().map(Enum::toString).collect(Collectors.toList()));
  }
}
