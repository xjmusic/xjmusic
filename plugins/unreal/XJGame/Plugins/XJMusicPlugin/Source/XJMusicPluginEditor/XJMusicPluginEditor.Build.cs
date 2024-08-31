using UnrealBuildTool;

public class XJMusicPluginEditor : ModuleRules
{
    public XJMusicPluginEditor(ReadOnlyTargetRules Target) : base(Target)
    {
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
                "XJMusicPlugin",
                "CoreUObject",
                "Slate",
                "SlateCore",
                "UnrealEd",
                "ToolMenus",
                "AssetTools",
                "Projects",
                "InputCore",
                "Engine",
                "AudioExtensions"
            }
        );
        
        if (Target.bBuildEditor == true)
        {
            PublicDependencyModuleNames.AddRange(
                new string[]
                {
                    "DesktopPlatform",
                }
            );
        }
    }
}