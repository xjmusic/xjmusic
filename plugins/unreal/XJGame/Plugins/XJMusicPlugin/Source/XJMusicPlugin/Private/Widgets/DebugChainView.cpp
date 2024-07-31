// Fill out your copyright notice in the Description page of Project Settings.


#include "Widgets/DebugChainView.h"
#include "SlateOptMacros.h"
#include <Brushes/SlateColorBrush.h>

BEGIN_SLATE_FUNCTION_BUILD_OPTIMIZATION

void SDebugChainView::Construct(const FArguments& Args)
{
	TSharedPtr<TEngineBase> Engine = Args._Engine.Pin();
	if (!Engine)
	{
		return;
	}

	FString TemplateStr = FString::Printf(TEXT("Template: %s"), *Engine->GetActiveTemplateName());
	FString CraftAheadStr = FString::Printf(TEXT("Craft Ahead: %d s"), Engine->GetSettings().CraftAheadSeconds);
	FString DubAheadStr = FString::Printf(TEXT("Dub Ahead: %d s"), Engine->GetSettings().DubAheadSeconds);
	FString DeadlineStr = FString::Printf(TEXT("Deadline: %d s"), Engine->GetSettings().DeadlineSeconds);
	FString PersistenceWindowStr = FString::Printf(TEXT("Persistence Window: %d s"), Engine->GetSettings().PersistenceWindowSeconds);

	ChildSlot
	.VAlign(VAlign_Bottom)
	.HAlign(HAlign_Fill)
	[
		SNew(SOverlay)
		+ SOverlay::Slot()
		.VAlign(VAlign_Fill)
		.HAlign(HAlign_Fill)
		[
			SNew(SImage)
			.Image(new FSlateColorBrush(FLinearColor(0.1f, 0.1f, 0.1f, 1.0f)))
		]
		+ SOverlay::Slot()
		.VAlign(VAlign_Fill)
		.HAlign(HAlign_Fill)
		[
			SNew(SVerticalBox)
			+ SVerticalBox::Slot()
			.VAlign(VAlign_Fill)
			.HAlign(HAlign_Fill)
			.FillHeight(0.2f)
			[
				SNew(SHorizontalBox)
				+ SHorizontalBox::Slot()
				.VAlign(VAlign_Fill)
				.HAlign(HAlign_Fill)
				.Padding(FMargin(0.0f, 0.0f, 10.0f, 0.0f))
				[
					SNew(STextBlock)
					.Text(FText::FromString(TemplateStr))
					.Font(FSlateFontInfo(FPaths::EngineContentDir() / TEXT("Slate/Fonts/Roboto-Bold.ttf"), 24))
				]
				+ SHorizontalBox::Slot()
				.VAlign(VAlign_Fill)
				.HAlign(HAlign_Left)
				.Padding(FMargin(0.0f, 0.0f, 10.0f, 0.0f))
				[
					SNew(STextBlock)
					.Text(FText::FromString(CraftAheadStr))
					.Font(FSlateFontInfo(FPaths::EngineContentDir() / TEXT("Slate/Fonts/Roboto-Bold.ttf"), 24))
				]
				+ SHorizontalBox::Slot()
				.VAlign(VAlign_Fill)
				.HAlign(HAlign_Left)
				.Padding(FMargin(0.0f, 0.0f, 10.0f, 0.0f))
				[
					SNew(STextBlock)
					.Text(FText::FromString(DubAheadStr))
					.Font(FSlateFontInfo(FPaths::EngineContentDir() / TEXT("Slate/Fonts/Roboto-Bold.ttf"), 24))
				]
				+ SHorizontalBox::Slot()
				.VAlign(VAlign_Fill)
				.HAlign(HAlign_Left)
				.Padding(FMargin(0.0f, 0.0f, 10.0f, 0.0f))
				[
					SNew(STextBlock)
					.Text(FText::FromString(DeadlineStr))
					.Font(FSlateFontInfo(FPaths::EngineContentDir() / TEXT("Slate/Fonts/Roboto-Bold.ttf"), 24))
				]
				+ SHorizontalBox::Slot()
				.HAlign(HAlign_Left)
				.Padding(FMargin(0.0f, 0.0f, 10.0f, 0.0f))
				[
					SNew(STextBlock)
					.Text(FText::FromString(PersistenceWindowStr))
					.Font(FSlateFontInfo(FPaths::EngineContentDir() / TEXT("Slate/Fonts/Roboto-Bold.ttf"), 24))
				]
			]
			+ SVerticalBox::Slot()
			.VAlign(VAlign_Fill)
			.HAlign(HAlign_Right)
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
						SAssignNew(ActiveAudiosVB, SVerticalBox)
					]
				]
			]
		]
	];
}

void SDebugChainView::UpdateActiveAudios(const TMap<FString, FAudioPlayer>& ActiveAudios, const TimeRecord& ChainMicros)
{
	if (!ActiveAudiosVB.IsValid())
	{
		return;
	}

	if (ActiveAudiosVB->GetAllChildren()->Num() > 0)
	{
		ActiveAudiosVB->ClearChildren();
	}

	for (const TPair<FString, FAudioPlayer>& Audio : ActiveAudios)
	{
		const FAudioPlayer& Value = Audio.Value;

		FString AudioInfoStr = FString::Printf(TEXT("%s %fs - %fs"), *Value.Name, Value.StartTime.GetSeconds(), Value.EndTime.GetSeconds());

		bool bActive = ChainMicros.GetMicros() >= Value.StartTime.GetMicros() && ChainMicros.GetMicros() < Value.EndTime.GetMicros();

		ActiveAudiosVB->AddSlot()
		.AutoHeight()
		.Padding(5)
		[
			SNew(STextBlock)
			.Text(FText::FromString(AudioInfoStr))
			.Font(FSlateFontInfo(FPaths::EngineContentDir() / TEXT("Slate/Fonts/Roboto-Bold.ttf"), 14))
			.ColorAndOpacity(bActive ? FLinearColor::Green : FLinearColor::Gray)
		];
	}
}

END_SLATE_FUNCTION_BUILD_OPTIMIZATION