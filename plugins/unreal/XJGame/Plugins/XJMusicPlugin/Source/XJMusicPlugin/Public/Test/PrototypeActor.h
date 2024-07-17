// Fill out your copyright notice in the Description page of Project Settings.

#pragma once

#include "CoreMinimal.h"
#include "GameFramework/Actor.h"

#include <xjmusic/work/WorkManager.h>
#include <xjmusic/fabricator/SegmentUtils.h>
#include <xjmusic/Engine.h>

#include "PrototypeActor.generated.h"

using namespace XJ;

struct FAudioPlayer
{
	float StartTime = 0.0f;
	float EndTime = 0.0f;

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

	UPROPERTY(EditInstanceOnly, Category = "XjMusic", meta = (ToolTip = "Path to XJ project folder, with trailing slash"))
	FString XjProjectFolder;
	
	UPROPERTY(EditInstanceOnly, Category = "XjMusic", meta = (ToolTip = "Name of XJ project file"))
	FString XjProjectFile;
	
	virtual void BeginPlay() override;

	virtual void BeginDestroy() override;

	void RunXjOneCycleTick(const float DeltaTime);

	bool IsWithinTimeLimit() const
	{
		if (MAXIMUM_TEST_WAIT_SECONDS * MILLIS_PER_SECOND > EntityUtils::currentTimeMillis() - XjStartTime)
		{
			return true;
		}

		UE_LOG(LogTemp, Error, TEXT("EXCEEDED TEST TIME LIMIT OF %lld SECONDS"), MAXIMUM_TEST_WAIT_SECONDS)

		return false;
	}

	bool HasSegmentsDubbedPastMinimumOffset() const
	{
		if (!XjEngine)
		{
			return false;
		}

		const auto segment = SegmentUtils::getLastCrafted(XjEngine->getSegmentStore()->readAllSegments());
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

	long long XjStartTime = EntityUtils::currentTimeMillis();
	
	unsigned long long AtChainMicros = 0;

	TUniquePtr<Engine> XjEngine;

	TMap<unsigned long long, FAudioPlayer> AudioLookup;

	TSet<FString> CurrentlyPlayingIds;

	class UXjMusicInstanceSubsystem* XjMusicInstanceSubsystem = nullptr;
};
