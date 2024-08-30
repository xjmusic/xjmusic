// Fill out your copyright notice in the Description page of Project Settings.

#pragma once

#include "CoreMinimal.h"
#include "EngineBase.h"
#include <xjmusic/work/WorkManager.h>
#include <xjmusic/fabricator/SegmentUtils.h>
#include <xjmusic/Engine.h>

using namespace XJ;

class TXjMainEngine : public TEngineBase
{

public:

	void Setup(const FString& PathToProject) override;

	TArray<FAudioPlayer> RunCycle(const uint64 ChainMicros) override;

	void DoOverrideTaxonomy(const FString& Taxonomy) override;

	void DoOverrideMacro(const FString& Macro) override;

	void DoOverrideIntensity(const float Intensity) override;

	TArray<FString> GetAllTaxonomyMemes() const override;

	TArray<FString> GetAllMacros() const override;

	FEngineSettings GetSettings() const override;

	FString GetActiveTemplateName() const override;

	TArray<FSegmentInfo> GetSegments() override;

	TimeRecord GetLastMicros() const override
	{
		return LastChainMicros;
	}

private:
	TUniquePtr<Engine> XjEngine;

	TimeRecord LastChainMicros;

	FString CurrentTemplateName;

	int MILLIS_PER_SECOND = 1000;
	int MICROS_PER_MILLI = 1000;
	int GENERATED_FIXTURE_COMPLEXITY = 3;

	uint64 MICROS_PER_SECOND = MICROS_PER_MILLI * MILLIS_PER_SECOND;

	int GetSegmentBarsBeats(const int Id) const;

	FSegmentChoice ParseSegmentChoice(const SegmentChoice* Choicem, const int64 SegmentMicros);
};