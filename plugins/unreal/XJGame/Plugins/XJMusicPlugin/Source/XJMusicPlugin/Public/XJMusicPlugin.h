// Copyright Epic Games, Inc. All Rights Reserved.

#pragma once

#include "CoreMinimal.h"
#include "Modules/ModuleManager.h"

class FToolBarBuilder;
class FMenuBuilder;

class FXJMusicPluginModule : public IModuleInterface
{

public:

	virtual void StartupModule() override;
	virtual void ShutdownModule() override;
	
private:

	void OnPostWorldInitialization(UWorld* World, const UWorld::InitializationValues Ivs);

	void OnLevelRemovedFromWorld(ULevel* Level, UWorld* World);

	void OnLevelBeginPlay();

	UPROPERTY()
	class UXjMusicInstanceSubsystem* XjSubsystem = nullptr;

	UWorld* LastWorld = nullptr;

	FDelegateHandle WorldBeginPlayHandle;
};
