// Fill out your copyright notice in the Description page of Project Settings.


#include "Types/XjOutputSoundNode.h"

#include "ActiveSound.h"
#include "XjMusicInstanceSubsystem.h"
#include "Mixer/XjMixer.h"
#include "Mixer/XjOutput.h"

void UXjOutputSoundNode::ParseNodes(FAudioDevice* AudioDevice, const UPTRINT NodeWaveInstanceHash,
                                    FActiveSound& ActiveSound, const struct FSoundParseParameters& ParseParams, TArray<FWaveInstance*>& WaveInstances)
{
	UXjOutput* Output = GetOutputSoundWave(ActiveSound.GetWorld());
	if(IsValid(Output))
	{
		Output->Parse(AudioDevice, NodeWaveInstanceHash, ActiveSound, ParseParams, WaveInstances);
	}
	else
	{
		ActiveSound.bFinished = false;
	}
}

UXjOutput* UXjOutputSoundNode::GetOutputSoundWave(UWorld* World)
{
	if(!World)
	{
		return nullptr;
	}
	
	UGameInstance* Instance = World->GetGameInstance();
	if(!Instance)
	{
		return nullptr;
	}
	
	UXjMusicInstanceSubsystem* XjMusicInstanceSubsystem = Instance->GetSubsystem<UXjMusicInstanceSubsystem>();
	if(!XjMusicInstanceSubsystem)
	{
		return nullptr;
	}

	UXjMixer* XjMixer = XjMusicInstanceSubsystem->Mixer;
	if(!XjMixer)
	{
		return nullptr;
	}
	
	return XjMixer->Output;
}
