// Copyright Epic Games, Inc. All Rights Reserved.

using System.IO;
using System.Text.RegularExpressions;
using UnrealBuildTool;

public class XJMusicPlugin : ModuleRules
{
	public XJMusicPlugin(ReadOnlyTargetRules Target) : base(Target)
	{
        CppStandard = CppStandardVersion.Cpp17;

        IncludeOrderVersion = EngineIncludeOrderVersion.Oldest;

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
				"CoreUObject",
				"Engine",
				"Slate",
				"SlateCore",
                "AudioMixer",
                "AudioExtensions"
            }
		);

        PublicIncludePaths.Add(Path.Combine(ModuleDirectory, "../../Include/"));

        if (Target.Configuration == UnrealTargetConfiguration.DebugGame)
		{
            PublicAdditionalLibraries.Add(Path.Combine(ModuleDirectory, "../../Lib/Debug/xjmusic.lib"));
		}
		else
		{
            PublicAdditionalLibraries.Add(Path.Combine(ModuleDirectory, "../../Lib/Release/xjmusic.lib"));
        }
    }
}
