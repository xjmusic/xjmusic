// Fill out your copyright notice in the Description page of Project Settings.


#include "Test/XjTestActor.h"
#include "XjMusicInstanceSubsystem.h"
#include <Kismet/GameplayStatics.h>
#include <Sound/SoundBase.h>
#include <Sound/SoundWave.h>

AXjTestActor::AXjTestActor()
{
	PrimaryActorTick.bCanEverTick = true;
}

void AXjTestActor::BeginPlay()
{
	Super::BeginPlay();

	UXjMusicInstanceSubsystem* SB = GetGameInstance()->GetSubsystem<UXjMusicInstanceSubsystem>();

	FString Path = SB->RetriveProjectsInfo();

	USoundWave* SW = SB->GetSoundWaveFromFile(Path);
	if (SW)
	{
		UGameplayStatics::PlaySound2D(GetWorld(), SW);
	}
}

void AXjTestActor::Tick(float DeltaTime)
{
	Super::Tick(DeltaTime);

	GetGameInstance()->GetSubsystem<UXjMusicInstanceSubsystem>()->RunXjOneCycleTick();
}

