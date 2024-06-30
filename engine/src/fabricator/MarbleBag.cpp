// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

#include <random>
#include <optional>
#include <utility>


#include "xjmusic/fabricator/MarbleBag.h"
#include "xjmusic/fabricator/FabricationException.h"

using namespace XJ;

/**
 * @return {std::string} marble picked at random from bag
 */
UUID MarbleBag::pick() {
  std::vector<int> phases;
  for (const auto &[phase, marbles]: marbles) {
    phases.push_back(phase);
  }
  std::sort(phases.begin(), phases.end());

  for (const auto &phase: phases) {
    std::optional<UUID> pick = pickPhase(phase);
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
void MarbleBag::addAll(const int phase, const std::map<UUID, int> &toAdd) {
  for (const auto &[id, qty]: toAdd)
    add(phase, id, qty);
}

/**
 * Add one marble to the bag; increments the count of this marble +1
 *
 * @param phase of selection
 * @param id    of the marble to add
 */
void MarbleBag::add(const int phase, const UUID &id) {
  add(phase, id, 1);
}

/**
 * Add a quantity of marbles to the bag; increments the count of the specified marble by the specified quantity.
 *
 * @param phase of selection
 * @param id    of the marble to add
 * @param qty   quantity of this marble to add
 */
void MarbleBag::add(const int phase, const UUID &id, const int qty) {
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
int MarbleBag::size() const {
  int total = 0;
  for (const auto &[phase, marbles]: marbles) {
    for (const auto &[id, qty]: marbles) {
      total += qty;
    }
  }
  return total;
}

/**
 * Display as string
 */
std::string MarbleBag::toString() const {
  std::string result;
  for (const auto &[phase, marbleMap]: marbles) {
    std::string phaseStr = "Phase" + std::to_string(phase) + "[";
    for (const auto &[id, qty]: marbleMap) {
      phaseStr += id + ":" + std::to_string(qty) + ", ";
    }
    if (!marbleMap.empty()) {
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
bool MarbleBag::empty() const {
  return 0 == size();
}

/**
 * @return true if there are any marbles in the bag
 */
bool MarbleBag::isPresent() const {
  return 0 < size();
}

/**
 * Pick a marble from the specified phase
 *
 * @param phase from which to pick a marble
 * @return marble if available
 */
std::optional<UUID> MarbleBag::pickPhase(const int phase) {
  int total = 0;
  std::vector<Group> blocks;

  for (const auto &[id, qty]: marbles[phase]) {
    if (qty > 0) {
      blocks.emplace_back(id, total, total + qty);
      total += qty;
    }
  }

  if (blocks.empty())
    return std::nullopt;

  if (total == 0)
    return blocks[0].id;

  std::uniform_int_distribution<> distrib(0, total - 1);
  const int pickIdx = distrib(gen);

  for (const Group &block: blocks) {
    if (pickIdx >= block.from && pickIdx < block.to)
      return block.id;
  }

  return std::nullopt;
}

MarbleBag::Group::Group(UUID id, const int from, const int to) {
  this->id = std::move(id);
  this->from = from;
  this->to = to;
}

int MarbleBag::quickPick(const int total) {
  if (1 == total)
    return 0;
  if (0 == total)
    throw FabricationException("Cannot pick from empty set");
  std::random_device rd;
  std::mt19937 gen(rd());
  std::uniform_int_distribution<> distrib(0, total - 1);
  return distrib(gen);
}

bool MarbleBag::quickBooleanChanceOf(const float probability) {
  if (probability < 0 || probability >= 1)
    throw FabricationException("Probability must be 0 <= n < 1");
  std::random_device rd;
  std::mt19937 gen(rd());
  std::uniform_real_distribution<> distrib(0, 1);
  return distrib(gen) < probability;
}

MarbleBag::MarbleBag(const MarbleBag &other) {
  marbles = other.marbles;
}


