// Fill out your copyright notice in the Description page of Project Settings.

#pragma once

#include "CoreMinimal.h"
#include "Subsystems/GameInstanceSubsystem.h"
#include "Widgets/DebugChainView.h"
#include "XjMusicInstanceSubsystem.generated.h"

class UXjProject;
class UXjManager;
class UXjMixer;
class UXjAudioLoader;
class UXJMusicDefaultSettings;

UCLASS(DisplayName = "XjSubsystem")
class XJMUSICPLUGIN_API UXjMusicInstanceSubsystem : public UGameInstanceSubsystem
{
	GENERATED_BODY()
	
public:
	
	virtual void Initialize(FSubsystemCollectionBase& Collection) override;

	virtual void Deinitialize() override;

	UFUNCTION(BlueprintCallable)
	void DoOverrideTaxonomy(const FString Taxonomy);

	UFUNCTION(BlueprintCallable)
	void DoOverrideMacro(const FString Macro);

	UFUNCTION(BlueprintCallable)
	void DoOverrideIntensity(const float Value);

	UFUNCTION(BlueprintPure)
	TArray<FString> GetAllTaxonomyMemes() const;

	UFUNCTION(BlueprintPure)

	TArray<FString> GetAllMacros() const;

	void SetupXJ();

	void ShutdownXJ();

	void AddActiveAudio(const FAudioPlayer& Audio);

	void UpdateActiveAudio(const FAudioPlayer& Audio);

	void RemoveActiveAudio(const FAudioPlayer& Audio);
	
	const TMap<FString, FAudioPlayer>& GetActiveAudios() const
	{
		return ActiveAudios;
	}

	FString GetRuntimeProjectDirectory() const
	{
		return RuntimeProjectDir;
	}

	UPROPERTY()
	UXjProject* XjProjectInstance = nullptr;

	UPROPERTY()
	UXjManager* Manager = nullptr;

	UPROPERTY()
	UXjMixer* Mixer = nullptr;

	UPROPERTY()
	UXjAudioLoader* AudioLoader = nullptr;

private:

	FString RuntimeProjectDir;
	
	void OnEnabledShowDebugChain(IConsoleVariable* Var);

	void UpdateDebugChainView();

	void RestoreRuntimeProjectDirectory(UXJMusicDefaultSettings* XjSettings);

	void DeleteRuntimeProjectDirectory();

	TSharedPtr<SDebugChainView> DebugChainViewWidget;

	TMap<FString, FAudioPlayer> ActiveAudios;
};