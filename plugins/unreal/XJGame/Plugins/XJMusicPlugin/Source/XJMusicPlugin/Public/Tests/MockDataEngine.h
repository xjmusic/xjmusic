// Fill out your copyright notice in the Description page of Project Settings.

#pragma once

#include "CoreMinimal.h"
#include "Engine/DataTable.h"
#include "Engine/EngineBase.h"
#include "MockDataEngine.generated.h"

USTRUCT(BlueprintType)
struct FMockAudioInfo : public FTableRowBase
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
};

class TMockDataEngine : public TEngineBase
{
public:
	virtual void Setup(const FString& PathToProject) override;

	virtual void Shutdown() override;

	virtual TSet<FAudioPlayer> RunCycle(const uint64 ChainMicros) override;
};