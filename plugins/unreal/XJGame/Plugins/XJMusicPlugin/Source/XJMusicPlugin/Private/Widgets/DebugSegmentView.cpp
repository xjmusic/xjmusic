// Fill out your copyright notice in the Description page of Project Settings.


#include "Widgets/DebugSegmentView.h"
#include "SlateOptMacros.h"
#include <Brushes/SlateColorBrush.h>

BEGIN_SLATE_FUNCTION_BUILD_OPTIMIZATION

void SDebugSegmentView::Construct(const FArguments& Args)
{
	FString IdStr = FString::FromInt(Args._SegmentInfo.Id);

	ChildSlot
	[
		SNew(SBox)
		.MinDesiredHeight(600.0f)
		.MinDesiredWidth(200.0f)
		[
			SNew(SOverlay)
			+ SOverlay::Slot()
			.VAlign(VAlign_Fill)
			.HAlign(HAlign_Fill)
			[
				SNew(SImage)
				.Image(new FSlateColorBrush(FLinearColor(0.2f, 0.2f, 0.2f, 1.0f)))
			]
			+ SOverlay::Slot()
			.VAlign(VAlign_Fill)
			.HAlign(HAlign_Fill)
			[
				SNew(STextBlock)
				.Text(FText::FromString(IdStr))
				.Font(FSlateFontInfo(FPaths::EngineContentDir() / TEXT("Slate/Fonts/Roboto-Bold.ttf"), 14))
			]
		]
	];
}

END_SLATE_FUNCTION_BUILD_OPTIMIZATION