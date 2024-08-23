// Copyright Epic Games, Inc. All Rights Reserved.

#include "XJMusicPlugin.h"
#include <XjMusicInstanceSubsystem.h>

#define LOCTEXT_NAMESPACE "FXJMusicPluginModule"

void FXJMusicPluginModule::StartupModule()
{
    FWorldDelegates::OnPostWorldInitialization.AddRaw(this, &FXJMusicPluginModule::OnPostWorldInitialization);
	FWorldDelegates::LevelRemovedFromWorld.AddRaw(this, &FXJMusicPluginModule::OnLevelRemovedFromWorld);
}

void FXJMusicPluginModule::OnPostWorldInitialization(UWorld* World, const UWorld::InitializationValues Ivs)
{
	if (!World)
	{
		return;
	}

	LastWorld = World;
    WorldBeginPlayHandle = World->OnWorldBeginPlay.AddRaw(this, &FXJMusicPluginModule::OnLevelBeginPlay);
}

void FXJMusicPluginModule::OnLevelRemovedFromWorld(ULevel* Level, UWorld* World)
{
	if (!World)
	{
		return;
	}

	World->OnWorldBeginPlay.Remove(WorldBeginPlayHandle);

	if (XjSubsystem)
	{
		XjSubsystem->ShutdownXJ();
	}
}

void FXJMusicPluginModule::OnLevelBeginPlay()
{
	if (!LastWorld)
	{
		return;
	}

	XjSubsystem = LastWorld->GetGameInstance()->GetSubsystem<UXjMusicInstanceSubsystem>();
	if (XjSubsystem)
	{
		XjSubsystem->SetupXJ();
	}
}

void FXJMusicPluginModule::ShutdownModule()
{
	FWorldDelegates::OnPostWorldInitialization.RemoveAll(this);
	FWorldDelegates::OnPreWorldFinishDestroy.RemoveAll(this);
}

#undef LOCTEXT_NAMESPACE
	
IMPLEMENT_MODULE(FXJMusicPluginModule, XJMusicPlugin)