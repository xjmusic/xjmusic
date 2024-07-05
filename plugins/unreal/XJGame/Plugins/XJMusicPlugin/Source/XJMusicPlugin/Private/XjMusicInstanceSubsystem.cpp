// Fill out your copyright notice in the Description page of Project Settings.


#include "XjMusicInstanceSubsystem.h"
#include <Settings/XJMusicDefaultSettings.h>


void UXjMusicInstanceSubsystem::Initialize(FSubsystemCollectionBase& Collection)
{
	Super::Initialize(Collection);

	content = new ContentEntityStore();
	fake = new ContentFixtures();
	fake->project1 = ContentFixtures::buildProject("fish");
	fake->library1 = ContentFixtures::buildLibrary(&fake->project1, "test");
	fake->generateFixtures(content, GENERATED_FIXTURE_COMPLEXITY);
	
	Template tmpl = **content->getTemplates().begin();
	tmpl.shipKey = "complex_library_test";
	tmpl.config = "outputEncoding=\"PCM_SIGNED\"\noutputContainer = \"WAV\"\ndeltaArcEnabled = false\n";
	content->put(tmpl);
	
	store = new SegmentEntityStore(); 
	FabricatorFactory* factory = new FabricatorFactory(store);
	
	work = new WorkManager(factory, store);
	
	auto settings = WorkSettings();
	settings.inputTemplate = tmpl;
	
	if (work)
	{
		work->start(content, settings);
	}
}

void UXjMusicInstanceSubsystem::Deinitialize()
{
	delete store;
	delete fake;
	delete content;
	delete work;

	Super::Deinitialize();
}

void UXjMusicInstanceSubsystem::RunXjOneCycleTick()
{
	if(!hasSegmentsDubbedPastMinimumOffset() && isWithinTimeLimit()) 
	{
		work->runCycle(atChainMicros);

		UE_LOG(LogTemp, Warning, TEXT("Ran cycle at %d"), atChainMicros)

		atChainMicros += MICROS_PER_CYCLE;
	}
	
	//ensure(hasSegmentsDubbedPastMinimumOffset());
}

void UXjMusicInstanceSubsystem::RetriveProjectsInfo()
{
	IFileManager& FileManager = IFileManager::Get();

	TArray<FString> WavFiles;

	UXJMusicDefaultSettings* XjSettings = GetMutableDefault<UXJMusicDefaultSettings>();
	if (!XjSettings)
	{
		return;
	}

	FileManager.FindFilesRecursive(WavFiles, *XjSettings->GetFullWorkPath(), TEXT("*.wav"), true, false);

	ImportedAudioFiles = WavFiles;

	for (const FString& FilePath : WavFiles)
	{
		UE_LOG(LogTemp, Log, TEXT("Found .wav file: %s"), *FilePath);
	}
}
