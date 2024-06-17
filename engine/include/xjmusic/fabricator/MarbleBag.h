// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

#ifndef XJMUSIC_UTIL_MARBLE_BAG_H
#define XJMUSIC_UTIL_MARBLE_BAG_H

#include <map>
#include <set>
#include <random>
#include <optional>

#include "xjmusic/entities/content/ContentEntity.h"

namespace XJ {

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
  class MarbleBag {
  public:

    /**
     * Construct a new Marble Bag
     */
    MarbleBag() = default;

    /**
     * Construct an empty marble bag
     */
    static MarbleBag empty();

    /**
     * @return {String} marble picked at random from bag
     */
    UUID pick();

    /**
     * Add all marbles from another object mapping marble -> quantity
     *
     * @param phase of selection
     * @param toAdd map of marble id to quantity
     */
    void addAll(int phase, const std::map<UUID, int>& toAdd);

    /**
     * Add one marble to the bag; increments the count of this marble +1
     *
     * @param phase of selection
     * @param id    of the marble to add
     */
    void add(int phase, const UUID& id);

    /**
     * Add a quantity of marbles to the bag; increments the count of the specified marble by the specified quantity.
     *
     * @param phase of selection
     * @param id    of the marble to add
     * @param qty   quantity of this marble to add
     */
    void add(int phase, const UUID& id, int qty);

    /**
     * Number of marbles in the bag
     *
     * @return {number}
     */
    int size();

    /**
     * Display as string
     */
    std::string toString();

    /**
     * @return true if the marble bag is completely empty
     */
    bool isEmpty();

    /**
     * @return true if there are any marbles in the bag
     */
    bool isPresent();

    /**
     * Pick a marble from the specified phase
     *
     * @param phase from which to pick a marble
     * @return marble if available
     */
    std::optional<UUID> pickPhase(int phase);

    /**
     * Quick pick an integer from 0 to total - 1
     * @return  The picked integer
     */
    static int quickPick(int total);

    /**
     * Group of marbles with a given id
     */
    class Group {
    public:
      UUID id;
      int from;
      int to;

      Group(UUID id, int from, int to);
    };

  private:
    std::map<int/*Phase*/, std::map<UUID/*Id*/, int/*Qty*/>> marbles;
    std::random_device rd;
    std::mt19937 gen{rd()};

  };

}// namespace XJ

#endif// XJMUSIC_UTIL_MARBLE_BAG_H