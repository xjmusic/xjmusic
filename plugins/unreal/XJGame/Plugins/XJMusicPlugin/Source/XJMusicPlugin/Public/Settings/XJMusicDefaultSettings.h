// Fill out your copyright notice in the Description page of Project Settings.

#pragma once

#include "CoreMinimal.h"
#include "Types/XjProject.h"
#include "XJMusicDefaultSettings.generated.h"

UCLASS(config = "XJMusicConfig")
class XJMUSICPLUGIN_API UXJMusicDefaultSettings : public UObject
{
	GENERATED_BODY()

public:
	
	UPROPERTY(Config, EditAnywhere, Category = Settings, meta=(AllowedClasses="XjProject"))
	FSoftObjectPath LaunchProject;
	
	UPROPERTY(Config, EditAnywhere, Category = Settings)
	FString PathToXjMusicWorkstation;

	UPROPERTY(EditAnywhere, Category = Settings)
	bool bDevelopmentMode = false;

	UPROPERTY(EditAnywhere, Category = DevelopmentMode, meta = (EditCondition = "bDevelopmentMode"))
	class UDataTable* MockDataDT = nullptr;

	UPROPERTY(EditAnywhere, Category = DevelopmentMode, meta = (EditCondition = "bDevelopmentMode"))
	int MaxAudiosOutputPerCycle = 0;

	UPROPERTY(EditAnywhere, Category = DevelopmentMode, meta = (EditCondition = "bDevelopmentMode"))
	float LatencyBetweenCyclesInSeconds = 0.0f;

};
