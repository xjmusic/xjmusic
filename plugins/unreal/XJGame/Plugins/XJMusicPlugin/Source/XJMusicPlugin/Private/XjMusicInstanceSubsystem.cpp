// Fill out your copyright notice in the Description page of Project Settings.


#include "XjMusicInstanceSubsystem.h"
#include "Kismet/GameplayStatics.h"
#include <Settings/XJMusicDefaultSettings.h>
#include <Sound/SoundBase.h>
#include <Sound/SoundWave.h>
#include <Engine/StreamableManager.h>
#include <Engine/AssetManager.h>
#include <Engine/ObjectLibrary.h>
#include <Misc/FileHelper.h>

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
	TArray<FString> WavFiles;

	UXJMusicDefaultSettings* XjSettings = GetMutableDefault<UXJMusicDefaultSettings>();
	if (!XjSettings)
	{
		return;
	}

	FString WorkPath = XjSettings->GetFullWorkPath();

	IPlatformFile& PlatformFile = FPlatformFileManager::Get().GetPlatformFile();

	if (!PlatformFile.DirectoryExists(*WorkPath))
	{
		return;
	}
}

USoundWave* UXjMusicInstanceSubsystem::GetSoundWaveFromFile(const FString& filePath)
{
	USoundWave* SoundWave = NewObject<USoundWave>(USoundWave::StaticClass());
	if (!SoundWave)
	{
		UE_LOG(LogTemp, Error, TEXT("Failed to create USoundWave object"));
		return nullptr;
	}

	TArray<uint8> RawFile;
	if (!FFileHelper::LoadFileToArray(RawFile, *filePath))
	{
		return nullptr;
	}

	SoundWave->SoundGroup = ESoundGroup::SOUNDGROUP_Default;
	SoundWave->NumChannels = 2; 

	SoundWave->RawData.Lock(LOCK_READ_WRITE);

	void* LockedData = SoundWave->RawData.Realloc(RawFile.Num());
	if (LockedData)
	{
		FMemory::Memcpy(LockedData, RawFile.GetData(), RawFile.Num());
	}

	SoundWave->RawData.Unlock();

	SoundWave->RawData.SetBulkDataFlags(BULKDATA_ForceInlinePayload);
	SoundWave->InvalidateCompressedData();
	SoundWave->bNeedsThumbnailGeneration = true;

	return SoundWave;
}
