// Copyright Epic Games, Inc. All Rights Reserved.

#pragma once

#include "CoreMinimal.h"
#include "Framework/Commands/Commands.h"
#include "XJMusicPluginStyle.h"

class FXJMusicPluginCommands : public TCommands<FXJMusicPluginCommands>
{
public:

	FXJMusicPluginCommands()
		: TCommands<FXJMusicPluginCommands>(TEXT("XJMusicPlugin"), NSLOCTEXT("Contexts", "XJMusicPlugin", "XJMusicPlugin Plugin"), NAME_None, FXJMusicPluginStyle::GetStyleSetName())
	{
	}

	virtual void RegisterCommands() override;

public:
	TSharedPtr< FUICommandInfo > PluginAction;
};
