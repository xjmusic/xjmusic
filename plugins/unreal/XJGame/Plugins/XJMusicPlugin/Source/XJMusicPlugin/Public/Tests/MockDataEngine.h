// Fill out your copyright notice in the Description page of Project Settings.

#pragma once

#include "CoreMinimal.h"
#include "Engine/DataTable.h"
#include "Engine/EngineBase.h"
#include "MockDataEngine.generated.h"

USTRUCT(BlueprintType)
struct FMockAudioTableRow : public FTableRowBase
{
	GENERATED_BODY()

public:
	UPROPERTY(EditAnywhere, BlueprintReadWrite)
	FString Name;

	UPROPERTY(EditAnywhere, BlueprintReadWrite)
	FString Id;

	UPROPERTY(EditAnywhere, BlueprintReadWrite)
	int64 StartTimeAtChainMicros = 0;

	UPROPERTY(EditAnywhere, BlueprintReadWrite)
	int64 EndTimeAtChainMicros = 0;

	UPROPERTY(EditAnywhere, BlueprintReadWrite)
	int64 SchedulePastCertainChainMicros = 0;
};

class TMockDataEngine : public TEngineBase
{
public:

	virtual TArray<FAudioPlayer> RunCycle(const uint64 ChainMicros) override;

	void SetMockData(class UDataTable* DataTable);

	int MaxAudiosOutputPerCycle = 0;

	float LatencyBetweenCyclesInSeconds = 0.0f;


	TimeRecord GetLastMicros() const override
	{
		TimeRecord Time;
		Time.SetInMicros(LastMicros);

		return Time;
	}


private:
	TArray<FMockAudioTableRow> ScheduledAudios;

	uint64 LastMicros = 0;

	int LastItr = 0;
};