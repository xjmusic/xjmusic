// Fill out your copyright notice in the Description page of Project Settings.

#pragma once

#include "CoreMinimal.h"
#include "Subsystems/GameInstanceSubsystem.h"

#include "../_helper/ContentFixtures.h"

#include "xjmusic/craft/Craft.h"
#include "xjmusic/fabricator/FabricatorFactory.h"
#include "xjmusic/work/WorkManager.h"
#include <xjmusic/xjmusic.h>
#include <xjmusic/fabricator/SegmentUtils.h>

#include "XjMusicInstanceSubsystem.generated.h"

using namespace XJ;

UCLASS()
class XJMUSICPLUGIN_API UXjMusicInstanceSubsystem : public UGameInstanceSubsystem
{
	GENERATED_BODY()
	
public:
	virtual void Initialize(FSubsystemCollectionBase& Collection) override;
	virtual void Deinitialize() override;

	void RunXjOneCycleTick();

	FString RetriveProjectsInfo();


	USoundWave* GetSoundWaveFromFile(const FString& filePath);
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
	ContentFixtures* fake = nullptr;

	unsigned long long atChainMicros = 0;

	TArray<FString> ImportedAudioFiles;

	TArray<FSoftObjectPath> AudioFilesReferences;

	bool isWithinTimeLimit() const
	{
		if (MAXIMUM_TEST_WAIT_SECONDS * MILLIS_PER_SECOND > EntityUtils::currentTimeMillis() - startTime)
		{
			return true;
		}

		UE_LOG(LogTemp, Error, TEXT("EXCEEDED TEST TIME LIMIT OF %d SECONDS"), MAXIMUM_TEST_WAIT_SECONDS);

		return false;
	}

	bool hasSegmentsDubbedPastMinimumOffset()
	{
		const auto segment = XJ::SegmentUtils::getLastCrafted(store->readAllSegments());
		return segment.has_value() && segment.value()->id >= MARATHON_NUMBER_OF_SEGMENTS;
	}
};