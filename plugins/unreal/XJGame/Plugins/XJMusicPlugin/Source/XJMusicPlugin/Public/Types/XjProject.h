// Fill out your copyright notice in the Description page of Project Settings.

#pragma once

#include "CoreMinimal.h"
#include "UObject/Object.h"
#include "XjProject.generated.h"

UCLASS(BlueprintType)
class XJMUSICPLUGIN_API UXjProject : public UObject
{
	GENERATED_BODY()

public:
	UPROPERTY(VisibleAnywhere, BlueprintReadOnly)
	FString ProjectName;

	UPROPERTY(VisibleAnywhere, BlueprintReadOnly)
	FString ProjectPath;
};
