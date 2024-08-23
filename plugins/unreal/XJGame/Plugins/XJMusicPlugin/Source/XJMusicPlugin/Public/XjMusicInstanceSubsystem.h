// Fill out your copyright notice in the Description page of Project Settings.

#pragma once

#include "CoreMinimal.h"
#include "Subsystems/GameInstanceSubsystem.h"
#include "Widgets/DebugChainView.h"

#include "XjMusicInstanceSubsystem.generated.h"

UCLASS(DisplayName = "XjSubsystem")
class XJMUSICPLUGIN_API UXjMusicInstanceSubsystem : public UGameInstanceSubsystem
{
	GENERATED_BODY()
	
public:
	
	virtual void Initialize(FSubsystemCollectionBase& Collection) override;

	virtual void Deinitialize() override;

	UFUNCTION(BlueprintCallable)
	void DoOverrideTaxonomy(const FString Taxonomy);

	void SetupXJ();

	void ShutdownXJ();

	void AddActiveAudio(const FAudioPlayer& Audio);

	void UpdateActiveAudio(const FAudioPlayer& Audio);

	void RemoveActiveAudio(const FAudioPlayer& Audio);
	
	const TMap<FString, FAudioPlayer>& GetActiveAudios() const
	{
		return ActiveAudios;
	}

	bool IsAssetsLoading() const;
	
	FString RuntimeProjectDir;
	
	UPROPERTY()
	class UXjProject* XjProjectInstance = nullptr;
	
private:

	void OnEnabledShowDebugChain(class IConsoleVariable* Var);

	void UpdateDebugChainView();

	void RestoreRuntimeProjectDirectory();

	void DeleteRuntimeProjectDirectory();
	
private:

	UPROPERTY()
	class UXjManager* Manager = nullptr;

	UPROPERTY()
	class UXjMixer* Mixer = nullptr;

	UPROPERTY()
	class UXjAudioLoader* AudioLoader = nullptr;

	TSharedPtr<SDebugChainView> DebugChainViewWidget;

	TMap<FString, FAudioPlayer> ActiveAudios;
};