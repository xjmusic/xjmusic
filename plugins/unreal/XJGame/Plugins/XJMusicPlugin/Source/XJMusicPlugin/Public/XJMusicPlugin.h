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

	void OnPostWorldInitialization(UWorld* World, const UWorld::InitializationValues IVS);

	void OnLevelBeginPlay();

private:
	TSharedPtr<class FUICommandList> PluginCommands;

	UWorld* LastWorld = nullptr;

	FDelegateHandle WorldBeginPlayHandle;
	FDelegateHandle WorldPostInitializeHandle;
};
