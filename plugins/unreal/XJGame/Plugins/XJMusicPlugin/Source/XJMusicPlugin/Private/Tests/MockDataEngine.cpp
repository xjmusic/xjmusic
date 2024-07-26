// Fill out your copyright notice in the Description page of Project Settings.

#include "Tests/MockDataEngine.h"

void TMockDataEngine::Setup(const FString& PathToProject)
{
}

void TMockDataEngine::Shutdown()
{
}

TSet<FAudioPlayer> TMockDataEngine::RunCycle(const uint64 ChainMicros)
{
    return TSet<FAudioPlayer>();
}
