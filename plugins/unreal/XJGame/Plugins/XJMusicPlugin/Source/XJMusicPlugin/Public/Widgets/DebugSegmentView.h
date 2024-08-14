// Fill out your copyright notice in the Description page of Project Settings.

#pragma once

#include "CoreMinimal.h"
#include "Widgets/SCompoundWidget.h"
#include "Engine/EngineBase.h"

class XJMUSICPLUGIN_API SDebugSegmentView : public SCompoundWidget
{
public:

	SLATE_BEGIN_ARGS(SDebugSegmentView)
		: _SegmentInfo()
	{}

	SLATE_ARGUMENT(FSegmentInfo, SegmentInfo)

	SLATE_END_ARGS();

	void Construct(const FArguments& Args);

	void Update(const FSegmentInfo& Info);

	void MarkOutdated(bool bValue = true);

private:

	FSegmentInfo SegmentInfo;

	TSharedPtr<SScrollBox> FirstLevelChoicesVB;

	TMap<FString, TSharedPtr<SVerticalBox>> SecondLevelChoicesVB;

	bool bOutdated = false;

	void AddNewSegmentChoice(const FSegmentChoice& Choice);
};
