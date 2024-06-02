// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <gtest/gtest.h>
#include <string>

#include "xjmusic/content/TemplateBinding.h"

using namespace Content;

TEST(TemplateBindingTest, FieldValues) {
  TemplateBinding subject;

  subject.id = "7ec2d282-d481-4fee-b57b-082e90284102";
  subject.templateId = "0c39f908-5eb3-4c94-a8c6-ac87ec02f7e9";
  subject.type = TemplateBinding::Type::Program;
  subject.targetId = "f2029814-7e4a-4bfa-a281-33be4f7d00ca";

  ASSERT_EQ("7ec2d282-d481-4fee-b57b-082e90284102", subject.id);
  ASSERT_EQ("0c39f908-5eb3-4c94-a8c6-ac87ec02f7e9", subject.templateId);
  ASSERT_EQ(TemplateBinding::Type::Program, subject.type);
  ASSERT_EQ("f2029814-7e4a-4bfa-a281-33be4f7d00ca", subject.targetId);
}

TEST(TemplateBindingTest, ParseTemplateBindingType) {
  ASSERT_EQ(TemplateBinding::Type::Library, TemplateBinding::parseType("Library"));
  ASSERT_EQ(TemplateBinding::Type::Program, TemplateBinding::parseType("Program"));
  ASSERT_EQ(TemplateBinding::Type::Instrument, TemplateBinding::parseType("Instrument"));
}

TEST(TemplateBindingTest, ToStringTemplateBindingType) {
  ASSERT_EQ("Library", TemplateBinding::toString(TemplateBinding::Type::Library));
  ASSERT_EQ("Program", TemplateBinding::toString(TemplateBinding::Type::Program));
  ASSERT_EQ("Instrument", TemplateBinding::toString(TemplateBinding::Type::Instrument));
}
