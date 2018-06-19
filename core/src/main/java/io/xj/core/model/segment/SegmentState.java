// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.segment;

import com.google.common.collect.Lists;
import io.xj.core.exception.BusinessException;
import io.xj.core.exception.CancelException;
import io.xj.core.transport.CSV;
import io.xj.core.util.Text;

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
    return Text.stringValues(values());
  }

  /**
   cast string to enum

   @param value to cast to enum
   @return enum
   @throws BusinessException on failure
   */
  public static SegmentState validate(String value) throws BusinessException {
    if (Objects.isNull(value))
      throw new BusinessException("State is required");

    try {
      return valueOf(Text.toProperSlug(value));
    } catch (Exception e) {
      throw new BusinessException("'" + value + "' is not a valid state (" + CSV.joinEnum(values()) + ").", e);
    }
  }

  /**
   Require state is in an array of states

   @param toState       to check
   @param allowedStates required to be in
   @throws CancelException if not in required states
   */
  public static void onlyAllowTransitions(SegmentState toState, SegmentState... allowedStates) throws CancelException {
    List<String> allowedStateNames = Lists.newArrayList();
    for (SegmentState search : allowedStates) {
      allowedStateNames.add(search.toString());
      if (Objects.equals(search, toState)) {
        return;
      }
    }
    throw new CancelException(String.format("transition to %s not in allowed (%s)",
      toState, CSV.join(allowedStateNames)));
  }

  /**
   * Segment state transitions are protected, dependent on the state this segment is being transitioned from, and the intended state it is being transitioned to.
   * @param fromState to protect transition from
   * @param toState to test transition to
   * @throws CancelException on prohibited transition
   */
  public static void protectTransition(SegmentState fromState, SegmentState toState) throws CancelException {
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
