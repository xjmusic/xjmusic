// Fill out your copyright notice in the Description page of Project Settings.

#pragma once

#include "CoreMinimal.h"
#include "Types/XjTypes.h"
#include <Engine/EngineBase.h>

#include "XjManager.generated.h"

class FXjRunnable : public FRunnable
{

public:

	FXjRunnable(const FString& PathToProjectFile, const UWorld* World);

	virtual bool Init() override;
	virtual uint32 Run() override;
	virtual void Stop() override;

	TimeRecord GetAtChainMicros() const
	{
		return AtChainMicros;
	}

private:
	bool TryInitMockEngine();

private:

	double LastFramTime = 0.0f;

	FThreadSafeBool bShouldStop = false;

	int RunCycleFrequency = 9;

	TimeRecord XjStartTime;

	TimeRecord AtChainMicros;

	class UXjMusicInstanceSubsystem* XjMusicSubsystem = nullptr;

	TSharedPtr<TEngineBase> Engine;
};

UCLASS()
class XJMUSICPLUGIN_API UXjManager : public UObject
{
	GENERATED_BODY()

public:

	void Setup();

	virtual void BeginDestroy() override;

	TimeRecord GetAtChainMicros() const
	{
		if (!XjRunnable)
		{
			return {};
		}

		return XjRunnable->GetAtChainMicros();
	}

private:
	TSharedPtr<FXjRunnable> XjRunnable;
	TSharedPtr<FRunnableThread> XjThread;
};
