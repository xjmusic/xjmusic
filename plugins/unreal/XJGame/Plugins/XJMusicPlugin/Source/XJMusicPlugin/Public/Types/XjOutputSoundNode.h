// Fill out your copyright notice in the Description page of Project Settings.

#pragma once

#include "CoreMinimal.h"
#include "Sound/SoundNode.h"
#include "XjOutputSoundNode.generated.h"

class UXjMixer;
class UXjOutput;

UCLASS(hidecategories=Object, editinlinenew, meta=( DisplayName="XJ Player" ))
class XJMUSICPLUGIN_API UXjOutputSoundNode : public USoundNode
{
	GENERATED_BODY()

public:
	
	virtual void ParseNodes(FAudioDevice* AudioDevice, const UPTRINT NodeWaveInstanceHash, FActiveSound& ActiveSound,
							const struct FSoundParseParameters& ParseParams, TArray<FWaveInstance*>& WaveInstances) override;
	
	virtual int32 GetMaxChildNodes() const override
	{
		return 0;
	}
	
	virtual float GetDuration() override
	{
		return INDEFINITELY_LOOPING_DURATION;
	}
	
	virtual bool IsPlayWhenSilent() const override
	{
		return true;
	}
	
	virtual int32 GetNumSounds(const UPTRINT NodeWaveInstanceHash, FActiveSound& ActiveSound) const override
	{
		return 1;
	}

private:

	UXjOutput* GetOutputSoundWave(UWorld* World);
};
