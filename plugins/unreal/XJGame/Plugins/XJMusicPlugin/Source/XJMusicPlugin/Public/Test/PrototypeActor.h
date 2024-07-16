// Fill out your copyright notice in the Description page of Project Settings.

#pragma once

#include "CoreMinimal.h"
#include "GameFramework/Actor.h"

#include <xjmusic/work/WorkManager.h>
#include <xjmusic/fabricator/SegmentUtils.h>
#include <xjmusic/Engine.h>
#include <xjmusic/fabricator/Fabricator.h>

#include "PrototypeActor.generated.h"

using namespace XJ;

class AudioPlayer
{
public:
	unsigned long long StartTime = 0;
	unsigned long long EndTime = -1;

	bool bIsPlaying = false;

	FString Name;
	FString Id;
};

UCLASS()
class XJMUSICPLUGIN_API APrototypeActor : public AActor
{
	GENERATED_BODY()
	
public:	
	APrototypeActor();

	virtual void Tick(float DeltaTime) override;

protected:
	virtual void BeginPlay() override;

	virtual void BeginDestroy() override;

	void RunXjOneCycleTick(const float DeltaTime);

	bool isWithinTimeLimit()
	{
		if (MAXIMUM_TEST_WAIT_SECONDS * MILLIS_PER_SECOND > EntityUtils::currentTimeMillis() - startTime)
		{
			return true;
		}

		UE_LOG(LogTemp, Error, TEXT("EXCEEDED TEST TIME LIMIT OF %d SECONDS"), MAXIMUM_TEST_WAIT_SECONDS)

			return false;
	}

	bool hasSegmentsDubbedPastMinimumOffset() const
	{
		const auto segment = SegmentUtils::getLastCrafted(engine->getSegmentStore()->readAllSegments());
		return segment.has_value() && segment.value()->id >= MARATHON_NUMBER_OF_SEGMENTS;
	}

private:
	int MARATHON_NUMBER_OF_SEGMENTS = 50;
	long MICROS_PER_CYCLE = 1000000;
	long long MAXIMUM_TEST_WAIT_SECONDS = 10 * MARATHON_NUMBER_OF_SEGMENTS;
	long long MILLIS_PER_SECOND = 1000;
	long MICROS_PER_MILLI = 1000;
	long MICROS_PER_SECOND = MICROS_PER_MILLI * MILLIS_PER_SECOND;
	int GENERATED_FIXTURE_COMPLEXITY = 3;
	long long startTime = EntityUtils::currentTimeMillis();

	SegmentEntityStore* store = nullptr;

	ContentEntityStore* content = nullptr;
	WorkManager* work = nullptr;

	Engine* engine = nullptr;

	unsigned long long atChainMicros = 0;

	TMap<unsigned long long, AudioPlayer> AudioLookup;

	TArray<FString> PlayingNames;

	TArray<AudioPlayer> ActiveAudios;

	class UXjMusicInstanceSubsystem* XjMusicInstanceSubsystem = nullptr;
};
