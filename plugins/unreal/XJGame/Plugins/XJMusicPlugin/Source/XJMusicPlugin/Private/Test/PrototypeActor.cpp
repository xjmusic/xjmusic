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

	WorkSettings DeaultSettings;

	try
	{
		std::string path = "D:/Dev/vgm/vgm.xj";
		engine = new Engine(path, DeaultSettings.controlMode, DeaultSettings.craftAheadSeconds, DeaultSettings.dubAheadSeconds, DeaultSettings.persistenceWindowSeconds);
	
		if (!engine)
		{
			return;
		}

		std::set<const Template*> TemplatesInfo =  engine->getProjectContent()->getTemplates();


		for (const Template* Info : TemplatesInfo)
		{
			FString Name(Info->name.c_str());

			UE_LOG(LogTemp, Warning, TEXT("Imported template: %s"), *Name);
		}
		
		if (TemplatesInfo.size() < 1)
		{
			return;
		}

		const Template* FirstTemplate = *TemplatesInfo.begin();

		engine->start(FirstTemplate->id);
	}
	catch (const std::invalid_argument& e)
	{
		FString str(e.what());
		UE_LOG(LogTemp, Error, TEXT("%s"), *str);
	}
}

void APrototypeActor::BeginDestroy()
{
	Super::BeginDestroy();
}

void APrototypeActor::RunXjOneCycleTick()
{
	auto audios = engine->runCycle(atChainMicros);
	///ASSERT_FALSE(audios.empty());
	for (auto audio : audios) 
	{
		FString name = audio.getAudio()->name.c_str();
		UE_LOG(LogTemp, Warning, TEXT("%s"), *name);
		//ASSERT_TRUE(std::filesystem::exists(subject->getPathToBuildDirectory() / audio.getAudio()->waveformKey));
	}
	//spdlog::info("Ran cycle at {}", atChainMicros);
	atChainMicros += MICROS_PER_CYCLE;
}

void APrototypeActor::Tick(float DeltaTime)
{
	Super::Tick(DeltaTime);

	if (!hasSegmentsDubbedPastMinimumOffset() && isWithinTimeLimit())
	{
		RunXjOneCycleTick();
	}
}

