// Fill out your copyright notice in the Description page of Project Settings.


#include "Widgets/DebugSegmentView.h"
#include "SlateOptMacros.h"
#include <Brushes/SlateColorBrush.h>
#include <Widgets/Layout/SUniformGridPanel.h>

BEGIN_SLATE_FUNCTION_BUILD_OPTIMIZATION

void SDebugSegmentView::Construct(const FArguments& Args)
{
	FString IdStr = FString::FromInt(Args._SegmentInfo.Id);
	FString DeltaStr = FloatToString(Args._SegmentInfo.Delta);
	FString BeginSecondsStr = FloatToString(Args._SegmentInfo.StartTime.GetSeconds());
	FString TypeStr = Args._SegmentInfo.TypeStr;

	FString TotalTimeStr = FString::Printf(TEXT("%d bars\n%.2fs"), Args._SegmentInfo.TotalBars, Args._SegmentInfo.TotalTime.GetSeconds());
	FString IntensityStr = FString::Printf(TEXT("Intensity:\n%.2f"), Args._SegmentInfo.Intensity);
	FString TempoStr = FString::Printf(TEXT("Tempo:\n%d"), Args._SegmentInfo.Tempo);
	FString KeyStr = FString::Printf(TEXT("Key:\n%s"), *Args._SegmentInfo.Key);

	FString MemesStr = "Memes:\n";
	for (const FString& Meme : Args._SegmentInfo.Memes)
	{
		MemesStr += Meme + "\n";
	}

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
				SNew(SVerticalBox)
				+ SVerticalBox::Slot()
				.FillHeight(0.25f)
				[
					SNew(SUniformGridPanel)
					.SlotPadding(5.0f)
					+ SUniformGridPanel::Slot(0, 0)
					[
						SNew(STextBlock)
						.Text(FText::FromString(IdStr))
						.Font(FSlateFontInfo(FPaths::EngineContentDir() / TEXT("Slate/Fonts/Roboto-Bold.ttf"), 14))
					]
					+ SUniformGridPanel::Slot(1, 0)
					[
						SNew(STextBlock)
						.Text(FText::FromString(DeltaStr))
						.Font(FSlateFontInfo(FPaths::EngineContentDir() / TEXT("Slate/Fonts/Roboto-Bold.ttf"), 14))
					]
					+ SUniformGridPanel::Slot(0, 1)
					[
						SNew(STextBlock)
						.Text(FText::FromString(BeginSecondsStr + "s"))
						.Font(FSlateFontInfo(FPaths::EngineContentDir() / TEXT("Slate/Fonts/Roboto-Bold.ttf"), 14))
					]
					+ SUniformGridPanel::Slot(1, 1)
					[
						SNew(STextBlock)
						.Text(FText::FromString(TypeStr))
						.Font(FSlateFontInfo(FPaths::EngineContentDir() / TEXT("Slate/Fonts/Roboto-Bold.ttf"), 14))
					]
					+ SUniformGridPanel::Slot(0, 2)
					[
						SNew(STextBlock)
						.Text(FText::FromString(TotalTimeStr))
						.Font(FSlateFontInfo(FPaths::EngineContentDir() / TEXT("Slate/Fonts/Roboto-Bold.ttf"), 14))
					]
					+ SUniformGridPanel::Slot(1, 2)
					[
						SNew(STextBlock)
						.Text(FText::FromString(IntensityStr))
						.Font(FSlateFontInfo(FPaths::EngineContentDir() / TEXT("Slate/Fonts/Roboto-Bold.ttf"), 14))
					]
					+ SUniformGridPanel::Slot(2, 2)
					[
						SNew(STextBlock)
						.Text(FText::FromString(TempoStr))
						.Font(FSlateFontInfo(FPaths::EngineContentDir() / TEXT("Slate/Fonts/Roboto-Bold.ttf"), 14))
					]
					+ SUniformGridPanel::Slot(3, 2)
					[
						SNew(STextBlock)
						.Text(FText::FromString(KeyStr))
						.Font(FSlateFontInfo(FPaths::EngineContentDir() / TEXT("Slate/Fonts/Roboto-Bold.ttf"), 14))
					]
					+ SUniformGridPanel::Slot(0, 3)
					[
						SNew(STextBlock)
						.Text(FText::FromString(MemesStr))
						.Font(FSlateFontInfo(FPaths::EngineContentDir() / TEXT("Slate/Fonts/Roboto-Bold.ttf"), 14))
					]
				]
			]
		]
	];
}

END_SLATE_FUNCTION_BUILD_OPTIMIZATION