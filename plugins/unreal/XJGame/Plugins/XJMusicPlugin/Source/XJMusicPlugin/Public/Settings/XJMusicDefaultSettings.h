// Fill out your copyright notice in the Description page of Project Settings.

#pragma once

#include "CoreMinimal.h"
#include "UObject/NoExportTypes.h"
#include "XJMusicDefaultSettings.generated.h"

UCLASS(config = "XJMusicConfig")
class XJMUSICPLUGIN_API UXJMusicDefaultSettings : public UObject
{
	GENERATED_BODY()

public:
	UPROPERTY(Config, EditAnywhere, Category = Settings)
	FString PathToXjMusicWorkstation;

	UPROPERTY(Config, EditAnywhere, Category = Settings)
	FString XjProjectFolder = "D:/Dev/vgm/";

	UPROPERTY(Config, EditAnywhere, Category = Settings)
	FString XjProjectFile = "vgm.xj";
};
