// Fill out your copyright notice in the Description page of Project Settings.

#pragma once

#include "CoreMinimal.h"
#include "UObject/NoExportTypes.h"
#include "UObject/SoftObjectPath.h"
#include "Engine/StreamableManager.h"
#include "XjAudioLoader.generated.h"

USTRUCT()
struct FXjAudioWave
{
	GENERATED_BODY();

public:
	FXjAudioWave() = default;
	~FXjAudioWave();

	FXjAudioWave(const FXjAudioWave& Other);
	const FXjAudioWave& operator = (const FXjAudioWave& Other);

	UPROPERTY()
	USoundWave* Wave;

	int32 NumSamples = 0;
	int16* SamplesData = nullptr;

	void LoadData(USoundWave* NewWave);

	void UnLoadData();

	bool IsValidToUse() const;

};

USTRUCT()
struct FFileData
{
	GENERATED_BODY()

	UPROPERTY()
	FString Path;

	UPROPERTY()
	TArray<uint8> Content;
};

UCLASS()
class XJMUSICPLUGIN_API UXjAudioLoader : public UObject
{
	GENERATED_BODY()

public:

	void Setup();

	void Shutdown();

	FXjAudioWave GetSoundById(const FString& Id);

	bool IsLoading() const
	{
		return InitialAssetsStream.IsValid() && InitialAssetsStream->IsLoadingInProgress();
	}

private:

	FStreamableManager StreamableManager;
	
	TSharedPtr<FStreamableHandle> InitialAssetsStream;

	FCriticalSection StreamAccessCriticalSection;
	
	TMap<FString, FSoftObjectPath> AudiosSoftReferences;
	TMap<FString, FXjAudioWave> CachedAudios;

	void RetrieveProjectsContent(const FString& Directory);

	UPROPERTY()
	TArray<FFileData> Files;

	TArray<uint8> ReadFile(const FString& FilePath) const;

	void WriteFile(const FString& FilePath, const TArray<uint8>& Content) const;

	UFUNCTION()
	void StoreDirectory(const FString& DirectoryPath);

	UFUNCTION()
	void RestoreDirectory(const FString& DestinationPath) const;

	UFUNCTION()
	void DeleteDirectory(const FString& DirectoryPath) const;
};
