// Fill out your copyright notice in the Description page of Project Settings.


#include "Manager/XjManager.h"
#include "XjMusicInstanceSubsystem.h"
#include "Math/UnrealMathUtility.h"
#include "Engine/XjMainEngine.h"
#include "Tests/MockDataEngine.h"
#include "Settings/XJMusicDefaultSettings.h"
#include "Manager/XjAudioLoader.h"

void UXjManager::Setup(class UXjMusicInstanceSubsystem* XjSubsystem, UXjAudioLoader* AudioLoader)
{
	if (!AudioLoader || !XjSubsystem || !XjSubsystem->XjProjectInstance)
	{
		return;
	}

	if (Engine)
	{
		return;
	}

	XjMusicSubsystem = XjSubsystem;

	XjStartTime.SetInSeconds(FPlatformTime::Seconds());

	if (!TryInitMockEngine())
	{
		Engine = MakeShared<TXjMainEngine>();
	}

	FString PathToProject = XjMusicSubsystem->GetRuntimeProjectDirectory() + "/" + XjMusicSubsystem->XjProjectInstance->ProjectName + ".xj";

	Engine->Setup(PathToProject);

	TSharedPtr<FStreamableHandle> StreamHandle = AudioLoader->InitialAssetsStream;

	bool Bound = StreamHandle->BindCompleteDelegate(FStreamableDelegate::CreateUObject(this, &UXjManager::OnAssetsLoaded));
	if (!Bound)
	{
		OnAssetsLoaded();
	}
}
 
void UXjManager::Tick(float DeltaTime)
{
	check(Engine);
	check(XjMusicSubsystem);

	if (FramTimeAccumulation < RunCycleInterval)
	{
		FramTimeAccumulation += DeltaTime;

		FPlatformProcess::Sleep(0.0f);
		return;
	}

	while (!Commands.IsEmpty())
	{
		if (!Engine)
		{
			break;
		}

		XjCommand Command;
		Commands.Dequeue(Command);

		switch (Command.Type)
		{
		case XjCommandType::TaxonomyChange:
			Engine->DoOverrideTaxonomy(Command.Arguments);
			break;

		case XjCommandType::MacrosChange:
			Engine->DoOverrideMacro(Command.Arguments);
			break;

		case XjCommandType::IntensityChange:
			Engine->DoOverrideIntensity(Command.FloatValue);
			break;
		}
	}


	TArray<FAudioPlayer> ReceivedAudios = Engine->RunCycle(AtChainMicros.GetMicros());

	for (const FAudioPlayer& Audio : ReceivedAudios)
	{
		switch (Audio.Event)
		{
		case EAudioEventType::Create:
			XjMusicSubsystem->AddActiveAudio(Audio);
			break;

		case EAudioEventType::Update:
			XjMusicSubsystem->UpdateActiveAudio(Audio);
			break;

		case EAudioEventType::Delete:
			XjMusicSubsystem->RemoveActiveAudio(Audio);
			break;

		}
	}

	AtChainMicros.SetInSeconds(AtChainMicros.GetSeconds() + FramTimeAccumulation);

	FramTimeAccumulation = 0.0f;
}

bool UXjManager::TryInitMockEngine()
{
	UXJMusicDefaultSettings* XjSettings = GetMutableDefault<UXJMusicDefaultSettings>();
	if (XjSettings && XjSettings->bDevelopmentMode)
	{
		Engine = MakeShared<TMockDataEngine>();
		
		if (TMockDataEngine* MockEngine = StaticCast<TMockDataEngine*>(Engine.Get()))
		{
			MockEngine->SetMockData(XjSettings->MockDataDT);
			MockEngine->MaxAudiosOutputPerCycle = XjSettings->MaxAudiosOutputPerCycle;
			MockEngine->LatencyBetweenCyclesInSeconds = XjSettings->LatencyBetweenCyclesInSeconds;
		}

		return true;
	}

	return false;
}

void UXjManager::OnAssetsLoaded()
{
	bCanTick = true;
}
