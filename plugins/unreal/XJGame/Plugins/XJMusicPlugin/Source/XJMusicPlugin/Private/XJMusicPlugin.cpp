// Copyright Epic Games, Inc. All Rights Reserved.

#include "XJMusicPlugin.h"
#include "XJMusicPluginStyle.h"
#include "XJMusicPluginCommands.h"
#include "Misc/MessageDialog.h"
#include "ToolMenus.h"
#include "ISettingsModule.h"

#include <Settings/XJMusicDefaultSettings.h>
#include <Interfaces/IPluginManager.h>
#include <XjMusicInstanceSubsystem.h>

#include "AssetToolsModule.h"
#include "Misc/FileHelper.h"

static const FName XJMusicPluginTabName("XJMusicPlugin");

#define LOCTEXT_NAMESPACE "FXJMusicPluginModule"

void FXJMusicPluginModule::StartupModule()
{
	FXJMusicPluginStyle::Initialize();
	FXJMusicPluginStyle::ReloadTextures();

	FXJMusicPluginCommands::Register();
	
	PluginCommands = MakeShareable(new FUICommandList);

	UToolMenus::RegisterStartupCallback(FSimpleMulticastDelegate::FDelegate::CreateRaw(this, &FXJMusicPluginModule::RegisterMenus));

	
	if (ISettingsModule* SettingModule = FModuleManager::GetModulePtr<ISettingsModule>("Settings"))
	{
		SettingModule->RegisterSettings("Project", "Plugins", "XJSettings",
			LOCTEXT("RuntimeSettingsName", "XJ Music Settings"),
			LOCTEXT("RuntimeSettingsDescription", "Configure XJ music plugin settings"),
			GetMutableDefault<UXJMusicDefaultSettings>());
	}

    FWorldDelegates::OnPostWorldInitialization.AddRaw(this, &FXJMusicPluginModule::OnPostWorldInitialization);
	FWorldDelegates::LevelRemovedFromWorld.AddRaw(this, &FXJMusicPluginModule::OnLevelRemovedFromWorld);
}


void FXJMusicPluginModule::OnPostWorldInitialization(UWorld* World, const UWorld::InitializationValues Ivs)
{
	if (!World)
	{
		return;
	}

	LastWorld = World;
    WorldBeginPlayHandle = World->OnWorldBeginPlay.AddRaw(this, &FXJMusicPluginModule::OnLevelBeginPlay);
}

void FXJMusicPluginModule::OnLevelRemovedFromWorld(ULevel* Level, UWorld* World)
{
	if (!World)
	{
		return;
	}

	World->OnWorldBeginPlay.Remove(WorldBeginPlayHandle);

	if (XjSubsystem)
	{
		XjSubsystem->ShutdownXJ();
	}
}

void FXJMusicPluginModule::OnLevelBeginPlay()
{
	if (!LastWorld)
	{
		return;
	}

	XjSubsystem = LastWorld->GetGameInstance()->GetSubsystem<UXjMusicInstanceSubsystem>();
	if (XjSubsystem)
	{
		XjSubsystem->SetupXJ();
	}
}

void FXJMusicPluginModule::ShutdownModule()
{
	UToolMenus::UnRegisterStartupCallback(this);

	UToolMenus::UnregisterOwner(this);

	FXJMusicPluginStyle::Shutdown();

	FXJMusicPluginCommands::Unregister();

	if (ISettingsModule* SettingsModule = FModuleManager::GetModulePtr<ISettingsModule>("Settings"))
	{
		SettingsModule->UnregisterSettings("Project", "Plugins", "XJSettings");
	}

	FWorldDelegates::OnPostWorldInitialization.RemoveAll(this);
	FWorldDelegates::OnPreWorldFinishDestroy.RemoveAll(this);
}

TSharedRef<SWidget> FXJMusicPluginModule::GenerateComboBox()
{
	FMenuBuilder Builder(true, nullptr);

	Builder.AddMenuEntry(FText::FromString("Build"), 
						 FText(), 
						 FSlateIcon(),
						 FExecuteAction::CreateRaw(this, &FXJMusicPluginModule::BuildButtonClicked));

	Builder.AddMenuEntry(FText::FromString("Open Workstation"), 
						 FText(), 
						 FSlateIcon(), 
						 FExecuteAction::CreateRaw(this, &FXJMusicPluginModule::PluginButtonClicked));

	return Builder.MakeWidget();
}

void FXJMusicPluginModule::PluginButtonClicked()
{
	UXJMusicDefaultSettings* XjSettings = GetMutableDefault<UXJMusicDefaultSettings>();
	if (!XjSettings)
	{
		return;
	}

	TSharedPtr<IPlugin> FoundPlugin = IPluginManager::Get().FindPlugin("XJMusicPlugin");
	if (!FoundPlugin.IsValid())
	{
		return;
	}


	FPlatformProcess::CreateProc(*XjSettings->PathToXjMusicWorkstation, *XjSettings->PathToXjProjectFile, true, false, false, nullptr, 0, nullptr, nullptr);
}

void FXJMusicPluginModule::BuildButtonClicked()
{
	UXJMusicDefaultSettings* XjSettings = GetMutableDefault<UXJMusicDefaultSettings>();
	if (!XjSettings)
	{
		return;
	}

	const FString ProjectPath = XjSettings->PathToXjProjectFile;
	const FString FolderContainingProject = FPaths::GetPath(ProjectPath);
	const FString LastFolderName = FPaths::GetBaseFilename(FolderContainingProject);
	const FString PathToBuildFolder = FPaths::Combine(FolderContainingProject, TEXT("build/"));
	
	IPlatformFile& PlatformFile = FPlatformFileManager::Get().GetPlatformFile();

	if (!PlatformFile.DirectoryExists(*PathToBuildFolder))
	{
		return;
	}

	TArray<FString> FoundAudioFilesPaths;

	PlatformFile.FindFilesRecursively(FoundAudioFilesPaths, *PathToBuildFolder, TEXT(".wav"));

	UAutomatedAssetImportData* ImportData = NewObject<UAutomatedAssetImportData>();
	ImportData->bReplaceExisting = true;
	ImportData->DestinationPath = FPaths::Combine(ProjectsLocalPath, LastFolderName);
	ImportData->Filenames = FoundAudioFilesPaths;

	FAssetToolsModule& AssetToolsModule = FModuleManager::GetModuleChecked<FAssetToolsModule>("AssetTools");
	AssetToolsModule.Get().ImportAssetsAutomated(ImportData);
}

void FXJMusicPluginModule::RegisterMenus()
{
	FToolMenuOwnerScoped OwnerScoped(this);

	{
		UToolMenu* Menu = UToolMenus::Get()->ExtendMenu("LevelEditor.MainMenu.Window");
		{
			FToolMenuSection& Section = Menu->FindOrAddSection("WindowLayout");
		}
	}

	{
		UToolMenu* ToolbarMenu = UToolMenus::Get()->ExtendMenu("LevelEditor.LevelEditorToolBar");
		{
			FToolMenuSection& Section = ToolbarMenu->FindOrAddSection("Settings");
			{
				FToolMenuEntry& Entry = 
					Section.AddEntry(FToolMenuEntry::InitComboButton("XJMusicPlugin.PluginAction", 
					{}, FOnGetContent::CreateRaw(this, &FXJMusicPluginModule::GenerateComboBox)));
				Entry.Icon = FSlateIcon(FXJMusicPluginStyle::GetStyleSetName(), "XJMusicPlugin.PluginAction");
				Entry.Label = INVTEXT("XJ Music");
			}
		}
	}
}

#undef LOCTEXT_NAMESPACE
	
IMPLEMENT_MODULE(FXJMusicPluginModule, XJMusicPlugin)