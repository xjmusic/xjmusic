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

	//content = new ContentEntityStore();
	//fake = new ContentFixtures();
	//fake->project1 = ContentFixtures::buildProject("fish");
	//fake->library1 = ContentFixtures::buildLibrary(&fake->project1, "test");
	//fake->generateFixtures(content, GENERATED_FIXTURE_COMPLEXITY);
	//
	//Template tmpl = **content->getTemplates().begin();
	//tmpl.shipKey = "complex_library_test";
	//tmpl.config = "outputEncoding=\"PCM_SIGNED\"\noutputContainer = \"WAV\"\ndeltaArcEnabled = false\n";
	//content->put(tmpl);
	//
	//store = new SegmentEntityStore();
	//FabricatorFactory* factory = new FabricatorFactory(store);
	//
	//work = new WorkManager(factory, store);

	//auto settings = WorkSettings();
	//settings.inputTemplate = tmpl;
	//
	//if (work)
	//{
	//	work->start(content, settings);
	//}
}

void AXjTestActor::BeginDestroy()
{
	delete store;
	delete content;
	delete work;

	Super::BeginDestroy();
}

void AXjTestActor::RunXjOneCycleTick()
{
	if (!hasSegmentsDubbedPastMinimumOffset() && isWithinTimeLimit())
	{
		work->runCycle(atChainMicros);

		UE_LOG(LogTemp, Warning, TEXT("Ran cycle at %d"), atChainMicros)

			atChainMicros += MICROS_PER_CYCLE;
	}

	//ensure(hasSegmentsDubbedPastMinimumOffset());
}

void AXjTestActor::Tick(float DeltaTime)
{
	Super::Tick(DeltaTime);

	RunXjOneCycleTick();
}

