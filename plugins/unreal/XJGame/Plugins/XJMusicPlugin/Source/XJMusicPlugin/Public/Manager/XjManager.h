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
	bool TryInitMockEngine();

private:

	FThreadSafeBool bShouldStop = false;

	int RunCycleFrequency = 3;

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
