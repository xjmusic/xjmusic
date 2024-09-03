// Copyright Epic Games, Inc. All Rights Reserved.

using UnrealBuildTool;
using System.Collections.Generic;

public class XJGameEditorTarget : TargetRules
{
	public XJGameEditorTarget( TargetInfo Target) : base(Target)
	{
		Type = TargetType.Editor;
		DefaultBuildSettings = BuildSettingsVersion.V2;
        bOverrideBuildEnvironment = true;

        ExtraModuleNames.AddRange( new string[] { "XJGame" } );
	}
}
