// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef XJMUSIC_WORK_FABRICATION_SETTINGS_H
#define XJMUSIC_WORK_FABRICATION_SETTINGS_H

#include "xjmusic/fabricator/Fabricator.h"
#include "xjmusic/content/Template.h"

namespace XJ
{

	class WorkSettings
	{
	public:
		Fabricator::ControlMode controlMode = Fabricator::ControlMode::Auto;
		int						craftAheadSeconds = 20;
		int						dubAheadSeconds = 10;
		int						deadlineSeconds = 1;
		long					persistenceWindowSeconds = 3600;
		std::string				toString() const;
	};

} // namespace XJ

#endif // XJMUSIC_WORK_FABRICATION_SETTINGS_H
