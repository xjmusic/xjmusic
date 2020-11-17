// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus.dao;

import io.xj.ChainBinding;
import io.xj.lib.util.CSV;
import io.xj.lib.util.Text;
import io.xj.lib.util.ValueException;

import java.util.List;

public interface ChainBindingDAO extends DAO<ChainBinding> {

  /**
   String Values

   @return ImmutableList of string values
   */
  static List<String> chainBindingStringValues() {
    return Text.toStrings(ChainBinding.Type.values());
  }

  /**
   Type based on entity

   @param entity to get type based on
   @return type baesd on entity
   */
  static ChainBinding.Type chainBindingTypeValueOf(Object entity) throws ValueException {
    String entityName = entity.getClass().getSimpleName();
    switch (entityName) {
      case "Library":
        return ChainBinding.Type.Library;
      case "Program":
        return ChainBinding.Type.Program;
      case "Instrument":
        return ChainBinding.Type.Instrument;
      default:
        throw new ValueException("'" + entityName + "' is not a valid entity (" + CSV.joinEnum(ChainBinding.Type.values()) + ").");
    }
  }
}
