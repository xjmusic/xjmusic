// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef XJMUSIC_TEST_HELPER_ASSERTION_HELPERS_H
#define XJMUSIC_TEST_HELPER_ASSERTION_HELPERS_H

#include "gtest/gtest.h"

#include "xjmusic/music/Note.h"
#include "xjmusic/music/PitchClass.h"
#include "xjmusic/music/Root.h"

namespace XJ {

/**
 * Expect a runtime error with a specific message
 */
#define EXPECT_THROW_WITH_MESSAGE(statement, expected_message) \
    EXPECT_THROW({                                             \
        try {                                                  \
            statement;                                         \
        }                                                      \
        catch (const std::runtime_error &e) {                  \
            EXPECT_STREQ(expected_message, e.what());          \
            throw;                                             \
        }                                                      \
    }, std::runtime_error)                                     \

  class TestHelpers {
  public:

    /**
     * Assert the pitch class and octave of a Note object
     * @param expect  expected note
     * @param actual  actual note
     */
    static void assertNote(const std::string &expect, const Note &actual);

  };

} // namespace XJ

#endif //XJMUSIC_TEST_HELPER_ASSERTION_HELPERS_H