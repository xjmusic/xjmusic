// Fill out your copyright notice in the Description page of Project Settings.

#pragma once

#include "CoreMinimal.h"
#include "GameFramework/Actor.h"

#include <xjmusic/work/WorkManager.h>
#include <xjmusic/fabricator/SegmentUtils.h>
#include <xjmusic/Engine.h>

#include "PrototypeActor.generated.h"

using namespace XJ;

class TimeRecord
{
public:
	TimeRecord() = default;

	TimeRecord(const uint64 NewTime)
	{
		SetInMicros(NewTime);
	}

	TimeRecord(const int NewTime)
	{
		SetInMicros(NewTime);
	}

	TimeRecord(const float NewTime)
	{
		SetInSeconds(NewTime);
	}

	void SetInMicros(const uint64 NewTime)
	{
		Micros = NewTime;
		Seconds = NewTime / 1000000.0f;
	}

	void SetInSeconds(const float NewTime)
	{
		Seconds = NewTime;
		Micros = NewTime * 1000000;
	}

	uint64 GetMicros() const
	{
		return Micros;
	}

	float GetMillie() const
	{
		return Seconds * 1000.0f;
	}

	float GetSeconds() const
	{
		return Seconds;
	}

	FString ToString() const
	{
		FString Out = FString::Printf(TEXT("%f s (%lld micro)"), Seconds, Micros);
		return Out;
	}

	bool operator > (const TimeRecord& Other) const
	{
		return this->Micros > Other.Micros;
	}

	bool operator < (const TimeRecord& Other) const
	{
		return this->Micros < Other.Micros;
	}

	bool operator >= (const TimeRecord& Other) const
	{
		return this->Micros >= Other.Micros;
	}

	bool operator <= (const TimeRecord& Other) const
	{
		return this->Micros <= Other.Micros;
	}

	bool operator == (const TimeRecord& Other) const
	{
		return this->Micros == Other.Micros;
	}

	void operator = (const uint64) = delete;
	void operator = (const float) = delete;

private:
	uint64 Micros = 0;
	float Seconds = 0.0f;
};

static uint32 GetTypeHash(const TimeRecord& Record)
{
	return GetTypeHash(Record.GetMicros());
}

struct FAudioPlayer
{
	TimeRecord  StartTime;
	TimeRecord EndTime;

	bool bIsPlaying = false;

	FString Name;
	FString Id;
};

class FXjRunnable : public FRunnable
{

public:

	FXjRunnable(const FString& XjProjectFolder, const FString& XjProjectFile, UWorld* World, class UAudioComponent* AudioComponent);
	virtual ~FXjRunnable() override;

	virtual bool Init() override;
	virtual uint32 Run() override;
	virtual void Stop() override;

private:

	bool IsWithinTimeLimit() const
	{
		uint64 ElapsedTime = EntityUtils::currentTimeMillis() - XjStartTime.GetMicros();
		if (MAXIMUM_TEST_WAIT_SECONDS * MILLIS_PER_SECOND > ElapsedTime)
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

	FThreadSafeBool bShouldStop = false;

	int RunCycleFrequency = 3;

	int MARATHON_NUMBER_OF_SEGMENTS = 50;
	int MILLIS_PER_SECOND = 1000;
	int MICROS_PER_MILLI = 1000;
	int GENERATED_FIXTURE_COMPLEXITY = 3;

	uint64 MAXIMUM_TEST_WAIT_SECONDS = 10 * MARATHON_NUMBER_OF_SEGMENTS;
	uint64 MICROS_PER_SECOND = MICROS_PER_MILLI * MILLIS_PER_SECOND;

	TimeRecord XjStartTime;

	TimeRecord AtChainMicros;

	TUniquePtr<Engine> XjEngine;

	class UXjMusicInstanceSubsystem* XjMusicInstanceSubsystem = nullptr;

	TMap<FString, TArray<FAudioPlayer>> DebugViewAudioToTime;

	TMap<TimeRecord, TArray<FString>> DebugViewTimeToAudio;
};


UCLASS()
class XJMUSICPLUGIN_API APrototypeActor : public AActor
{
	GENERATED_BODY()
	
public:	

	APrototypeActor();

protected:

	UPROPERTY(EditInstanceOnly, Category = "XjMusic", meta = (ToolTip = "Path to XJ project folder, with trailing slash"))
	FString XjProjectFolder;
	
	UPROPERTY(EditInstanceOnly, Category = "XjMusic", meta = (ToolTip = "Name of XJ project file"))
	FString XjProjectFile;
	
	virtual void BeginPlay() override;

	virtual void BeginDestroy() override;

	class UAudioComponent* AudioComponent;

private:
	FXjRunnable* XjRunnable = nullptr;
	FRunnableThread* XjThread = nullptr;
};
