// Copyright Epic Games, Inc. All Rights Reserved.

using UnrealBuildTool;
using System.Collections.Generic;

public class XJGameTarget : TargetRules
{
	public XJGameTarget( TargetInfo Target) : base(Target)
	{
		Type = TargetType.Game;
		DefaultBuildSettings = BuildSettingsVersion.V2;
		bOverrideBuildEnvironment = true;

		ExtraModuleNames.AddRange( new string[] { "XJGame" } );
	}
}
