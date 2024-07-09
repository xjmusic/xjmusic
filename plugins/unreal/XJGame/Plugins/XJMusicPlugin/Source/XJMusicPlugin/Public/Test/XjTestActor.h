// Fill out your copyright notice in the Description page of Project Settings.

#pragma once

#include "CoreMinimal.h"
#include "GameFramework/Actor.h"
#include "XjTestActor.generated.h"

UCLASS()
class XJMUSICPLUGIN_API AXjTestActor : public AActor
{
	GENERATED_BODY()
	
public:	
	AXjTestActor();

	UPROPERTY(EditAnywhere)
	USoundBase* TestAudio;

protected:
	virtual void BeginPlay() override;

public:	
	virtual void Tick(float DeltaTime) override;

};
