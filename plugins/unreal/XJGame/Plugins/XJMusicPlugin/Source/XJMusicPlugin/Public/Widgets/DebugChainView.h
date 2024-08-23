// Fill out your copyright notice in the Description page of Project Settings.

#pragma once

#include "CoreMinimal.h"
#include "Widgets/SCompoundWidget.h"
#include "Engine/EngineBase.h"
#include "Widgets/Layout/SScrollBox.h"
#include "Widgets/DebugSegmentView.h"

class XJMUSICPLUGIN_API SDebugChainView : public SCompoundWidget
{
public:
	SLATE_BEGIN_ARGS(SDebugChainView)
	{}

	SLATE_ARGUMENT(TWeakPtr<TEngineBase>, Engine)

	SLATE_END_ARGS();

	void Construct(const FArguments& Args);

	void UpdateActiveAudios(const TMap<FString, FAudioPlayer>& ActiveAudios, const TimeRecord& ChainMicros);

	void Tick(const FGeometry& AllottedGeometry, const double InCurrentTime, const float InDeltaTime) override;

private:
	TSharedPtr<SScrollBox> ActiveAudiosVB;
	TSharedPtr<SScrollBox> SegmentsSB;

	TSharedPtr<STextBlock> ChainMicrosTB;

	TSharedPtr< STextBlock> LoadingTB;

	TSharedPtr<TEngineBase> Engine;

	TMap<int, TSharedPtr<SDebugSegmentView>> CreatedSegments;
};
