// Fill out your copyright notice in the Description page of Project Settings.


#include "Test/PrototypeActor.h"
#include <optional>
#include <set>

APrototypeActor::APrototypeActor()
{
	PrimaryActorTick.bCanEverTick = true;

}

void APrototypeActor::BeginPlay()
{
	Super::BeginPlay();

	std::string Path("D:\\vgm\\vgm.xj");

	engine = new Engine(Path, Fabricator::ControlMode::Taxonomy, std::nullopt, std::nullopt, std::nullopt);
	if (engine)
	{
		std::set<const Template*> Templates =  engine->getProjectContent()->getTemplates();
		for (const Template* Info : Templates)
		{
			FString Name = Info->name.c_str();
			UE_LOG(LogTemp, Warning, TEXT("Loaded template: %s"), *Name);
		}
	}
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

