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

	UXJMusicDefaultSettings(const FObjectInitializer& obj);

	UPROPERTY(Config, EditAnywhere, Category = Settings)
	FString XjMusicPath;

	UPROPERTY(Config, EditAnywhere, Category = Settings)
	FString XjWorkDirectory;

	FString GetFullWorkPath() const;
};
