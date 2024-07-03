// Copyright Epic Games, Inc. All Rights Reserved.

#include "XJMusicPluginCommands.h"

#define LOCTEXT_NAMESPACE "FXJMusicPluginModule"

void FXJMusicPluginCommands::RegisterCommands()
{
	UI_COMMAND(PluginAction, "XJMusic", "Run XJMusic app", EUserInterfaceActionType::Button, FInputGesture());
}

#undef LOCTEXT_NAMESPACE
