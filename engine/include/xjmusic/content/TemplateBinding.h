// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef XJMUSIC_TEMPLATE_BINDING_H
#define XJMUSIC_TEMPLATE_BINDING_H

#include <set>
#include <string>

#include "ContentEntity.h"
#include "xjmusic/util/EntityUtils.h"

namespace XJ {

  class TemplateBinding : public ContentEntity {
  public:
    enum Type {
      Library,
      Program,
      Instrument
    };

    TemplateBinding() = default;

    UUID templateId;
    Type type{};
    UUID targetId;

    /**
     * Represent a TemplateBinding as a string
     * @return  The string representation of the TemplateBinding
     */
    std::string toString() const;

    /**
     * Parse the TemplateBinding EType enum value from a string
     * @param value  The string to parse
     * @return      The TemplateBinding EType enum value
     */
    static Type parseType(const std::string &value);

    /**
     * Convert a TemplateBinding EType enum value to a string
     * @param type  The TemplateBinding EType enum value
     * @return      The string representation of the TemplateBinding EType
     */
    static std::string toString(const Type &type);

    /**
     * Convert a set of TemplateBinding pointers to a string
     * @param templateBindings  The set of TemplateBinding pointers
     * @return                The string representation of the set of TemplateBinding pointers
     */
    static std::string toPrettyCsv(const std::set<const TemplateBinding *> &templateBindings);
  };

  /**
   * Parse a TemplateBinding from a JSON object
   * @param json  input
   * @param entity  output
   */
  inline void from_json(const json &json, TemplateBinding &entity) {
    EntityUtils::setRequired(json, "id", entity.id);
    EntityUtils::setRequired(json, "templateId", entity.templateId);
    entity.type = TemplateBinding::parseType(json.at("type").get<std::string>());
    EntityUtils::setRequired(json, "targetId", entity.targetId);
  }

}// namespace XJ

#endif//XJMUSIC_TEMPLATE_BINDING_H
