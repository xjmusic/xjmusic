// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

#include <random>
#include <optional>
#include <utility>


#include "xjmusic/fabricator/MarbleBag.h"
#include "xjmusic/fabricator/FabricationException.h"

using namespace XJ;

/**
 * Construct an empty marble bag
 */
MarbleBag MarbleBag::empty() {
  return {};
}

/**
 * @return {std::string} marble picked at random from bag
 */
UUID MarbleBag::pick() {
  std::vector<int> phases;
  for (const auto &entry: marbles) {
    phases.push_back(entry.first);
  }
  std::sort(phases.begin(), phases.end());

  std::optional<UUID> pick;
  for (const auto &phase: phases) {
    pick = pickPhase(phase);
    if (pick.has_value())
      return pick.value();
  }

  throw FabricationException(std::to_string(size()) + " available; no marble picked!");
}

/**
 * Add all marbles from another object mapping marble -> quantity
 *
 * @param phase of selection
 * @param toAdd map of marble id to quantity
 */
void MarbleBag::addAll(int phase, const std::map<UUID, int> &toAdd) {
  for (const auto &entry: toAdd)
    add(phase, entry.first, entry.second);
}

/**
 * Add one marble to the bag; increments the count of this marble +1
 *
 * @param phase of selection
 * @param id    of the marble to add
 */
void MarbleBag::add(int phase, const UUID &id) {
  add(phase, id, 1);
}

/**
 * Add a quantity of marbles to the bag; increments the count of the specified marble by the specified quantity.
 *
 * @param phase of selection
 * @param id    of the marble to add
 * @param qty   quantity of this marble to add
 */
void MarbleBag::add(int phase, const UUID &id, int qty) {
  if (marbles.find(phase) == marbles.end())
    marbles[phase] = std::map<UUID, int>();
  if (marbles[phase].find(id) != marbles[phase].end())
    marbles[phase][id] += qty;
  else
    marbles[phase][id] = qty;
}

/**
 * Number of marbles in the bag
 *
 * @return {number}
 */
int MarbleBag::size() {
  int total = 0;
  for (const auto &phase: marbles) {
    for (const auto &marble: phase.second) {
      total += marble.second;
    }
  }
  return total;
}

/**
 * Display as string
 */
std::string MarbleBag::toString() {
  std::string result;
  for (const auto &phase: marbles) {
    std::string phaseStr = "Phase" + std::to_string(phase.first) + "[";
    for (const auto &marble: phase.second) {
      phaseStr += marble.first + ":" + std::to_string(marble.second) + ", ";
    }
    if (!phase.second.empty()) {
      phaseStr.pop_back(); // remove last comma
      phaseStr.pop_back(); // remove last space
    }
    phaseStr += "]";
    result += phaseStr + ", ";
  }
  if (!marbles.empty()) {
    result.pop_back(); // remove last comma
    result.pop_back(); // remove last space
  }
  return result;
}

/**
 * @return true if the marble bag is completely empty
 */
bool MarbleBag::isEmpty() {
  return 0 == size();
}

/**
 * @return true if there are any marbles in the bag
 */
bool MarbleBag::isPresent() {
  return 0 < size();
}

/**
 * Pick a marble from the specified phase
 *
 * @param phase from which to pick a marble
 * @return marble if available
 */
std::optional<UUID> MarbleBag::pickPhase(int phase) {
  int total = 0;
  std::vector<Group> blocks;

  for (const auto &entry: marbles[phase]) {
    if (entry.second > 0) {
      blocks.emplace_back(entry.first, total, total + entry.second);
      total += entry.second;
    }
  }

  if (blocks.empty())
    return std::nullopt;

  if (total == 0)
    return blocks[0].id;

  std::uniform_int_distribution<> distrib(0, total - 1);
  int pickIdx = distrib(gen);

  for (const Group &block: blocks) {
    if (pickIdx >= block.from && pickIdx < block.to)
      return block.id;
  }

  return std::nullopt;
}

MarbleBag::Group::Group(UUID id, int from, int to) {
  this->id = std::move(id);
  this->from = from;
  this->to = to;
}

int MarbleBag::quickPick(int total) {
  if (1 == total)
    return 0;
  if (0 == total)
    throw FabricationException("Cannot pick from empty set");
  std::random_device rd;
  std::mt19937 gen(rd());
  std::uniform_int_distribution<> distrib(0, total - 1);
  return distrib(gen);
}
