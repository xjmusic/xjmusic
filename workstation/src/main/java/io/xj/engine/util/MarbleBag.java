// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.engine.util;

import io.xj.model.util.TremendouslyRandom;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Bag of Marbles
 * <p>
 * Choices should be random https://github.com/xjmusic/xjmusic/issues/291
 * <p>
 * The current implementation literally places one of each object in a bag in memory. However, this is inefficient compared to:
 * - a new bag accepts the addition of N number of T type of marbles
 * - each T type of marbles represents a block of integers theoretically. E.g. if there are 4 of Ta, 7 of Tb, and 12 of Tc, then we have a theoretical block of integers from 0-3 (Ta), 4-10 (Tb), and 11-22 (Tc).
 * - choose a random number within the available blocks, like a needle of a roulette wheel, choosing the block it lands on. E.g. choose an integer from 0-22
 * <p>
 * Marble bag has phases https://github.com/xjmusic/xjmusic/issues/291
 * <p>
 * This will consolidate the logic around "choose this if available, else that, else that"
 * XJ’s marble bag is actually divided into phases. When a marble is put into the bag, it is assigned a phase.
 * For example, if the phase 1 bag contains any marbles, we will pick from only the phase 1 bag and skip phases 2 and beyond.
 * This supports functionality such as “XJ always chooses a directly-bound program or instrument when available”
 */
public class MarbleBag {
  final Map<Integer/*Phase*/, Map<UUID/*Id*/, Integer/*Qty*/>> marbles;

  /**
   * Construct a new Marble Bag
   */
  MarbleBag() {
    marbles = new ConcurrentHashMap<>();
  }

  /**
   * Construct an empty marble bag
   */
  public static MarbleBag empty() {
    return new MarbleBag();
  }

  /**
   * @return {String} marble picked at random from bag
   */
  public UUID pick() throws RuntimeException {
    List<Integer> phases = marbles.keySet().stream().sorted(Integer::compare).toList();
    Optional<UUID> pick;

    for (var phase : phases) {
      pick = pickPhase(phase);
      if (pick.isPresent())
        return pick.get();
    }

    throw new RuntimeException(String.format("%d available; no marble picked!", size()));
  }

  /**
   * Add all marbles from another object mapping marble -> quantity
   *
   * @param phase of selection
   * @param toAdd map of marble id to quantity
   */
  public void addAll(Integer phase, Map<UUID, Integer> toAdd) {
    for (Map.Entry<UUID, Integer> entry : toAdd.entrySet())
      add(phase, entry.getKey(), entry.getValue());
  }

  /**
   * Add one marble to the bag; increments the count of this marble +1
   *
   * @param phase of selection
   * @param id    of the marble to add
   */
  public void add(Integer phase, UUID id) {
    add(phase, id, 1);
  }

  /**
   * Add a quantity of marbles to the bag; increments the count of the specified marble by the specified quantity.
   *
   * @param phase of selection
   * @param id    of the marble to add
   * @param qty   quantity of this marble to add
   */
  public void add(Integer phase, UUID id, Integer qty) {
    if (!marbles.containsKey(phase))
      marbles.put(phase, new ConcurrentHashMap<>());
    if (marbles.get(phase).containsKey(id))
      marbles.get(phase).put(id, marbles.get(phase).get(id) + qty);
    marbles.get(phase).put(id, qty);
  }

  /**
   * Number of marbles in the bag
   *
   * @return {number}
   */
  public int size() {
    return marbles.values().stream()
      .flatMapToInt(phase -> phase.values().stream().mapToInt(qty -> qty))
      .sum();
  }

  /**
   * Display as string
   */
  public String toString() {
    return marbles.entrySet().stream()
      .sorted(Comparator.comparingInt(Map.Entry::getKey))
      .map((phase) -> String.format("Phase%d[%s]", phase.getKey(),
        phase.getValue().entrySet().stream()
          .map((marble) -> String.format("%s:%d", marble.getKey().toString(), marble.getValue()))
          .collect(Collectors.joining(", "))
      ))
      .collect(Collectors.joining(", "));
  }

  /**
   * @return true if the marble bag is completely empty
   */
  public boolean isEmpty() {
    return 0 == size();
  }

  /**
   * @return true if there are any marbles in the bag
   */
  public boolean isPresent() {
    return 0 < size();
  }

  /**
   * Pick a marble from the specified phase
   *
   * @param phase from which to pick a marble
   * @return marble if available
   */
  Optional<UUID> pickPhase(Integer phase) {
    var total = 0;
    List<Group> blocks = new ArrayList<>();

    for (Map.Entry<UUID, Integer> entry : marbles.get(phase).entrySet())
      if (0 < entry.getValue()) {
        blocks.add(new Group(entry.getKey(), total, total + entry.getValue()));
        total += entry.getValue();
      }

    if (blocks.isEmpty())
      return Optional.empty();

    if (0 == total)
      return Optional.of(blocks.get(0).id);

    var pickIdx = TremendouslyRandom.zeroToLimit(total);

    for (Group block : blocks)
      if (pickIdx >= block.from && pickIdx < block.to)
        return Optional.of(block.id);

    return Optional.empty();
  }

  /**
   * Group of marbles with a given id
   */
  static class Group {
    UUID id;
    Integer from;
    Integer to;

    public Group(UUID id, int from, int to) {
      this.id = id;
      this.from = from;
      this.to = to;
    }
  }

  public static <N> Optional<N> quickPick(List<N> items) {
    return items.isEmpty() ? Optional.empty() : Optional.of(items.get(TremendouslyRandom.zeroToLimit(items.size())));
  }

}

