// Fill out your copyright notice in the Description page of Project Settings.


#include "Widgets/DebugChainView.h"
#include "SlateOptMacros.h"

#include "Types/XjTypes.h"
#include "Widgets/Images/SImage.h"

BEGIN_SLATE_FUNCTION_BUILD_OPTIMIZATION

void SDebugChainView::Construct(const FArguments& Args)
{
	Engine = Args._Engine.Pin();

	check(Engine);

	const FString TemplateStr = FString::Printf(TEXT("Template: %s"), *Engine->GetActiveTemplateName());
	const FString CraftAheadStr = FString::Printf(TEXT("Craft Ahead: %d s"), Engine->GetSettings().CraftAheadSeconds);
	const FString DubAheadStr = FString::Printf(TEXT("Dub Ahead: %d s"), Engine->GetSettings().DubAheadSeconds);
	const FString DeadlineStr = FString::Printf(TEXT("Deadline: %d s"), Engine->GetSettings().DeadlineSeconds);
	const FString PersistenceWindowStr = FString::Printf(TEXT("Persistence Window: %d s"), Engine->GetSettings().PersistenceWindowSeconds);

	const FSlateFontInfo HeaderFontInfo = FSlateFontInfo(FPaths::EngineContentDir() / TEXT("Slate/Fonts/Roboto-Bold.ttf"), 11);

	const FMargin HeaderTextOffset(10.0f, 0.0f, 10.0f, 0.0f);

	const FSlateColorBrush* BackgroundBrush = new FSlateColorBrush(FLinearColor(0.1f, 0.1f, 0.1f, 1.0f));

	const FSlateColorBrush* HeaderBackgroundBrush = new FSlateColorBrush(FLinearColor(0.05f, 0.05f, 0.05f, 1.0f));

	const FSlateColorBrush* ActiveAudiosBackgroundBrush = new FSlateColorBrush(FLinearColor(0.2f, 0.2f, 0.2f, 1.0f));

	ChildSlot
	.VAlign(VAlign_Bottom)
	.HAlign(HAlign_Fill)
	[
		SNew(SBox)
		.MinDesiredHeight(600.0f)
		.MaxDesiredHeight(600.0f)
		[
			SNew(SOverlay)
				+ SOverlay::Slot()
				.VAlign(VAlign_Fill)
				.HAlign(HAlign_Fill)
				[
					SNew(SImage)
						.Image(BackgroundBrush)
				]
				+SOverlay::Slot()
				.VAlign(VAlign_Center)
				.HAlign(HAlign_Center)
				[
					SAssignNew(LoadingTB, STextBlock)
					.Text(INVTEXT("LOADING ASSETS..."))
					.Font(HeaderFontInfo)
				]
				+ SOverlay::Slot()
				.VAlign(VAlign_Fill)
				.HAlign(HAlign_Fill)
				[
					SNew(SVerticalBox)
						+ SVerticalBox::Slot()
						.VAlign(VAlign_Fill)
						.HAlign(HAlign_Fill)
						.FillHeight(0.04f)
						[
							SNew(SOverlay)
								+ SOverlay::Slot()
								.HAlign(HAlign_Fill)
								[
									SNew(SImage)
										.Image(HeaderBackgroundBrush)
								]
								+ SOverlay::Slot()
								.HAlign(HAlign_Left)
								[
									SNew(SHorizontalBox)
										+ SHorizontalBox::Slot()
										.VAlign(VAlign_Fill)
										.AutoWidth()
										.Padding(HeaderTextOffset)
										[
											SAssignNew(ChainMicrosTB, STextBlock)
												.Font(HeaderFontInfo)
										]
										+ SHorizontalBox::Slot()
										.VAlign(VAlign_Fill)
										.AutoWidth()
										.Padding(HeaderTextOffset)
										[
											SNew(STextBlock)
												.Text(FText::FromString(TemplateStr))
												.Font(HeaderFontInfo)
										]
										+ SHorizontalBox::Slot()
										.VAlign(VAlign_Fill)
										.AutoWidth()
										.Padding(HeaderTextOffset)
										[
											SNew(STextBlock)
												.Text(FText::FromString(CraftAheadStr))
												.Font(HeaderFontInfo)
										]
										+ SHorizontalBox::Slot()
										.VAlign(VAlign_Fill)
										.AutoWidth()
										.Padding(HeaderTextOffset)
										[
											SNew(STextBlock)
												.Text(FText::FromString(DubAheadStr))
												.Font(HeaderFontInfo)
										]
										+ SHorizontalBox::Slot()
										.VAlign(VAlign_Fill)
										.AutoWidth()
										.Padding(HeaderTextOffset)
										[
											SNew(STextBlock)
												.Text(FText::FromString(DeadlineStr))
												.Font(HeaderFontInfo)
										]
										+ SHorizontalBox::Slot()
										.AutoWidth()
										.Padding(HeaderTextOffset)
										[
											SNew(STextBlock)
												.Text(FText::FromString(PersistenceWindowStr))
												.Font(HeaderFontInfo)
										]
								]
						]
						+ SVerticalBox::Slot()
						.VAlign(VAlign_Fill)
						.HAlign(HAlign_Fill)
						[
							SNew(SHorizontalBox)
								+ SHorizontalBox::Slot()
								.VAlign(VAlign_Fill)
								.HAlign(HAlign_Left)
								[
									SAssignNew(SegmentsSB, SScrollBox)
									.Orientation(EOrientation::Orient_Horizontal)
								]
								+ SHorizontalBox::Slot()
								.VAlign(VAlign_Fill)
								.HAlign(HAlign_Right)
								.FillWidth(0.25f)
								[
									SNew(SBox)
										.MinDesiredHeight(600.0f)
										.Padding(10)
										[
											SNew(SOverlay)
												+ SOverlay::Slot()
												.VAlign(VAlign_Fill)
												.HAlign(HAlign_Fill)
												[
													SNew(SImage)
														.Image(ActiveAudiosBackgroundBrush)
												]
												+ SOverlay::Slot()
												.VAlign(VAlign_Top)
												.HAlign(HAlign_Fill)
												[
													SAssignNew(ActiveAudiosVB, SScrollBox)
														.Orientation(EOrientation::Orient_Vertical)
												]
										]
								]
						]
				]
		]
		
	];
}

void SDebugChainView::UpdateActiveAudios(const TMap<FString, FAudioPlayer>& ActiveAudios, const TimeRecord& ChainMicros)
{
	if (!ActiveAudiosVB.IsValid() || ActiveAudios.Num() == 0)
	{
		return;
	}

	if (ActiveAudiosVB->GetAllChildren()->Num() > 0)
	{
		ActiveAudiosVB->ClearChildren();
	}

	if (LoadingTB && LoadingTB->GetVisibility() == EVisibility::Visible)
	{
		LoadingTB->SetVisibility(EVisibility::Hidden);
		LoadingTB.Reset();
	}

	for (const TPair<FString, FAudioPlayer>& Audio : ActiveAudios)
	{
		const FAudioPlayer& Value = Audio.Value;

		FString AudioInfoStr = FString::Printf(TEXT("%s - %s \t %s"), *FloatToString(Value.StartTime.GetSeconds()), *FloatToString(Value.EndTime.GetSeconds()), *Value.Name);

		bool bActive = ChainMicros.GetMicros() >= Value.StartTime.GetMicros() && ChainMicros.GetMicros() < Value.EndTime.GetMicros();

		ActiveAudiosVB->AddSlot()
		.Padding(5)
		[
			SNew(STextBlock)
			.Text(FText::FromString(AudioInfoStr))
			.Font(FSlateFontInfo(FPaths::EngineContentDir() / TEXT("Slate/Fonts/Roboto-Bold.ttf"), 14))
			.ColorAndOpacity(bActive ? FLinearColor::Green : FLinearColor::Gray)
		];
	}

	if (!SegmentsSB)
	{
		return;
	}

	for (const TPair<int, TSharedPtr<SDebugSegmentView>>& Segment : CreatedSegments)
	{
		if (!Segment.Value)
		{
			continue;
		}

		Segment.Value->MarkOutdated();
	}

	for (FSegmentInfo Segment : Engine->GetSegments())
	{
		if (TSharedPtr<SDebugSegmentView>* SegmentView = CreatedSegments.Find(Segment.Id))
		{
			if (!Engine || !*SegmentView)
			{
				return;
			}

			(*SegmentView)->Update(Segment);

			const TimeRecord EndTime = Segment.StartTime + Segment.TotalTime;
			const bool bActive = Engine->GetLastMicros() >= Segment.StartTime && Engine->GetLastMicros() <= EndTime;

			(*SegmentView)->ShowActiveBorder(bActive);

			if (bActive)
			{
				SegmentsSB->ScrollDescendantIntoView(*SegmentView);
			}

			continue;
		}

		TSharedPtr<SDebugSegmentView> NewSegment;

		SegmentsSB->AddSlot()
		.Padding(10)
		[
			SAssignNew(NewSegment, SDebugSegmentView).SegmentInfo(Segment)
		];

		NewSegment->Update(Segment);
		CreatedSegments.Add(Segment.Id, NewSegment);

		const TimeRecord EndTime = Segment.StartTime + Segment.TotalTime;
		const bool bActive = Engine->GetLastMicros() >= Segment.StartTime && Engine->GetLastMicros() <= EndTime;

		NewSegment->ShowActiveBorder(bActive);
	}
}

void SDebugChainView::Tick(const FGeometry& AllottedGeometry, const double InCurrentTime, const float InDeltaTime)
{
	SCompoundWidget::Tick(AllottedGeometry, InCurrentTime, InDeltaTime);

	if (!Engine || !ChainMicrosTB)
	{
		return;
	}

	ChainMicrosTB->SetText(FText::FromString(FString::Printf(TEXT("Time: %.2fs"), Engine->GetLastMicros().GetSeconds())));
}

END_SLATE_FUNCTION_BUILD_OPTIMIZATION