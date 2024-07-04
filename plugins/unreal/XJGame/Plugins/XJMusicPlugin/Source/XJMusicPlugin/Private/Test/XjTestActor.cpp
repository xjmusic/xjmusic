// Fill out your copyright notice in the Description page of Project Settings.


#include "Test/XjTestActor.h"
#include "XjMusicInstanceSubsystem.h"

AXjTestActor::AXjTestActor()
{
	PrimaryActorTick.bCanEverTick = true;
}

void AXjTestActor::BeginPlay()
{
	Super::BeginPlay();
	
}

void AXjTestActor::Tick(float DeltaTime)
{
	Super::Tick(DeltaTime);

	GetGameInstance()->GetSubsystem<UXjMusicInstanceSubsystem>()->RunXjOneCycleTick();
}

