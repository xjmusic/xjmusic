// Fill out your copyright notice in the Description page of Project Settings.

#pragma once

#include "CoreMinimal.h"
#include "Types/XjTypes.h"
#include "HAL/Platform.h"
#include "Containers/Set.h"

struct EngineSettings
{
	int32 CraftAheadSeconds = 20;
	int32 DubAheadSeconds = 10;
	int32 PersistenceWindowSeconds = 3600;
};

class TEngineBase
{

public:

	virtual void Setup(const FString& PathToProject) {}

	virtual void Shutdown() {}

	virtual TSet<FAudioPlayer> RunCycle(const uint64 ChainMicros) { return {}; }

	virtual EngineSettings GetSettings() const { return {}; }
};