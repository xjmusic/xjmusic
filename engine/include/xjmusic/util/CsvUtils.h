// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

#include <algorithm>
#include <map>
#include <sstream>
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
     * Join a set of items' toPrettyCsv() values properly, e.g. "One, Two, Three, and Four"
     * @param parts
     * @return
     */
    static std::string join(const std::vector<std::string> &parts);

    /**
     Join a set of items properly, e.g. "One, Two, Three, and Four"

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

  };

} // namespace XJ