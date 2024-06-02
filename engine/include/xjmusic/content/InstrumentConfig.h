
// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef XJMUSIC_INSTRUMENT_CONFIG_H
#define XJMUSIC_INSTRUMENT_CONFIG_H

#include <string>
#include <vector>

#include "xjmusic/util/ConfigParser.h"

#include "Instrument.h"

namespace Content {

  class InstrumentConfig : public Util::ConfigParser {
  private:
    static const std::string DEFAULT;

  public:
    explicit InstrumentConfig();

    explicit InstrumentConfig(const Instrument &source);

    explicit InstrumentConfig(const std::string &input);

    bool isAudioSelectionPersistent;
    bool isMultiphonic;
    bool isOneShot;
    bool isOneShotCutoffEnabled;
    bool isTonal;
    int releaseMillis;
    std::vector<std::string> oneShotObserveLengthOfEvents;

    /**
   * Format the InstrumentConfig as a HOCON string
   * @return  The HOCON string
   */
    [[nodiscard]] std::string toString() const;

    /**
   * Get the default InstrumentConfig as a HOCON string
   */
    [[nodiscard]] static std::string getDefaultString();
  };

}// namespace Content

#endif//XJMUSIC_INSTRUMENT_CONFIG_H
