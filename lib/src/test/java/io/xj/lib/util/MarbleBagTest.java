// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.util;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 Choices should be random #180669293
 */
public class MarbleBagTest {
  private static final Logger LOG = LoggerFactory.getLogger(MarbleBagTest.class);
  UUID frogId = UUID.randomUUID();
  UUID bearId = UUID.randomUUID();
  UUID zebraId = UUID.randomUUID();

  @Before
  public void setUp() throws Exception {
  }

  /**
   adds all marbles from an object containing other marbles
   */
  @Test
  public void addAll() {
    var bag = MarbleBag.empty();
    bag.addAll(Map.of(
      frogId, 1000,
      bearId, 30,
      zebraId, 5));
    assertEquals(1035, bag.size());
  }

  /**
   adds marbles to bag, picks 100 marbles into another bag, logs that resulting bag
   */
  @Test
  public void add_pick() throws ValueException {
    var bag = MarbleBag.empty();
    bag.add(frogId, 1000);
    bag.add(bearId, 30);
    bag.add(zebraId, 5);
    LOG.info("will pick 100 marbles from {}", bag);
    var result = MarbleBag.empty();
    var allowed = Set.of(frogId, bearId, zebraId);
    for (var i = 0; i < 100; i++) {
      var pick = bag.pick();
      assertTrue(allowed.contains(pick));
      result.add(pick);
    }
    LOG.info("picked {}", result);
  }

  /**
   tells us how many marbles are in the bag
   */
  @Test
  public void size() {
    var bag = MarbleBag.empty();
    bag.add(frogId, 1000);
    bag.add(bearId, 30);
    bag.add(zebraId, 5);
    assertEquals(1035, bag.size());
  }

}
