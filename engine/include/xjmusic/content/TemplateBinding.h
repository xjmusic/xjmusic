// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef TEMPLATE_BINDING_H
#define TEMPLATE_BINDING_H

#include <string>
#include <utility>
#include <set>

#include "xjmusic/util/EntityUtils.h"
#include "ContentEntity.h"

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
    TemplateBinding::Type type{};
    UUID targetId;

    /**
     * Represent a TemplateBinding as a string
     * @return  The string representation of the TemplateBinding
     */
    std::string toString() const;

    /**
     * Parse the TemplateBinding Type enum value from a string
     * @param value  The string to parse
     * @return      The TemplateBinding Type enum value
     */
    static TemplateBinding::Type parseType(const std::string &value);

    /**
     * Convert a TemplateBinding Type enum value to a string
     * @param type  The TemplateBinding Type enum value
     * @return      The string representation of the TemplateBinding Type
     */
    static std::string toString(const TemplateBinding::Type &type);

    /**
     * Convert a set of TemplateBinding pointers to a string
     * @param templateBindings  The set of TemplateBinding pointers
     * @return                The string representation of the set of TemplateBinding pointers
     */
    static std::string toPrettyCsv(const std::set<const TemplateBinding *>& templateBindings);
  };

}// namespace XJ

#endif//TEMPLATE_BINDING_H
