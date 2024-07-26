// Fill out your copyright notice in the Description page of Project Settings.

#pragma once

#include "CoreMinimal.h"
#include "EngineBase.h"

#include <xjmusic/work/WorkManager.h>
#include <xjmusic/fabricator/SegmentUtils.h>
#include <xjmusic/Engine.h>

using namespace XJ;

class TXjMainEngine : public TEngineBase
{

public:

	virtual void Setup(const FString& PathToProject) override;

	virtual void Shutdown() override;

	virtual TSet<FAudioPlayer> RunCycle(const uint64 ChainMicros) override;

private:
	TUniquePtr<Engine> XjEngine;

	int MILLIS_PER_SECOND = 1000;
	int MICROS_PER_MILLI = 1000;
	int GENERATED_FIXTURE_COMPLEXITY = 3;

	uint64 MICROS_PER_SECOND = MICROS_PER_MILLI * MILLIS_PER_SECOND;
};