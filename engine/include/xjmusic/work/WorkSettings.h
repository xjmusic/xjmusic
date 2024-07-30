// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef XJMUSIC_WORK_FABRICATION_SETTINGS_H
#define XJMUSIC_WORK_FABRICATION_SETTINGS_H

#include "xjmusic/fabricator/Fabricator.h"

#include <xjmusic/util/ValueUtils.h>

namespace XJ
{

	class WorkSettings
	{
	public:
		Fabricator::ControlMode controlMode = Fabricator::ControlMode::Auto;
		int						craftAheadMicros = 20 * ValueUtils::MICROS_PER_SECOND;
		int						dubAheadMicros = 10 * ValueUtils::MICROS_PER_SECOND;
		int						deadlineMicros = 1 * ValueUtils::MICROS_PER_SECOND;
		long					persistenceWindowSeconds = 3600;
		std::string				toString() const;
	};

} // namespace XJ

#endif // XJMUSIC_WORK_FABRICATION_SETTINGS_H
