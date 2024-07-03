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

        Definitions.Add("GTEST_LINKED_AS_SHARED_LIBRARY");

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
        PublicAdditionalLibraries.Add(Path.Combine(ModuleDirectory, "../../Lib/gtest_main.lib"));
        PublicAdditionalLibraries.Add(Path.Combine(ModuleDirectory, "../../Lib/gmock.lib"));
        PublicAdditionalLibraries.Add(Path.Combine(ModuleDirectory, "../../Lib/gmock_main.lib"));
        PublicAdditionalLibraries.Add(Path.Combine(ModuleDirectory, "../../Lib/gtest.lib"));
        PublicAdditionalLibraries.Add(Path.Combine(ModuleDirectory, "../../Lib/yaml-cpp.lib"));

        PublicIncludePaths.Add(Path.Combine(ModuleDirectory, "../../Include/"));
    }
}
