// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.util;

import com.google.api.client.util.Lists;
import com.google.common.collect.Maps;

import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 Bag of Marbles
 <p>
 Choices should be random #180669293
 <p>
 The current implementation literally places one of each object in a bag in memory. However, this is inefficient compared to:
 - a new bag accepts the addition of N number of T type of marbles
 - each T type of marbles represents a block of integers theoretically. E.g. if there are 4 of Ta, 7 of Tb, and 12 of Tc, then we have a theoretical block of integers from 0-3 (Ta), 4-10 (Tb), and 11-22 (Tc).
 - choose a random number within the available blocks, like a needle of a roulette wheel, choosing the block it lands on. E.g. choose an integer from 0-22
 */
public class MarbleBag {
  private final Map<UUID, Integer> marbles;
  Random generator = new SecureRandom();  // need to randomize seed

  /**
   Construct a new Marble Bag
   */
  private MarbleBag() {
    marbles = Maps.newConcurrentMap();
  }

  /**
   Construct an empty marble bag
   */
  public static MarbleBag empty() {
    return new MarbleBag();
  }

  /**
   @return {String} marble picked at random from bag
   */
  public UUID pick() throws RuntimeException {
    var total = 0;
    List<MarbleBlock> blocks = Lists.newArrayList();

    for (Map.Entry<UUID, Integer> entry : marbles.entrySet())
      if (0 < entry.getValue()) {
        blocks.add(new MarbleBlock(entry.getKey(), total, total + entry.getValue()));
        total += entry.getValue();
      }

    if (blocks.isEmpty())
      throw new RuntimeException("No marbles in the bag!");

    if (0 == total)
      return blocks.get(0).id;

    var pickIdx = generator.nextInt(total);

    for (MarbleBlock block : blocks)
      if (pickIdx >= block.from && pickIdx < block.to)
        return block.id;

    throw new RuntimeException("No marble picked!");
  }

  /**
   Add all marbles from another object mapping marble -> quantity

   @param toAdd map of marble id to quantity
   */
  public void addAll(Map<UUID, Integer> toAdd) {
    for (Map.Entry<UUID, Integer> entry : toAdd.entrySet())
      add(entry.getKey(), entry.getValue());
  }

  /**
   Add one marble to the bag; increments the count of this marble +1

   @param id of the marble to add
   */
  public void add(UUID id) {
    add(id, 1);
  }

  /**
   Number of marbles in the bag

   @return {number}
   */
  public int size() {
    return marbles.values().stream().mapToInt(i -> i).sum();
  }

  /**
   Display as string
   */
  public String toString() {
    return marbles.entrySet().stream()
      .map((entry) -> String.format("%s:%d", entry.getKey().toString(), entry.getValue()))
      .collect(Collectors.joining(", "));
  }

  /**
   Add a quantity of marbles to the bag; increments the count of the specified marble by the specified quantity.

   @param marbleId of the marble to add
   @param qty      quantity of this marble to add
   */
  public void add(UUID marbleId, Integer qty) {
    if (marbles.containsKey(marbleId))
      marbles.put(marbleId, marbles.get(marbleId) + qty);
    marbles.put(marbleId, qty);
  }

  public boolean isEmpty() {
    return 0 == size();
  }

  /**
   Block of marbles with a given id
   */
  private static class MarbleBlock {
    UUID id;
    Integer from;
    Integer to;

    public MarbleBlock(UUID id, int from, int to) {
      this.id = id;
      this.from = from;
      this.to = to;
    }
  }

}

