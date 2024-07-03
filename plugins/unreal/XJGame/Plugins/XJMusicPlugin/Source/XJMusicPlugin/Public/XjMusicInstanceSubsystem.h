// Fill out your copyright notice in the Description page of Project Settings.

#pragma once

#include "CoreMinimal.h"
#include "Subsystems/GameInstanceSubsystem.h"

#include <gmock/gmock.h>
#include <gtest/gtest.h>

#include "xjmusic/craft/Craft.h"
#include "xjmusic/fabricator/FabricatorFactory.h"
#include "xjmusic/fabricator/SegmentUtils.h"
#include "xjmusic/work/WorkManager.h"
#include <xjmusic/xjmusic.h>

#include "XjMusicInstanceSubsystem.generated.h"
using namespace XJ;

UCLASS()
class XJMUSICPLUGIN_API UXjMusicInstanceSubsystem : public UGameInstanceSubsystem
{
	GENERATED_BODY()
	
public:
	virtual void Initialize(FSubsystemCollectionBase& Collection) override;
	virtual void Deinitialize() override;

private:
	int MARATHON_NUMBER_OF_SEGMENTS = 50;
	long MICROS_PER_CYCLE = 1000000;
	long long MAXIMUM_TEST_WAIT_SECONDS = 10 * MARATHON_NUMBER_OF_SEGMENTS;
	long long MILLIS_PER_SECOND = 1000;
	int GENERATED_FIXTURE_COMPLEXITY = 3;
	long long startTime = EntityUtils::currentTimeMillis();
	SegmentEntityStore* store = nullptr;
	ContentEntityStore* content = nullptr;
	WorkManager* work = nullptr;
};
