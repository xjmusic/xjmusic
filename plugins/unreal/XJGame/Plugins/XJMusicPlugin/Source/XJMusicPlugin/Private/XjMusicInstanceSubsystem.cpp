// Fill out your copyright notice in the Description page of Project Settings.

#include "XjMusicInstanceSubsystem.h"
#include "Kismet/GameplayStatics.h"
#include <Settings/XJMusicDefaultSettings.h>
#include <Sound/SoundBase.h>
#include <Sound/SoundWave.h>
#include <Engine/StreamableManager.h>
#include <Engine/AssetManager.h>
#include <Engine/ObjectLibrary.h>
#include <Misc/FileHelper.h>
#include <Runtime/Engine/Public/AudioDevice.h>
#include <Async/Async.h>
#include <Manager/XjManager.h>
#include <Widgets/SWeakWidget.h>
#include <Sound/SoundConcurrency.h>

static TAutoConsoleVariable<int32> CVarShowDebugChain(
	TEXT("xj.showdebug"), 
	1, 
	TEXT("Show debug view of the chain schedule"), 
	ECVF_Default);

void UXjMusicInstanceSubsystem::Initialize(FSubsystemCollectionBase& Collection)
{
	Super::Initialize(Collection);

	CVarShowDebugChain->SetOnChangedCallback(FConsoleVariableDelegate::CreateUObject(this, &UXjMusicInstanceSubsystem::OnEnabledShowDebugChain));
}

void UXjMusicInstanceSubsystem::Deinitialize()
{
	Super::Deinitialize();
}

void UXjMusicInstanceSubsystem::SetupXJ()
{
	if (IsValid(Manager))
	{
		return;
	}

	SoundConcurrency = NewObject<USoundConcurrency>();
	if (SoundConcurrency)
	{
		FSoundConcurrencySettings Settings;
		Settings.MaxCount = 64;
		Settings.ResolutionRule = EMaxConcurrentResolutionRule::StopOldest;

		SoundConcurrency->Concurrency = Settings;
	}

	Manager = NewObject<UXjManager>(this);
	if (Manager)
	{
		Manager->Setup();
	}

	if (CVarShowDebugChain->GetInt() > 0)
	{
		OnEnabledShowDebugChain(CVarShowDebugChain->AsVariable());
	}

	FTimerDelegate CheckDelegate;
	CheckDelegate.BindUObject(this, &UXjMusicInstanceSubsystem::CheckActiveAudios);

	GetWorld()->GetTimerManager().SetTimer(CheckTimerHandle, CheckDelegate, 1.0f, true);
}

void UXjMusicInstanceSubsystem::RetrieveProjectsContent(const FString& Directory)
{
	InitQuartz();

	TArray<FString> WavFiles;

	UXJMusicDefaultSettings* XjSettings = GetMutableDefault<UXJMusicDefaultSettings>();
	if (!XjSettings)
	{
		return;
	}

	FString WorkPath = Directory;

	IPlatformFile& PlatformFile = FPlatformFileManager::Get().GetPlatformFile();

	if (!PlatformFile.DirectoryExists(*WorkPath))
	{
		return;
	}

	TArray<FString> FoundAudioFilesPaths;

	PlatformFile.FindFilesRecursively(FoundAudioFilesPaths, *WorkPath, *AudioExtension);

	for (const FString& Path : FoundAudioFilesPaths)
	{
		FString Name = FPaths::GetCleanFilename(Path);
		AudioPathsByNameLookup.Add(Name, Path);
	}
}

void UXjMusicInstanceSubsystem::SetActiveEngine(const TWeakPtr<TEngineBase>& Engine)
{
	ActiveEngine = Engine;
}

bool UXjMusicInstanceSubsystem::PlayAudio(const FAudioPlayer& Audio)
{	
	AsyncTask(ENamedThreads::GameThread, [this, Audio]()
		{
			float DurationSeconds = Audio.EndTime.GetSeconds() - Audio.StartTime.GetSeconds();

			USoundWave* SoundWave = GetSoundWaveById(Audio.WaveId, DurationSeconds);
			if (!SoundWave)
			{
				return;
			}

			if (!FMath::IsNearlyEqual(DurationSeconds, SoundWave->Duration, 0.001f))
			{
				UE_LOG(LogTemp, Error, TEXT("Audio: %s actual %f and planned %f time are different!"), *Audio.Name, SoundWave->Duration, DurationSeconds);
			}

			UAudioComponent* NewAudioComponent = UGameplayStatics::CreateSound2D(GetWorld(), SoundWave, 
																					1.0f, 1.0f, 0.0f, SoundConcurrency);
			if (NewAudioComponent)
			{
				FQuartzQuantizationBoundary Boundary;
				Boundary.Quantization = EQuartzCommandQuantization::ThirtySecondNote;
				Boundary.CountingReferencePoint = EQuarztQuantizationReference::TransportRelative;
				Boundary.Multiplier = Audio.StartTime.GetMillie();

				NewAudioComponent->PlayQuantized(GetWorld(), QuartzClockHandle, Boundary, {});

				SoundsMapCriticalSection.Lock();

				if (!SoundsMap.Contains(Audio.Id))
				{
					SoundsMap.Add(Audio.Id, {});
				}

				SoundsMap.Add(Audio.Id, NewAudioComponent);

				SoundsMapCriticalSection.Unlock();
			}
		});

	return true;
}

void UXjMusicInstanceSubsystem::CheckActiveAudios()
{

}

void UXjMusicInstanceSubsystem::AddActiveAudio(const FAudioPlayer& Audio)
{
	if (ActiveAudios.Contains(Audio.Id))
	{
		return;
	}

	ActiveAudios.Add(Audio.Id, Audio);

	UpdateDebugChainView();

	PlayAudio(Audio);
}

void UXjMusicInstanceSubsystem::UpdateActiveAudio(const FAudioPlayer& Audio)
{
	if (!ActiveAudios.Contains(Audio.Id))
	{
		return;
	}

	ActiveAudios[Audio.Id] = Audio;

	UpdateDebugChainView();

	//Update end time
}

void UXjMusicInstanceSubsystem::RemoveActiveAudio(const FAudioPlayer& Audio)
{
	if (!ActiveAudios.Contains(Audio.Id))
	{
		return;
	}

	ActiveAudios.Remove(Audio.Id);

	UpdateDebugChainView();
}

USoundWave* UXjMusicInstanceSubsystem::GetSoundWaveById(const FString& Id, const float Duration)
{
	if (!AudioPathsByNameLookup.Contains(Id))
	{
		return nullptr;
	}

	FString FilePath = AudioPathsByNameLookup[Id];

	USoundWave* SoundWave = NewObject<USoundWave>(USoundWave::StaticClass());
	if (!SoundWave)
	{
		UE_LOG(LogTemp, Error, TEXT("Failed to create USoundWave object"));
		return nullptr;
	}

	TArray<uint8> RawFile;
	if (!FFileHelper::LoadFileToArray(RawFile, *FilePath))
	{
		return nullptr;
	}

	uint32 NeededChunkSize = RawFile.Num();

	FWaveModInfo WaveInfo;
	if (WaveInfo.ReadWaveInfo(RawFile.GetData(), RawFile.Num()))
	{
		check(WaveInfo.pChannels);
		const uint32 Channels = *WaveInfo.pChannels;
		
		check(WaveInfo.pSamplesPerSec);
		const uint32 SampleRate = *WaveInfo.pSamplesPerSec;

		check(WaveInfo.pBlockAlign);
		const uint32 BlockAlign = *WaveInfo.pBlockAlign;

		check(WaveInfo.pWaveDataSize);
		const uint32 WaveData = *WaveInfo.pWaveDataSize;

		const uint32 HeaderSize = RawFile.Num() - WaveData;

		const uint32 NewSize = HeaderSize + Duration * SampleRate * BlockAlign;

		NeededChunkSize = FMath::Min(NeededChunkSize, NewSize);

		SoundWave->NumChannels = Channels;
		SoundWave->SetSampleRate(SampleRate);
	}

	SoundWave->SoundGroup = ESoundGroup::SOUNDGROUP_Music;
	SoundWave->RawData.Lock(LOCK_READ_WRITE);

	void* LockedData = SoundWave->RawData.Realloc(NeededChunkSize);
	if (LockedData)
	{
		FMemory::Memcpy(LockedData, RawFile.GetData(), NeededChunkSize);
	}

	SoundWave->RawData.Unlock();

	SoundWave->RawData.SetBulkDataFlags(BULKDATA_ForceInlinePayload);
	SoundWave->InvalidateCompressedData();
	
	return SoundWave;
}

void UXjMusicInstanceSubsystem::InitQuartz()
{
	UQuartzSubsystem* QuartzSubsystem = UQuartzSubsystem::Get(GetWorld());
	if (QuartzSubsystem)
	{
		FQuartzTimeSignature TimeSignatures;
		TimeSignatures.BeatType = EQuartzTimeSignatureQuantization::ThirtySecondNote;
		TimeSignatures.NumBeats = 1;

		FQuartzClockSettings Settings;
		Settings.TimeSignature = TimeSignatures;

		QuartzClockHandle = QuartzSubsystem->CreateNewClock(GetWorld(), "XJ Clock", Settings);
		if (QuartzClockHandle)
		{
			QuartzClockHandle->SetThirtySecondNotesPerMinute(GetWorld(), {}, {}, QuartzClockHandle, 60000);
		
			QuartzClockHandle->StartClock(GetWorld(), QuartzClockHandle);
		}
	}
}

void UXjMusicInstanceSubsystem::OnEnabledShowDebugChain(IConsoleVariable* Var)
{
	if (!Var)
	{
		return;
	}

	int Value = Var->GetInt();

	if (Value <= 0)
	{
		if (DebugChainViewWidget)
		{
			DebugChainViewWidget->SetVisibility(EVisibility::Hidden);
		}
	}
	else
	{
		if (DebugChainViewWidget)
		{
			DebugChainViewWidget->SetVisibility(EVisibility::Visible);
		}
		else
		{
			DebugChainViewWidget = SNew(SDebugChainView).Engine(ActiveEngine);

			if (!GEngine)
			{
				return;
			}

			GEngine->GameViewport->AddViewportWidgetContent(
				SNew(SWeakWidget).PossiblyNullContent(DebugChainViewWidget.ToSharedRef()));

			DebugChainViewWidget->SetVisibility(EVisibility::Visible);
		}

		UpdateDebugChainView();
	}
}

void UXjMusicInstanceSubsystem::UpdateDebugChainView()
{
	if (!IsInGameThread())
	{
		AsyncTask(ENamedThreads::GameThread, [this]()
			{
				UpdateDebugChainView();
			});

		return;
	}

	if (!Manager)
	{
		return;
	}

	if (DebugChainViewWidget)
	{
		DebugChainViewWidget->UpdateActiveAudios(ActiveAudios, Manager->GetAtChainMicros());
	}
}
