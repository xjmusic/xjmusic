// Fill out your copyright notice in the Description page of Project Settings.

#pragma once

#include "CoreMinimal.h"
#include "Types/XjTypes.h"
#include "HAL/Platform.h"
#include "Containers/Set.h"

struct FEngineSettings
{
	int32 CraftAheadSeconds;
	int32 DubAheadSeconds;
	int32 DeadlineSeconds;
	int32 PersistenceWindowSeconds;
};

class TEngineBase
{

public:

	virtual void Setup(const FString& PathToProject) {}

	virtual void Shutdown() {}

	virtual TArray<FAudioPlayer> RunCycle(const uint64 ChainMicros) { return {}; }

	virtual FEngineSettings GetSettings() const { return {}; }

	virtual FString GetActiveTemplateName() const { return {}; }
};