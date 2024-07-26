// Fill out your copyright notice in the Description page of Project Settings.

#pragma once

#include "CoreMinimal.h"
#include "UObject/NoExportTypes.h"
#include "Types/XjTypes.h"
#include <Engine/EngineBase.h>

#include "XjManager.generated.h"

class FXjRunnable : public FRunnable
{

public:

	FXjRunnable(const FString& XjProjectFolder, const FString& XjProjectFile, UWorld* World);

	virtual bool Init() override;
	virtual uint32 Run() override;
	virtual void Stop() override;

private:

	FThreadSafeBool bShouldStop = false;

	int RunCycleFrequency = 3;

	int MILLIS_PER_SECOND = 1000;
	int MICROS_PER_MILLI = 1000;
	int GENERATED_FIXTURE_COMPLEXITY = 3;

	uint64 MICROS_PER_SECOND = MICROS_PER_MILLI * MILLIS_PER_SECOND;

	TimeRecord XjStartTime;

	TimeRecord AtChainMicros;

	class UXjMusicInstanceSubsystem* XjMusicSubsystem = nullptr;

	TUniquePtr<TEngineBase> Engine;
};

UCLASS()
class XJMUSICPLUGIN_API UXjManager : public UObject
{
	GENERATED_BODY()

public:

	void Setup();

	void BeginDestroy() override;

private:
	FXjRunnable* XjRunnable = nullptr;
	FRunnableThread* XjThread = nullptr;
};
