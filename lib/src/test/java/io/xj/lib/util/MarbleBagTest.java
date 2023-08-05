// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.lib.util;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Choices should be random https://www.pivotaltracker.com/story/show/180669293
 */
public class MarbleBagTest {
  static final Logger LOG = LoggerFactory.getLogger(MarbleBagTest.class);
  UUID frogId = UUID.randomUUID();
  UUID bearId = UUID.randomUUID();
  UUID zebraId = UUID.randomUUID();

  @Before
  public void setUp() throws Exception {
  }

  /**
   * adds all marbles from an object containing other marbles
   */
  @Test
  public void addAll() {
    var bag = MarbleBag.empty();
    bag.addAll(1, Map.of(
      frogId, 1000,
      bearId, 30,
      zebraId, 5));
    assertEquals(1035, bag.size());
  }

  /**
   * adds marbles to bag, picks 100 times and asserts allowed results
   */
  @Test
  public void add_pick() {
    var bag = MarbleBag.empty();
    bag.add(1, frogId, 1000);
    bag.add(1, bearId, 30);
    bag.add(1, zebraId, 5);
    LOG.info("will pick 100 marbles from {}", bag);
    var result = MarbleBag.empty();
    var allowed = Set.of(frogId, bearId, zebraId);
    for (var i = 0; i < 100; i++) {
      var pick = bag.pick();
      assertTrue(allowed.contains(pick));
      result.add(1, pick);
    }
    LOG.info("picked {}", result);
  }

  /**
   * adds marbles to bag in multiple phases, picks 100 times and asserts allowed results
   * <p>
   * Marble bag has phases https://www.pivotaltracker.com/story/show/180832650
   * <p>
   * This will consolidate the logic around "choose this if available, else that, else that"
   * XJ’s marble bag is actually divided into phases. When a marble is put into the bag, it is assigned a phase.
   * For example, if the phase 1 bag contains any marbles, we will pick from only the phase 1 bag and skip phases 2 and beyond.
   * This supports functionality such as “XJ always chooses a directly-bound program or instrument when available”
   */
  @Test
  public void pick_phaseLowerPreferred() {
    var bag = MarbleBag.empty();
    bag.add(1, frogId, 1000);
    bag.add(1, bearId, 30);
    bag.add(2, zebraId, 5);
    LOG.info("will pick 100 marbles from {}", bag);
    var result = MarbleBag.empty();
    var allowed = Set.of(frogId, bearId);
    for (var i = 0; i < 100; i++) {
      var pick = bag.pick();
      assertTrue(allowed.contains(pick));
      result.add(1, pick);
    }
    LOG.info("picked {}", result);
  }

  /**
   * tells us how many marbles are in the bag
   */
  @Test
  public void size() {
    var bag = MarbleBag.empty();
    bag.add(1, frogId, 1000);
    bag.add(1, bearId, 30);
    bag.add(1, zebraId, 5);
    assertEquals(1035, bag.size());
  }

  @Test
  public void isEmpty() {
    var bag = MarbleBag.empty();
    assertTrue(bag.isEmpty());
    bag.add(1, frogId, 1000);
    assertFalse(bag.isEmpty());
  }

  @Test
  public void isEmpty_notIfAnyPhasesHaveMarbles() {
    var bag = MarbleBag.empty();
    assertTrue(bag.isEmpty());
    bag.add(1, bearId, 0);
    bag.add(2, frogId, 1000);
    assertFalse(bag.isEmpty());
  }

  @Test
  public void quickPick() {
    var pick = MarbleBag.quickPick(List.of(frogId, bearId, zebraId));
    assertTrue(List.of(frogId, bearId, zebraId).contains(pick.orElseThrow()));
    assertTrue(MarbleBag.quickPick(List.of()).isEmpty());
  }
}
