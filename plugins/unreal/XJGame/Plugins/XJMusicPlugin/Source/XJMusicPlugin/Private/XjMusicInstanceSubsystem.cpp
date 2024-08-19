// Fill out your copyright notice in the Description page of Project Settings.

#include "XjMusicInstanceSubsystem.h"
#include "Kismet/GameplayStatics.h"
#include "Settings/XJMusicDefaultSettings.h"
#include "Runtime/Engine/Public/AudioDevice.h"
#include "Async/Async.h"
#include "Manager/XjManager.h"
#include "Widgets/SWeakWidget.h"
#include "Sound/SoundConcurrency.h"
#include "Mixer/XjMixer.h"
#include "Manager/XjAudioLoader.h"

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

void UXjMusicInstanceSubsystem::DoOverrideTaxonomy(const FString Taxonomy)
{
	if (!Manager)
	{
		return;
	}

	XjCommand Command;
	Command.Type = XjCommandType::TaxonomyChange;
	Command.Arguments = Taxonomy;

	Manager->PushCommand(Command);
}

void UXjMusicInstanceSubsystem::SetupXJ()
{
	if (IsValid(Manager))
	{
		return;
	}

	AudioLoader = NewObject<UXjAudioLoader>(this);
	if (AudioLoader)
	{
		AudioLoader->Setup();
	}

	Mixer = NewObject<UXjMixer>(this);
	if (Mixer)
	{
		Mixer->Setup();
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
}

void UXjMusicInstanceSubsystem::ShutdownXJ()
{
	DebugChainViewWidget.Reset();

	ActiveAudios.Empty();


	Mixer->Shutdown();

	AudioLoader->Shutdown();


	Manager->MarkPendingKill();

	Mixer->MarkPendingKill();

	AudioLoader->MarkPendingKill();
}

void UXjMusicInstanceSubsystem::AddActiveAudio(const FAudioPlayer& Audio)
{
	if (ActiveAudios.Contains(Audio.Id))
	{
		return;
	}

	ActiveAudios.Add(Audio.Id, Audio);

	UpdateDebugChainView();

	if (!Mixer || !AudioLoader)
	{
		return;
	}

	float DurationSeconds = Audio.EndTime.GetSeconds() - Audio.StartTime.GetSeconds();

	FXjAudioWave SoundWave = AudioLoader->GetOrLoadSoundById(Audio.WaveId, DurationSeconds);
	if (!SoundWave.IsValidToUse())
	{
		return;
	}

	FMixerAudio MixerAudio;
	MixerAudio.Id = Audio.Id;
	MixerAudio.Wave = SoundWave;
	MixerAudio.StartSamples = Audio.StartTime.GetSamples(Mixer->GetSampleRate(), Mixer->GetNumChannels());
	MixerAudio.EndSamples = Audio.EndTime.GetSamples(Mixer->GetSampleRate(), Mixer->GetNumChannels());

	Mixer->AddOrUpdateActiveAudio(MixerAudio);
}

void UXjMusicInstanceSubsystem::UpdateActiveAudio(const FAudioPlayer& Audio)
{
	if (!ActiveAudios.Contains(Audio.Id))
	{
		return;
	}

	ActiveAudios[Audio.Id] = Audio;

	UpdateDebugChainView();

	if (!Mixer || !AudioLoader)
	{
		return;
	}

	float DurationSeconds = Audio.EndTime.GetSeconds() - Audio.StartTime.GetSeconds();

	FXjAudioWave SoundWave = AudioLoader->GetOrLoadSoundById(Audio.WaveId, DurationSeconds);
	if (!SoundWave.IsValidToUse())
	{
		return;
	}

	FMixerAudio MixerAudio;
	MixerAudio.Id = Audio.Id;
	MixerAudio.Wave = SoundWave;
	MixerAudio.StartSamples = Audio.StartTime.GetSamples(Mixer->GetSampleRate(), Mixer->GetNumChannels());
	MixerAudio.EndSamples = Audio.EndTime.GetSamples(Mixer->GetSampleRate(), Mixer->GetNumChannels());

	Mixer->AddOrUpdateActiveAudio(MixerAudio);
}

void UXjMusicInstanceSubsystem::RemoveActiveAudio(const FAudioPlayer& Audio)
{
	if (!ActiveAudios.Contains(Audio.Id))
	{
		return;
	}

	ActiveAudios.Remove(Audio.Id);

	UpdateDebugChainView();

	if (Mixer)
	{
		Mixer->RemoveActiveAudio(Audio.Id);
	}
}

void UXjMusicInstanceSubsystem::OnEnabledShowDebugChain(IConsoleVariable* Var)
{
	if (!Var)
	{
		return;
	}

	int Value = Var->GetInt();

	if (APlayerController* PlayerController = GetWorld()->GetFirstPlayerController())
	{
		PlayerController->SetShowMouseCursor(Value > 0);
	}

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
			if (!GEngine || !Manager)
			{
				return;
			}

			DebugChainViewWidget = SNew(SDebugChainView).Engine(Manager->GetActiveEngine());

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
		ActiveAudios.ValueSort([](const FAudioPlayer& A, const FAudioPlayer& B)
			{
				return A.StartTime.GetMicros() < B.StartTime.GetMicros();
			});

		DebugChainViewWidget->UpdateActiveAudios(ActiveAudios, Manager->GetAtChainMicros());
	}
}
