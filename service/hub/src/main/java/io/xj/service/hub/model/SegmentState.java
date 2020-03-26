// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.model;

import com.google.common.collect.Lists;
import io.xj.lib.util.CSV;
import io.xj.lib.util.Text;
import io.xj.lib.util.ValueException;

import java.util.List;
import java.util.Objects;

public enum SegmentState {
  Planned,
  Crafting,
  Crafted,
  Dubbing,
  Dubbed,
  Failed;

  /**
   String Values

   @return ImmutableList of string values
   */
  public static List<String> stringValues() {
    return Text.toStrings(values());
  }

  /**
   cast string to enum

   @param value to cast to enum
   @return enum
   @throws ValueException on failure
   */
  public static SegmentState validate(String value) throws ValueException {
    if (Objects.isNull(value))
      throw new ValueException("State is required");

    try {
      return valueOf(Text.toProperSlug(value));
    } catch (Exception e) {
      throw new ValueException("'" + value + "' is not a valid state (" + CSV.joinEnum(values()) + ").", e);
    }
  }

  /**
   Require state is in an array of states

   @param toState       to check
   @param allowedStates required to be in
   @throws ValueException if not in required states
   */
  public static void onlyAllowTransitions(SegmentState toState, SegmentState... allowedStates) throws ValueException {
    List<String> allowedStateNames = Lists.newArrayList();
    for (SegmentState search : allowedStates) {
      allowedStateNames.add(search.toString());
      if (Objects.equals(search, toState)) {
        return;
      }
    }
    throw new ValueException(String.format("transition to %s not in allowed (%s)",
      toState, CSV.join(allowedStateNames)));
  }

  /**
   Segment state transitions are protected, dependent on the state this segment is being transitioned of, and the intended state it is being transitioned to.

   @param fromState to protect transition of
   @param toState   to test transition to
   @throws ValueException on prohibited transition
   */
  public static void protectTransition(SegmentState fromState, SegmentState toState) throws ValueException {
    switch (fromState) {

      case Planned:
        onlyAllowTransitions(toState, SegmentState.Planned, SegmentState.Crafting);
        break;

      case Crafting:
        onlyAllowTransitions(toState, SegmentState.Crafting, SegmentState.Crafted, SegmentState.Dubbing, SegmentState.Failed, SegmentState.Planned);
        break;

      case Crafted:
        onlyAllowTransitions(toState, SegmentState.Crafted, SegmentState.Dubbing);
        break;

      case Dubbing:
        onlyAllowTransitions(toState, SegmentState.Dubbing, SegmentState.Dubbed, SegmentState.Failed);
        break;

      case Dubbed:
        onlyAllowTransitions(toState, SegmentState.Dubbed);
        break;

      case Failed:
        onlyAllowTransitions(toState, SegmentState.Failed);
        break;

      default:
        onlyAllowTransitions(toState, SegmentState.Planned);
        break;
    }
  }
}
