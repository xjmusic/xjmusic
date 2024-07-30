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
				"Core",
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
			}
		);

		if (Target.Configuration == UnrealTargetConfiguration.Development)
		{
			PublicAdditionalLibraries.Add(Path.Combine(ModuleDirectory, "../../Lib/xjmusic.lib"));
			PublicAdditionalLibraries.Add(Path.Combine(ModuleDirectory, "../../Lib/spdlog.lib"));
		}
		else
		{
			PublicAdditionalLibraries.Add(Path.Combine(ModuleDirectory, "../../Lib/debug/xjmusic.lib"));
			PublicAdditionalLibraries.Add(Path.Combine(ModuleDirectory, "../../Lib/debug/spdlogd.lib"));
        }

        PublicIncludePaths.Add(Path.Combine(ModuleDirectory, "../../Include/"));
    }
}
