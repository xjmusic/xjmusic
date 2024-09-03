// Fill out your copyright notice in the Description page of Project Settings.


#include "Widgets/DebugSegmentView.h"
#include "SlateOptMacros.h"
#include <Brushes/SlateColorBrush.h>
#include <Widgets/Layout/SUniformGridPanel.h>
#include <Widgets/Layout/SScrollBox.h>
#include "Widgets/Images/SImage.h"

BEGIN_SLATE_FUNCTION_BUILD_OPTIMIZATION

void SDebugSegmentView::Construct(const FArguments& Args)
{
	SegmentInfo = Args._SegmentInfo;

	const FSlateFontInfo FontInfo = FSlateFontInfo(FPaths::EngineContentDir() / TEXT("Slate/Fonts/Roboto-Bold.ttf"), 10);

	const FSlateColorBrush* BackgroundBrush = new FSlateColorBrush(FLinearColor(0.2f, 0.2f, 0.2f, 1.0f));

	ChildSlot
	[
		SAssignNew(Border, SBorder)
		.BorderImage(DefaultBorderBrush)
		.Padding(FMargin(2.0f))
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
								SAssignNew(IdTextBlock,STextBlock)
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
								SAssignNew(BeginSecondsTextBlock, STextBlock)
								.Font(FontInfo)
							]
							+ SUniformGridPanel::Slot(1, 1)
							[
								SAssignNew(TypeTextBlock, STextBlock)
								.Font(FontInfo)
							]
							+ SUniformGridPanel::Slot(0, 2)
							[
								SAssignNew(TotalTextBlock, STextBlock)
								.Font(FontInfo)
							]
							+ SUniformGridPanel::Slot(1, 2)
							[
								SAssignNew(IntensityTextBlock, STextBlock)
								.Font(FontInfo)
							]
							+ SUniformGridPanel::Slot(2, 2)
							[
								SAssignNew(TempoTextBlock, STextBlock)
								.Font(FontInfo)
							]
							+ SUniformGridPanel::Slot(3, 2)
							[
								SAssignNew(KeyTextBlock, STextBlock)
								.Font(FontInfo)
							]
							+ SUniformGridPanel::Slot(0, 3)
							[
								SAssignNew(MemesTextBlock, STextBlock)
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
		]
		
	];
}

void SDebugSegmentView::Update(const FSegmentInfo& Info)
{
	const FString IdStr = FString::FromInt(Info.Id);
	const FString DeltaStr = FloatToString(Info.Delta);
	const FString BeginSecondsStr = FloatToString(Info.StartTime.GetSeconds());
	const FString TypeStr = Info.TypeStr;

	const FString TotalTimeStr = FString::Printf(TEXT("%d bars\n%.2fs"), Info.TotalBars, Info.TotalTime.GetSeconds());
	const FString IntensityStr = FString::Printf(TEXT("Intensity:\n%.2f"), Info.Intensity);
	const FString TempoStr = FString::Printf(TEXT("Tempo:\n%d"), Info.Tempo);
	const FString KeyStr = FString::Printf(TEXT("Key:\n%s"), *Info.Key);

	FString MemesStr = "Memes:\n";
	for (const FString& Meme : Info.Memes)
	{
		MemesStr += Meme + "\n";
	}

	IdTextBlock->SetText(FText::FromString(IdStr));
	BeginSecondsTextBlock->SetText(FText::FromString(BeginSecondsStr + "s"));
	TypeTextBlock->SetText(FText::FromString(TypeStr));
	TotalTextBlock->SetText(FText::FromString(TotalTimeStr));
	IntensityTextBlock->SetText(FText::FromString(IntensityStr));
	TempoTextBlock->SetText(FText::FromString(TempoStr));
	KeyTextBlock->SetText(FText::FromString(KeyStr));
	MemesTextBlock->SetText(FText::FromString(MemesStr));

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

void SDebugSegmentView::MarkOutdated(const bool bValue)
{
	if (bValue == bOutdated)
	{
		return;
	}
	
	bOutdated = bValue;

	SetEnabled(!bOutdated);
}

void SDebugSegmentView::ShowActiveBorder(const bool bShow)
{
	if (!Border || bActive == bShow)
	{
		return;
	}

	bActive = bShow;

	Border->SetBorderImage(bShow ? ActiveBorderBrush : DefaultBorderBrush);
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
			.Text(FText::FromString(FString::Printf(TEXT("[%.2f] %s"), Pick.StartTime.GetSeconds() + SegmentInfo.StartTime.GetSeconds(), *Pick.Name)))
			.Font(PickFontInfo)
			.ColorAndOpacity(Pick.bActive ? FLinearColor::Green :FLinearColor::Gray)
		];
	}
}

END_SLATE_FUNCTION_BUILD_OPTIMIZATION