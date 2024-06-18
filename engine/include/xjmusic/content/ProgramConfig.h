
// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef XJMUSIC_PROGRAM_CONFIG_H
#define XJMUSIC_PROGRAM_CONFIG_H

#include "xjmusic/util/ConfigParser.h"

#include "Program.h"

namespace XJ {

  class ProgramConfig : public ConfigParser {
  private:
    static const std::string DEFAULT;

  public:
    explicit ProgramConfig();

    explicit ProgramConfig(const Program &source);

    explicit ProgramConfig(const std::string &input);

    bool doPatternRestartOnChord;
    int barBeats;
    int cutoffMinimumBars;

    /**
     * Format the ProgramConfig as a HOCON string
     * @return  The HOCON string
     */
    [[nodiscard]] std::string toString() const;

    /**
     * Get the default ProgramConfig as a HOCON string
     */
    [[nodiscard]] static std::string getDefaultString();

    ProgramConfig(const Program *pProgram);
  };

}// namespace XJ

#endif//XJMUSIC_PROGRAM_CONFIG_H
