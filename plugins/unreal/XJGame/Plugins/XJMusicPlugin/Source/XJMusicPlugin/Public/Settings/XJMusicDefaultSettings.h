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

	UPROPERTY(EditAnywhere, Category = Settings)
	bool bDevelopmentMode = false;

	UPROPERTY(EditAnywhere, Category = DevelopmentMode, meta = (EditCondition = "bDevelopmentMode"))
	UDataTable* MockDataDT = nullptr;

	UPROPERTY(EditAnywhere, Category = DevelopmentMode, meta = (EditCondition = "bDevelopmentMode"))
	int MaxAudiosOutputPerCycle = 0;

	UPROPERTY(EditAnywhere, Category = DevelopmentMode, meta = (EditCondition = "bDevelopmentMode"))
	float LatencyBetweenCyclesInSeconds = 0.0f;

};
