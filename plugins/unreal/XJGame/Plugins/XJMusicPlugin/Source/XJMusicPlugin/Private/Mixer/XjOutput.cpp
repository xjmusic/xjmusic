// Fill out your copyright notice in the Description page of Project Settings.


#include "Mixer/XjOutput.h"

int32 UXjOutput::OnGeneratePCMAudio(TArray<uint8>& OutAudio, int32 NumSamples)
{
	if (OnGeneratePCMData.IsBound())
	{
		return OnGeneratePCMData.Execute(OutAudio, NumSamples);
	}

	return 0;
}
