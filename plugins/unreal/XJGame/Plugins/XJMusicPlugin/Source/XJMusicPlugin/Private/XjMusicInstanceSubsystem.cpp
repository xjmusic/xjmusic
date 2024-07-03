// Fill out your copyright notice in the Description page of Project Settings.


#include "XjMusicInstanceSubsystem.h"


void UXjMusicInstanceSubsystem::Initialize(FSubsystemCollectionBase& Collection)
{
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
	//
	//auto settings = WorkSettings();
	//settings.inputTemplate = tmpl;
	//
	//if (work)
	//{
	//	work->start(content, settings);
	//}
}

void UXjMusicInstanceSubsystem::Deinitialize()
{
}
