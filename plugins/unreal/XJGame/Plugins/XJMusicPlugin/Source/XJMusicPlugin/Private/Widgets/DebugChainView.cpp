// Fill out your copyright notice in the Description page of Project Settings.


#include "Widgets/DebugChainView.h"
#include "SlateOptMacros.h"

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
			.ColorAndOpacity(FLinearColor(0.2f, 0.2, 0.2, 1.0f))
		]
		+ SOverlay::Slot()
		.VAlign(VAlign_Fill)
		.HAlign(HAlign_Fill)
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
	];
}

END_SLATE_FUNCTION_BUILD_OPTIMIZATION