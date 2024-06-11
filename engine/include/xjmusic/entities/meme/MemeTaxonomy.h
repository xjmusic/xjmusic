
// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef XJMUSIC_ENTITIES_MEME_TAXONOMY_H
#define XJMUSIC_ENTITIES_MEME_TAXONOMY_H

#include <map>
#include <string>
#include <variant>

#include "xjmusic/util/StringUtils.h"

namespace XJ {

  /**
   * One category of memes in the taxonomy
   */
  class MemeCategory {
  private:
    std::string name;
    std::vector<std::string> memes;
    static std::regex rgx;
    static std::string MEME_SEPARATOR;
    static std::string KEY_NAME;
    static std::string KEY_MEMES;
    static std::string DEFAULT_CATEGORY_NAME;

    static std::vector<std::string>
    parseMemeList(std::map<std::string, std::variant<std::string, std::vector<std::string>>> &data);

  public:
    MemeCategory() = default;

    /**
     * Construct a category from a raw string like "CATEGORY[MEME1, MEME2]"
     * @param raw  The raw string
     */
    explicit MemeCategory(const std::string *raw);

    /**
     * Construct a category from a map like {name: "CATEGORY", memes: ["MEME1", "MEME2"]}
     * @param data  The map
     */
    explicit MemeCategory(std::map<std::string, std::variant<std::string, std::vector<std::string>>> &data);

    /**
     * Get the name of the category
     * @return  The name of the category
     */
    std::string getName();

    /**
     * Get the list of memes
     * @return  The list of memes
     */
    std::vector<std::string> getMemes();

    /**
     * Whether a list of memes is allowed because no more than one matches the category's memes
     * @param targets  The list of memes to check
     * @return         True if the list is allowed
     */
    bool isAllowed(std::vector<std::string> &targets) const;

    /**
     * Whether the category has memes
     * @return  True if the category has memes
     */
    bool hasMemes();

    /**
     * Convert the category to a map like {name: "CATEGORY", memes: ["MEME1", "MEME2"]}
     * @return  The map
     */
    [[nodiscard]] std::map<std::string, std::variant<std::string, std::vector<std::string>>> toMap() const;

    /**
     * Convert the category to a string like "CATEGORY[MEME1, MEME2]"
     * @return  The string
     */
    [[nodiscard]] std::string toString() const;
  };

  /**
   * TemplateConfig has Meme categories
   * https://www.pivotaltracker.com/story/show/181801646
   * <p>
   * <p>
   * A template configuration has a field called `memeTaxonomy` which defines the taxonomy of memes.
   * <p>
   * For example, this might look like
   * <p>
   * ```
   * memeTaxonomy=CITY[CHICAGO,DENVER,PHILADELPHIA]
   * ```
   * <p>
   * That would tell XJ about the existence of a meme category called City with values `CHICAGO`, `DENVER`, and `PHILADELPHIA`. And these would function as exclusion like numeric memes, e.g. after content having `CHICAGO` is chosen, we can choose nothing with `DENVER` or `PHILADELPHIA`.
   */
  class MemeTaxonomy {
  private:
    std::vector<MemeCategory> categories;
    static char CATEGORY_SEPARATOR;

  public:
    MemeTaxonomy() = default;

    /**
     * Construct a taxonomy from a raw string like "CATEGORY1[MEME1, MEME2],CATEGORY2[MEME3, MEME4]"
     * @param raw  The raw string
     */
    explicit MemeTaxonomy(const std::string &raw);

    /**
     * Construct a taxonomy from a list of maps like [{name: "CATEGORY1", memes: ["MEME1", "MEME2"]}, {name: "CATEGORY2", memes: ["MEME3", "MEME4"]}]
     * @param data  The list of maps
     */
    explicit MemeTaxonomy(
        std::vector<std::map<std::string, std::variant<std::string, std::vector<std::string>>>> &data);

    /**
     * Convert the taxonomy to a string like "CATEGORY1[MEME1, MEME2],CATEGORY2[MEME3, MEME4]"
     * @return  The string
     */
    [[nodiscard]] std::string toString() const;

    /**
     * Convert the taxonomy to a list of maps like [{name: "CATEGORY1", memes: ["MEME1", "MEME2"]}, {name: "CATEGORY2", memes: ["MEME3", "MEME4"]}]
     * @return  The list of maps
     */
    std::vector<std::map<std::string, std::variant<std::string, std::vector<std::string>>>> toList();

    /**
     * Get the categories
     * @return  The categories
     */
    std::vector<MemeCategory> getCategories();

    /**
     * Whether a list of memes is allowed because they are allowed by all taxonomy categories
     * @param memes  The list of memes to check
     * @return       True if the list is allowed
     */
    bool isAllowed(std::vector<std::string> memes);
  };

}// namespace XJ

#endif//XJMUSIC_ENTITIES_MEME_TAXONOMY_H
