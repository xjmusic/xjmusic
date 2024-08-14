// Copyright Epic Games, Inc. All Rights Reserved.

using System.IO;
using System.Text.RegularExpressions;
using UnrealBuildTool;

public class XJMusicPlugin : ModuleRules
{
	public XJMusicPlugin(ReadOnlyTargetRules Target) : base(Target)
	{
        CppStandard = CppStandardVersion.Cpp17;

        PCHUsage = ModuleRules.PCHUsageMode.UseExplicitOrSharedPCHs;

        PublicDependencyModuleNames.AddRange(
			new string[]
			{
				"Core", "AudioMixer",
			}
			);
			
		PrivateDependencyModuleNames.AddRange(
			new string[]
			{
				"Projects",
				"InputCore",
				"UnrealEd",
				"ToolMenus",
				"CoreUObject",
				"Engine",
				"Slate",
				"SlateCore",
                "AudioMixer"
            }
		);

		if (Target.Configuration == UnrealTargetConfiguration.Development)
		{
			PublicAdditionalLibraries.Add(Path.Combine(ModuleDirectory, "../../Lib/Release/xjmusic.lib"));
		}
		else
		{
			PublicAdditionalLibraries.Add(Path.Combine(ModuleDirectory, "../../Lib/Debug/xjmusic.lib"));
        }

        PublicIncludePaths.Add(Path.Combine(ModuleDirectory, "../../Include/"));
    }
}
