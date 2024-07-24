// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
#ifndef XJMUSIC_CSV_UTILS_H
#define XJMUSIC_CSV_UTILS_H

#include <map>
#include <string>
#include <vector>

#include "StringUtils.h"

namespace XJ {

  class CsvUtils {
  public:
    /**
     * Split a CSV string into a vector of strings
     * @param csv  CSV string
     * @return     vector of strings
     */
    static std::vector<std::string> split(const std::string &csv);

    /**
     * Split a CSV string into a vector of proper slugs
     * @param csv  CSV string
     * @return     vector of proper slugs
     */
    static std::vector<std::string> splitProperSlug(const std::string &csv);

    /**
     * Join a list of items
     * @param parts
     * @return
     */
    static std::string join(const std::vector<std::string> &parts);

    /**
     * Join a set of items in alphabetical order
     * @param parts
     * @return
     */
    static std::string join(const std::set<std::string> &parts);

    /**
     Join a list of items properly, e.g. "One, Two, Three, and Four"

     @param ids             to write
     @param beforeFinalItem text after last comma
     @return CSV of ids
     */
    static std::string prettyFrom(const std::vector<std::string> &ids, const std::string &beforeFinalItem);

    /**
     Get a CSV string of key=value properties

     @param properties key=value
     @return CSV string
     */
    static std::string from(const std::map<std::string, std::string> &properties);

    /**
    Format a list of items as a proper CSV list, with the given final separator word "and"

    @param items list of items
    @return formatted list like "One, Two, and Three"
    */
    static std::string toProperCsvAnd(const std::vector<std::string> &items);

    /**
    Format a list of items as a proper CSV list, with the given final separator word "or"

    @param items list of items
    @return formatted list like "One, Two, or Three"
    */
    static std::string toProperCsvOr(const std::vector<std::string> &items);

    /**
    Format a list of items as a proper CSV list, with the given final separator word

    @param items          list of items
    @param finalSeparator final separator word
    @return formatted list like "One, Two, ___ Three"
    */
    static std::string toProperCsv(std::vector<std::string> items, const std::string &finalSeparator);
  };

}// namespace XJ

#endif// XJMUSIC_CSV_UTILS_H