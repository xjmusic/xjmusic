// Fill out your copyright notice in the Description page of Project Settings.


#include "Manager/XjManager.h"
#include <XjMusicInstanceSubsystem.h>
#include <Math/UnrealMathUtility.h>
#include <Engine/XjMainEngine.h>
#include <Tests/MockDataEngine.h>
#include <Settings/XJMusicDefaultSettings.h>

FXjRunnable::FXjRunnable(const FString& PathToProject, const UWorld* World)
{
	check(World);

	std::string PathToProjectStr(TCHAR_TO_UTF8(*PathToProject));
	UE_LOG(LogTemp, Display, TEXT("Path to project: %s"), *FString(PathToProjectStr.c_str()));

	// Crawl the build folder in the folder containing the project file
	FString ProjectPath = PathToProject;
	FString FolderContainingProject = FPaths::GetPath(ProjectPath);
	FString PathToBuildFolder = FPaths::Combine(FolderContainingProject, TEXT("build/"));

	UE_LOG(LogTemp, Display, TEXT("Path to build folder: %s"), *PathToBuildFolder);

	XjMusicSubsystem = World->GetGameInstance()->GetSubsystem<UXjMusicInstanceSubsystem>();
	if (!XjMusicSubsystem)
	{
		UE_LOG(LogTemp, Error, TEXT("Cannot find XjMusicInstanceSubsystem"));
		return;
	}

	XjMusicSubsystem->RetrieveProjectsContent(PathToBuildFolder);

	XjStartTime.SetInSeconds(FPlatformTime::Seconds());

	if (!TryInitMockEngine())
	{
		Engine = MakeShared<TXjMainEngine>();
	}

	if (Engine)
	{
		XjMusicSubsystem->SetActiveEngine(Engine);
		Engine->Setup(PathToProject);
	}
}

bool FXjRunnable::Init()
{
	return true;
}

uint32 FXjRunnable::Run()
{
	while (!bShouldStop)
	{
		float StartFrameTime = FPlatformTime::Seconds();

		FString PlayingAudios = "Scheduled:\n";

		if (!Engine)
		{
			continue;
		}

		TArray<FAudioPlayer> ReceivedAudios = Engine->RunCycle(AtChainMicros.GetMicros());

		for (const FAudioPlayer& Audio : ReceivedAudios)
		{
			float Duration;

			switch (Audio.Event)
			{
			case EAudioEventType::Create:

				Duration = Audio.EndTime.GetMillie() - AtChainMicros.GetMillie();

				if (XjMusicSubsystem->PlayAudioByName(Audio.WaveId, Audio.StartTime.GetMillie(), Duration))
				{
					PlayingAudios += FString::Printf(TEXT("%s start: %f end: %f\n"),
													*Audio.Name, 
													 Audio.StartTime.GetSeconds(), 
													 Audio.EndTime.GetSeconds());
				}

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

		float EndFrameTime = FPlatformTime::Seconds();

		float DeltaTime = EndFrameTime - StartFrameTime;
		float SleepInterval = 1.0f / RunCycleFrequency - (EndFrameTime - StartFrameTime);
		SleepInterval = FMath::Max(SleepInterval, 0.0f);

		FPlatformProcess::Sleep(SleepInterval);

		if (SleepInterval == 0.0f)
		{
			SleepInterval = DeltaTime;
		}

		AtChainMicros.SetInSeconds(AtChainMicros.GetSeconds() + SleepInterval);
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

		if (TMockDataEngine* MockEngine = static_cast<TMockDataEngine*>(Engine.Get()))
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
	UXJMusicDefaultSettings* XjSettings = GetMutableDefault<UXJMusicDefaultSettings>();
	if (!XjSettings)
	{
		return;
	}

	XjRunnable = new FXjRunnable(XjSettings->PathToXjProjectFile, GetWorld());
	XjThread = FRunnableThread::Create(XjRunnable, TEXT("Xj Thread"));
}

void UXjManager::BeginDestroy()
{
	if (XjThread && XjRunnable)
	{
		XjRunnable->Stop();
		XjThread->WaitForCompletion();

		delete XjThread;
		delete XjRunnable;
	}

	Super::BeginDestroy();
}
