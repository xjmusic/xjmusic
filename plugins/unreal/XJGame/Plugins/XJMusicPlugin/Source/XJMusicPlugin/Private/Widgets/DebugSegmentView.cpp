// Fill out your copyright notice in the Description page of Project Settings.


#include "Widgets/DebugSegmentView.h"
#include "SlateOptMacros.h"
#include <Brushes/SlateColorBrush.h>
#include <Widgets/Layout/SUniformGridPanel.h>
#include <Widgets/Layout/SScrollBox.h>

BEGIN_SLATE_FUNCTION_BUILD_OPTIMIZATION

void SDebugSegmentView::Construct(const FArguments& Args)
{
	const FString IdStr = FString::FromInt(Args._SegmentInfo.Id);
	const FString DeltaStr = FloatToString(Args._SegmentInfo.Delta);
	const FString BeginSecondsStr = FloatToString(Args._SegmentInfo.StartTime.GetSeconds());
	const FString TypeStr = Args._SegmentInfo.TypeStr;

	const FString TotalTimeStr = FString::Printf(TEXT("%d bars\n%.2fs"), Args._SegmentInfo.TotalBars, Args._SegmentInfo.TotalTime.GetSeconds());
	const FString IntensityStr = FString::Printf(TEXT("Intensity:\n%.2f"), Args._SegmentInfo.Intensity);
	const FString TempoStr = FString::Printf(TEXT("Tempo:\n%d"), Args._SegmentInfo.Tempo);
	const FString KeyStr = FString::Printf(TEXT("Key:\n%s"), *Args._SegmentInfo.Key);

	FString MemesStr = "Memes:\n";
	for (const FString& Meme : Args._SegmentInfo.Memes)
	{
		MemesStr += Meme + "\n";
	}

	const FSlateFontInfo FontInfo = FSlateFontInfo(FPaths::EngineContentDir() / TEXT("Slate/Fonts/Roboto-Bold.ttf"), 10);

	const FSlateColorBrush* BackgroundBrush = new FSlateColorBrush(FLinearColor(0.2f, 0.2f, 0.2f, 1.0f));

	ChildSlot
	[
		SNew(SBox)
		.MinDesiredHeight(600.0f)
		.MaxDesiredHeight(600.0f)
		.MinDesiredWidth(350.0f)
		.MaxDesiredWidth(350.0f)
		[
			SNew(SOverlay)
			+ SOverlay::Slot()
			.VAlign(VAlign_Fill)
			.HAlign(HAlign_Fill)
			[
				SNew(SImage)
				.Image(BackgroundBrush)
			]
			+ SOverlay::Slot()
			.Padding(FMargin(5.0f, 0.0f, 0, 0))
			.VAlign(VAlign_Fill)
			.HAlign(HAlign_Fill)
			[
				SNew(SVerticalBox)
				+ SVerticalBox::Slot()
				.AutoHeight()
				[
					SNew(SBox)
					.MinDesiredHeight(200.0f)
					.MaxDesiredHeight(200.0f)
					[
						SNew(SUniformGridPanel)
						.SlotPadding(FMargin(0, 2.0f))
						+ SUniformGridPanel::Slot(0, 0)
						[
							SNew(STextBlock)
							.Text(FText::FromString(IdStr))
							.Font(FontInfo)
						]
						+ SUniformGridPanel::Slot(1, 0)
						[
							SNew(STextBlock)
							.Text(FText::FromString(""))
							.Font(FontInfo)
						]
						+ SUniformGridPanel::Slot(0, 1)
						[
							SNew(STextBlock)
							.Text(FText::FromString(BeginSecondsStr + "s"))
							.Font(FontInfo)
						]
						+ SUniformGridPanel::Slot(1, 1)
						[
							SNew(STextBlock)
							.Text(FText::FromString(TypeStr))
							.Font(FontInfo)
						]
						+ SUniformGridPanel::Slot(0, 2)
						[
							SNew(STextBlock)
							.Text(FText::FromString(TotalTimeStr))
							.Font(FontInfo)
						]
						+ SUniformGridPanel::Slot(1, 2)
						[
							SNew(STextBlock)
							.Text(FText::FromString(IntensityStr))
							.Font(FontInfo)
						]
						+ SUniformGridPanel::Slot(2, 2)
						[
							SNew(STextBlock)
							.Text(FText::FromString(TempoStr))
							.Font(FontInfo)
						]
						+ SUniformGridPanel::Slot(3, 2)
						[
							SNew(STextBlock)
							.Text(FText::FromString(KeyStr))
							.Font(FontInfo)
						]
						+ SUniformGridPanel::Slot(0, 3)
						[
							SNew(STextBlock)
							.Text(FText::FromString(MemesStr))
							.Font(FontInfo)
						]
					]
				]
				+ SVerticalBox::Slot()
				.AutoHeight()
				.Padding(FMargin(0.0f, 10.0f, 0, 0))
				[
					SNew(STextBlock)
					.Text(FText::FromString("Choices:"))
					.Font(FontInfo)
				]
				+ SVerticalBox::Slot()
				.Padding(FMargin(0.0f, 5.0f, 0, 0))
				[
					SAssignNew(FirstLevelChoicesVB, SScrollBox)
					.Orientation(EOrientation::Orient_Vertical)
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

	MarkOutdated(false);

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

void SDebugSegmentView::MarkOutdated(bool bValue)
{
	if (bValue == bOutdated)
	{
		return;
	}
	
	bOutdated = bValue;

	SetEnabled(!bOutdated);
}

void SDebugSegmentView::AddNewSegmentChoice(const FSegmentChoice& Choice)
{
	if (!FirstLevelChoicesVB)
	{
		return;
	}

	const FSlateFontInfo ChoiceFontInfo = FSlateFontInfo(FPaths::EngineContentDir() / TEXT("Slate/Fonts/Roboto-Bold.ttf"), 9);
	const FSlateFontInfo PickFontInfo = FSlateFontInfo(FPaths::EngineContentDir() / TEXT("Slate/Fonts/Roboto-Bold.ttf"), 8);

	FirstLevelChoicesVB->AddSlot()
	[
		SNew(STextBlock)
		.Text(FText::FromString(FString::Printf(TEXT("[%s%s] %s"), *Choice.Type, *Choice.Mode, *Choice.Name)))
		.Font(ChoiceFontInfo)
	];

	for (const FAudioPick& Pick : Choice.Picks)
	{
		FirstLevelChoicesVB->AddSlot()
		.Padding(10)
		[
			SNew(STextBlock)
			.Text(FText::FromString(FString::Printf(TEXT("[%.2f] %s"), Pick.StartTime.GetSeconds(), *Pick.Name)))
			.Font(PickFontInfo)
			.ColorAndOpacity(Pick.bActive ? FLinearColor::Green :FLinearColor::Gray)
		];
	}
}

END_SLATE_FUNCTION_BUILD_OPTIMIZATION