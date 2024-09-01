// Fill out your copyright notice in the Description page of Project Settings.

#pragma once

#include "CoreMinimal.h"
#include "Types/XjTypes.h"
#include "Engine/EngineBase.h"
#include "XjManager.generated.h"

enum class XjCommandType
{
	TaxonomyChange,
	MacrosChange,
	IntensityChange
};

struct XjCommand
{
	XjCommandType Type;

	FString Arguments;

	float FloatValue = 0.0f;
};

UCLASS()
class XJMUSICPLUGIN_API UXjManager : public UObject, public FTickableGameObject
{
	GENERATED_BODY()

public:

	void Setup();

	void Tick(float DeltaTime) override;

	bool IsTickableWhenPaused() const override
	{
		return true;
	}

	bool IsTickableInEditor() const override
	{
		return false;
	}

	bool IsTickable() const override 
	{ 
		return bCanTick; 
	}

	TStatId GetStatId() const override
	{
		return TStatId();
	}

	UWorld* GetWorld() const override
	{
		return GetOuter()->GetWorld();
	}

	void PushCommand(const XjCommand& NewCommand)
	{
		Commands.Enqueue(NewCommand);
	}

	TimeRecord GetAtChainMicros() const
	{
		return AtChainMicros;
	}

	TWeakPtr<TEngineBase> GetActiveEngine() const
	{
		return Engine;
	}

private:

	class UXjMusicInstanceSubsystem* XjMusicSubsystem = nullptr;

	TimeRecord XjStartTime;

	TimeRecord AtChainMicros;

	TQueue<XjCommand> Commands;

	TSharedPtr<TEngineBase> Engine;

	const float RunCycleInterval = (1.0f / 9.0f);

	float FramTimeAccumulation = RunCycleInterval;

	bool bCanTick = false;

	bool TryInitMockEngine();
};
