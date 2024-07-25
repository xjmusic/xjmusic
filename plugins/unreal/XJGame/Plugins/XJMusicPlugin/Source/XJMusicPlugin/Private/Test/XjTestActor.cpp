// Fill out your copyright notice in the Description page of Project Settings.


#include "Test/XjTestActor.h"
#include "XjMusicInstanceSubsystem.h"
#include <Kismet/GameplayStatics.h>
#include <Sound/SoundBase.h>
#include <Sound/SoundWave.h>
#include <Components/AudioComponent.h>

AXjTestActor::AXjTestActor()
{
	PrimaryActorTick.bCanEverTick = true;
}

void AXjTestActor::BeginPlay()
{
	Super::BeginPlay();
	
	UXjMusicInstanceSubsystem* XjMusicInstanceSubsystem = GetWorld()->GetGameInstance()->GetSubsystem<UXjMusicInstanceSubsystem>();
	if (XjMusicInstanceSubsystem)
	{
		XjMusicInstanceSubsystem->RetrieveProjectsContent("D:/Dev/vgm/build");
		XjMusicInstanceSubsystem->TestPlayAllSounds();
	}
}

void AXjTestActor::Tick(float DeltaTime)
{
	Super::Tick(DeltaTime);

}