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
				[
					SNew(SBox)
					[
						SNew(SUniformGridPanel)
					.SlotPadding(2.0f)
					+ SUniformGridPanel::Slot(0, 0)
					[
						SNew(STextBlock)
						.Text(FText::FromString(IdStr))
						.Font(FSlateFontInfo(FPaths::EngineContentDir() / TEXT("Slate/Fonts/Roboto-Bold.ttf"), 10))
					]
					+ SUniformGridPanel::Slot(1, 0)
					[
						SNew(STextBlock)
						.Text(FText::FromString(""))
						.Font(FSlateFontInfo(FPaths::EngineContentDir() / TEXT("Slate/Fonts/Roboto-Bold.ttf"), 10))
					]
					+ SUniformGridPanel::Slot(0, 1)
					[
						SNew(STextBlock)
						.Text(FText::FromString(BeginSecondsStr + "s"))
						.Font(FSlateFontInfo(FPaths::EngineContentDir() / TEXT("Slate/Fonts/Roboto-Bold.ttf"), 10))
					]
					+ SUniformGridPanel::Slot(1, 1)
					[
						SNew(STextBlock)
						.Text(FText::FromString(TypeStr))
						.Font(FSlateFontInfo(FPaths::EngineContentDir() / TEXT("Slate/Fonts/Roboto-Bold.ttf"), 10))
					]
					+ SUniformGridPanel::Slot(0, 2)
					[
						SNew(STextBlock)
						.Text(FText::FromString(TotalTimeStr))
						.Font(FSlateFontInfo(FPaths::EngineContentDir() / TEXT("Slate/Fonts/Roboto-Bold.ttf"), 10))
					]
					+ SUniformGridPanel::Slot(1, 2)
					[
						SNew(STextBlock)
						.Text(FText::FromString(IntensityStr))
						.Font(FSlateFontInfo(FPaths::EngineContentDir() / TEXT("Slate/Fonts/Roboto-Bold.ttf"), 10))
					]
					+ SUniformGridPanel::Slot(2, 2)
					[
						SNew(STextBlock)
						.Text(FText::FromString(TempoStr))
						.Font(FSlateFontInfo(FPaths::EngineContentDir() / TEXT("Slate/Fonts/Roboto-Bold.ttf"), 10))
					]
					+ SUniformGridPanel::Slot(3, 2)
					[
						SNew(STextBlock)
						.Text(FText::FromString(KeyStr))
						.Font(FSlateFontInfo(FPaths::EngineContentDir() / TEXT("Slate/Fonts/Roboto-Bold.ttf"), 10))
					]
					+ SUniformGridPanel::Slot(0, 3)
					[
						SNew(STextBlock)
						.Text(FText::FromString(MemesStr))
						.Font(FSlateFontInfo(FPaths::EngineContentDir() / TEXT("Slate/Fonts/Roboto-Bold.ttf"), 10))
					]
					]
				]
				+ SVerticalBox::Slot()
				.AutoHeight()
				.Padding(FMargin(0, 5, 0, 0))
				[
					SNew(STextBlock)
					.Text(FText::FromString("Choices:"))
					.Font(FSlateFontInfo(FPaths::EngineContentDir() / TEXT("Slate/Fonts/Roboto-Bold.ttf"), 10))
				]
				+ SVerticalBox::Slot()
				.AutoHeight()
				[
					SAssignNew(FirstLevelChoicesVB, SVerticalBox)
				]
			]
		]
	];
}

void SDebugSegmentView::Update(const FSegmentInfo& Info)
{
	if (FirstLevelChoicesVB->GetChildren()->Num() > 0)
	{
		FirstLevelChoicesVB->ClearChildren();
	}

	for (const FSegmentChoice& Choice : Info.MacroChoices)
	{
		AddNewSegmentChoice(Choice);
	}

	for (const FSegmentChoice& Choice : Info.MainChoices)
	{
		AddNewSegmentChoice(Choice);
	}

	for (const FSegmentChoice& Choice : Info.BeatChoices)
	{
		AddNewSegmentChoice(Choice);
	}

	for (const FSegmentChoice& Choice : Info.DetailChoices)
	{
		AddNewSegmentChoice(Choice);
	}
}

void SDebugSegmentView::AddNewSegmentChoice(const FSegmentChoice& Choice)
{
	if (!FirstLevelChoicesVB)
	{
		return;
	}

	FirstLevelChoicesVB->AddSlot()
	[
		SNew(STextBlock)
		.Text(FText::FromString(FString::Printf(TEXT("[%s%s] %s"), *Choice.Type, *Choice.Mode, *Choice.Name)))
		.Font(FSlateFontInfo(FPaths::EngineContentDir() / TEXT("Slate/Fonts/Roboto-Bold.ttf"), 10))
	];

	for (const FAudioPick& Pick : Choice.Picks)
	{
		FirstLevelChoicesVB->AddSlot()
		.Padding(10)
		[
			SNew(STextBlock)
			.Text(FText::FromString(FString::Printf(TEXT("[%.2f] %s"), Pick.StartTime.GetSeconds(), *Pick.Name)))
			.Font(FSlateFontInfo(FPaths::EngineContentDir() / TEXT("Slate/Fonts/Roboto-Bold.ttf"), 8))
			.ColorAndOpacity(Pick.bActive ? FLinearColor::Green :FLinearColor::Gray)
		];
	}
}

END_SLATE_FUNCTION_BUILD_OPTIMIZATION