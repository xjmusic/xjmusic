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

struct FAudioPick
{
	FString Name;
	TimeRecord StartTime;
	bool bActive = false;
};

struct FSegmentChoice
{
	FString Type;
	FString Mode;
	FString Name;

	TArray<FAudioPick> Picks;
};

static bool operator < (const FSegmentChoice& A, const FSegmentChoice& B)
{
	return FPlatformString::Stricmp(*A.Type, *B.Type) < 0;
}

struct FSegmentInfo
{
	int32 Id;

	float Delta;

	TimeRecord StartTime;

	FString TypeStr;

	int TotalBars;

	TimeRecord TotalTime;

	float Intensity;

	int Tempo;

	FString Key;

	TArray<FString> Memes;

	TArray<FSegmentChoice> MacroChoices;
	TArray<FSegmentChoice> MainChoices;
	TArray<FSegmentChoice> BeatChoices;
	TArray<FSegmentChoice> DetailChoices;

	bool operator == (const FSegmentInfo& Other) const
	{
		return Id == Other.Id;
	}
};

static uint32 GetTypeHash(const FSegmentInfo& Segment)
{
	return GetTypeHash(Segment.Id);
}

class TEngineBase
{

public:

	virtual void Setup(const FString& PathToProject) {}

	virtual void Shutdown() {}

	virtual TArray<FAudioPlayer> RunCycle(const uint64 ChainMicros) { return {}; }

	virtual FEngineSettings GetSettings() const { return {}; }

	virtual FString GetActiveTemplateName() const { return {}; }

	virtual TArray<FSegmentInfo> GetSegments() { return {}; }

	virtual TimeRecord GetLastMicros() const { return {}; }

	virtual void DoOverrideTaxonomy(const FString& Taxonomy) {}

	virtual void DoOverrideMacro(const FString& Macros) {}

	virtual void DoOverrideIntensity(const float IntensityValue) {}

	virtual TArray<FString> GetAllMacros() const { return {}; }

	virtual TArray<FString> GetAllTaxonomyMemes() const { return {}; }
};