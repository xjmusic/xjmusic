// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef XJMUSIC_VALUE_UTILS_H
#define XJMUSIC_VALUE_UTILS_H

#include <optional>
#include <random>
#include <set>
#include <regex>
#include <string>
#include <vector>

#include "EntityUtils.h"

namespace XJ {

  class ValueUtils {
  private:
    static std::regex isIntegerRgx;
    static std::string K;
    static float entityPositionDecimalPlaces;
    static float roundPositionMultiplier;
    static std::random_device rd;
    static std::mt19937 gen;

  public:
    static long MILLIS_PER_SECOND;
    static long MICROS_PER_MILLI;
    static long NANOS_PER_MICRO;
    static long MICROS_PER_SECOND;
    static float MICROS_PER_SECOND_FLOAT;
    static long NANOS_PER_SECOND;
    static long SECONDS_PER_MINUTE;
    static long MICROS_PER_MINUTE;
    static long MINUTES_PER_HOUR;
    static long HOURS_PER_DAY;
    static long SECONDS_PER_HOUR;
    static long SECONDS_PER_DAY;

    /**
     * Return the first value if it's non-null, else the second
     * @param d1 to check if non-null and return
     * @param d2 to default to, if s1 is null
     * @return s1 if non-null, else s2
     */
    static float eitherOr(float d1, float d2);

    /**
     * Return the first value if it's non-null, else the second
     * @param s1 to check if non-null and return
     * @param s2 to default to, if s1 is null
     * @return s1 if non-null, else s2
     */
    static std::string eitherOr(std::string s1, std::string s2);

    /**
     * Divide a set of integers by a float and return the divided set
     * @param divisor   to divide by
     * @param originals to divide
     * @return divided originals
     */
    static std::set<int> dividedBy(float divisor, const std::set<int> &originals);

    /**
     * Calculate ratio (of 0 to 1) within a zero-to-N limit
     * @param value to calculate radio of
     * @param limit N where ratio will be calculated based on zero-to-N
     * @return ratio between 0 and 1
     */
    static float ratio(float value, float limit);

    /**
     * True if input string is an integer
     * @param raw text to check if it's an integer
     * @return true if it's an integer
     */
    static bool isInteger(const std::string &raw);

    /**
     * Round a value to N decimal places.
     * Architect wants to limit the floating point precision of chord and event position, in order to limit obsession over the position of things. https://www.pivotaltracker.com/story/show/154976066
     * @param value to round
     * @return rounded position
     */
    static float limitDecimalPrecision(float value);

    /**
     * Get the "kilos" representation of an integer, as in 128k for 128000
     * @param value for which to get kilos
     * @return kilos representation
     */
    static std::string k(int value);

    /**
     * Get a random string from the collection
     * @param from which to get random string
     * @return random string from collection
     */
    static std::string randomFrom(std::vector<std::string> from);

    /**
     * Get N number of the member strings from the collection.
     * Don't repeat a choice.
     * @param from which to get random strings
     * @param num  number of strings to get
     * @return random strings from collection
     */
    static std::vector<std::string> randomFrom(std::vector<std::string> from, int num);

    /**
     * Greatest common denominator of two numbers
     * @param a from which to compute
     * @param b from which to compute
     * @return greatest common denominator
     */
    static long gcd(long a, long b);

    /**
     * Get the values (filtered from the given set of test factors) which are factors of the target value
     * @param target      which we'll test values against
     * @param testFactors values to test
     * @return values that are indeed a factor of the target value
     */
    static std::vector<int> factors(long target, std::vector<int> testFactors);

    /**
     * Get the smallest subdivision within the total, e.g.
     * --- [12,3] = 4
     * --- [12,4] = 3
     * --- [16,4] = 4
     * --- [24,3] = 4
     * --- [24,4] = 3
     * --- [48,3] = 4
     * --- [48,4] = 3
     * --- [64,4] = 4
     * @param numerator   from which to compute
     * @param denominator from which to compute
     */
    static int subDiv(int numerator, int denominator);

    /**
     * Round down to a multiple of the given factor
     * @param factor of which to get a multiple
     * @param value  source
     * @return floor of value
     */
    static int multipleFloor(int factor, float value);

    /**
     * Interpolate a value between the floor and ceiling
     * @param floor      bottom value
     * @param ceiling    top value
     * @param position   between 0 and 1 of value to interpolate between the floor and ceiling
     * @param multiplier of value (above ceiling)
     * @return interpolated value
     */
    static float interpolate(float floor, float ceiling, float position, float multiplier);

    /**
     * Enforce a maximum
     * @param value actual
     * @throws runtime_error if value greater than allowable
     */
    static void enforceMaxStereo(int value);

    /**
     * Get the key from a map, based on the highest value stored
     * @param map of key-value pairs
     * @return key of the highest value
     */
    static std::optional<UUID> getKeyOfHighestValue(const std::map<UUID, int> &map);

    /**
     * Round value to the nearest positive multiple of N
     */
    static int roundToNearest(int N, int value);

    /**
     * Remove some number of ids from the list
     * @param fromIds to begin with
     * @param count   number of ids to add
     * @return list including added ids
     */
    static std::vector<UUID> withIdsRemoved(std::vector<UUID> fromIds, int count);

    /**
     * Get string value of int, or empty if zero
     * @param value to translate
     * @return non-zero value, or empty
     */
    static std::string emptyZero(int value);

    /**
     * Get the last N values from a list
     * @param num  of entries
     * @param list of all entries
     * @return last N entries from the list
     */
    static std::vector<std::string> last(int num, std::vector<std::string> list);

  };

}

#endif //XJMUSIC_VALUE_UTILS_H
