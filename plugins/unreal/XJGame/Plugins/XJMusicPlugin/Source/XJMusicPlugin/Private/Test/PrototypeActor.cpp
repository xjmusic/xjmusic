// Fill out your copyright notice in the Description page of Project Settings.


#include "Test/PrototypeActor.h"
#include <optional>

APrototypeActor::APrototypeActor()
{
	PrimaryActorTick.bCanEverTick = true;

}

void APrototypeActor::BeginPlay()
{
	Super::BeginPlay();

	//engine = new Engine(std::string("D:/vgm/vgm.xj"), Fabricator::ControlMode::Taxonomy, std::nullopt, std::nullopt, std::nullopt);
}

void APrototypeActor::BeginDestroy()
{
	delete store;
	delete content;
	delete work;

	Super::BeginDestroy();
}

void APrototypeActor::RunXjOneCycleTick()
{
	if (!hasSegmentsDubbedPastMinimumOffset() && isWithinTimeLimit())
	{
		work->runCycle(atChainMicros);

		UE_LOG(LogTemp, Warning, TEXT("Ran cycle at %d"), atChainMicros)

			atChainMicros += MICROS_PER_CYCLE;
	}

	//ensure(hasSegmentsDubbedPastMinimumOffset());
}

void APrototypeActor::Tick(float DeltaTime)
{
	Super::Tick(DeltaTime);

}

