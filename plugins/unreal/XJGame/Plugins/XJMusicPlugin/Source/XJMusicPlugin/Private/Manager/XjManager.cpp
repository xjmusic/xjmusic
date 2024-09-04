// Fill out your copyright notice in the Description page of Project Settings.


#include "Manager/XjManager.h"
#include <XjMusicInstanceSubsystem.h>
#include <Math/UnrealMathUtility.h>
#include <Engine/XjMainEngine.h>
#include <Tests/MockDataEngine.h>
#include <Settings/XJMusicDefaultSettings.h>

FXjRunnable::FXjRunnable(UWorld* World)
{
	check(World);

	XjMusicSubsystem = World->GetGameInstance()->GetSubsystem<UXjMusicInstanceSubsystem>();
}

bool FXjRunnable::Init()
{
	XjStartTime.SetInSeconds(FPlatformTime::Seconds());

	if (!TryInitMockEngine())
	{
		Engine = MakeShared<TXjMainEngine>();
	}

	if (Engine && XjMusicSubsystem->XjProjectInstance)
	{
		Engine->Setup(XjMusicSubsystem->GetRuntimeProjectDirectory() + "/" + XjMusicSubsystem->XjProjectInstance->ProjectName + ".xj");
	}

	StreamHandle = XjMusicSubsystem->AudioLoader->InitialAssetsStream;

	LastFramTime = FPlatformTime::Seconds();

	return true;
}

uint32 FXjRunnable::Run()
{
	while (!bShouldStop)
	{
		check(Engine);
		check(XjMusicSubsystem);

		if (StreamHandle->IsLoadingInProgress())
		{
			LastFramTime = FPlatformTime::Seconds();

			FPlatformProcess::Sleep(0.0f);
			continue;
		}
		else if (!bPreDecompression)
		{
			bPreDecompression = true;
			
			UXjAudioLoader* AudioLoader = XjMusicSubsystem->AudioLoader;
			check(AudioLoader);

			AudioLoader->DecompressAll();

			LastFramTime = FPlatformTime::Seconds();
		}

		const double CurrentTime = FPlatformTime::Seconds();
		const double DeltaTime = CurrentTime - LastFramTime;

		if (DeltaTime < (1.0f / RunCycleFrequency))
		{
			FPlatformProcess::Sleep(0.0f);
			continue;
		}

		LastFramTime = CurrentTime;


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

		AtChainMicros.SetInSeconds(AtChainMicros.GetSeconds() + DeltaTime);
	}

	return 0;
}

void FXjRunnable::Stop()
{
	bShouldStop = true;
}

bool FXjRunnable::TryInitMockEngine()
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

void UXjManager::Setup()
{
	XjRunnable = MakeShared<FXjRunnable>(GetWorld());
	XjThread = TSharedPtr<FRunnableThread>(FRunnableThread::Create(XjRunnable.Get(), TEXT("Xj Thread")));
}

void UXjManager::BeginDestroy()
{
	if (XjThread && XjRunnable)
	{
		XjRunnable->Stop();
		XjThread->WaitForCompletion();
	}

	Super::BeginDestroy();
}