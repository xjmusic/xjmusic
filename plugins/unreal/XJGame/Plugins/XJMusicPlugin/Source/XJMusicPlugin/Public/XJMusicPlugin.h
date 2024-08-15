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
	
	void PluginButtonClicked();

private:

	void RegisterMenus();

	void OnPostWorldInitialization(UWorld* World, const UWorld::InitializationValues Ivs);

	void OnLevelRemovedFromWorld(ULevel* Level, UWorld* World);

	void OnLevelBeginPlay();

	TSharedPtr<class FUICommandList> PluginCommands;

	UPROPERTY()
	UXjMusicInstanceSubsystem* XjSubsystem = nullptr;

	UWorld* LastWorld = nullptr;

	FDelegateHandle WorldBeginPlayHandle;
};
