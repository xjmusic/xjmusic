// Fill out your copyright notice in the Description page of Project Settings.


#include "Manager/XjManager.h"
#include <XjMusicInstanceSubsystem.h>
#include <Math/UnrealMathUtility.h>
#include <Engine/XjMainEngine.h>
#include <Tests/MockDataEngine.h>
#include <Settings/XJMusicDefaultSettings.h>

FXjRunnable::FXjRunnable(const FString& PathToProjectFile, const UWorld* World)
{
	check(World);

	std::string PathToProjectStr(TCHAR_TO_UTF8(*PathToProjectFile));
	UE_LOG(LogTemp, Display, TEXT("Path to project: %s"), *FString(PathToProjectStr.c_str()));

	// Crawl the build folder in the folder containing the project file
	FString ProjectPath = PathToProjectFile;
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
		Engine->Setup(PathToProjectFile);
	}

	LastFramTime = FPlatformTime::Seconds();
}

bool FXjRunnable::Init()
{
	return true;
}

uint32 FXjRunnable::Run()
{
	while (!bShouldStop)
	{
		if (!Engine)
		{
			continue;
		}

		const double CurrentTime = FPlatformTime::Seconds();
		const double DeltaTime = CurrentTime - LastFramTime;

		if (DeltaTime < (1.0f / RunCycleFrequency))
		{
			FPlatformProcess::Sleep(0.0f);
			continue;
		}

		LastFramTime = CurrentTime;

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
	UXJMusicDefaultSettings* XjSettings = GetMutableDefault<UXJMusicDefaultSettings>();
	if (!XjSettings)
	{
		return;
	}

	XjRunnable = MakeShared<FXjRunnable>(XjSettings->PathToXjProjectFile, GetWorld());
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
