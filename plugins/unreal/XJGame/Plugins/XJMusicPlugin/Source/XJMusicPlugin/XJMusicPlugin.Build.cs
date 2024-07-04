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

        PublicAdditionalLibraries.Add(Path.Combine(ModuleDirectory, "../../Lib/xjmusic.lib"));
		PublicAdditionalLibraries.Add(Path.Combine(ModuleDirectory, "../../Lib/spdlog.lib"));

        PublicIncludePaths.Add(Path.Combine(ModuleDirectory, "../../Include/"));
    }
}
