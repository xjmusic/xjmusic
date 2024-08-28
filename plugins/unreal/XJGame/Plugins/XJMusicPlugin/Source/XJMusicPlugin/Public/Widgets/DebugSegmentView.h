// Fill out your copyright notice in the Description page of Project Settings.

#pragma once

#include "CoreMinimal.h"
#include "Widgets/SCompoundWidget.h"
#include "Engine/EngineBase.h"
#include "Brushes/SlateColorBrush.h"

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

	void MarkOutdated(const bool bValue = true);

	void ShowActiveBorder(const bool bShow);

private:

	FSegmentInfo SegmentInfo;

	TSharedPtr<STextBlock> IdTextBlock;
	TSharedPtr<STextBlock> BeginSecondsTextBlock;
	TSharedPtr<STextBlock> TypeTextBlock;
	TSharedPtr<STextBlock> TotalTextBlock;
	TSharedPtr<STextBlock> IntensityTextBlock;
	TSharedPtr<STextBlock> TempoTextBlock;
	TSharedPtr<STextBlock> KeyTextBlock;
	TSharedPtr<STextBlock> MemesTextBlock;

	TSharedPtr<SScrollBox> FirstLevelChoicesVB;

	TSharedPtr<SBorder> Border;

	TMap<FString, TSharedPtr<SVerticalBox>> SecondLevelChoicesVB;

	bool bOutdated = false;

	bool bActive = false;

	const FSlateColorBrush* DefaultBorderBrush = new FSlateColorBrush(FLinearColor(0.08f, 0.08f, 0.08f, 1.0f));
	const FSlateColorBrush* ActiveBorderBrush = new FSlateColorBrush(FLinearColor(0.05f, 0.45f, 0.05f, 1.0f));

	void AddNewSegmentChoice(const FSegmentChoice& Choice);
};
