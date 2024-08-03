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

static const FName XJMusicPluginTabName("XJMusicPlugin");

#define LOCTEXT_NAMESPACE "FXJMusicPluginModule"

void FXJMusicPluginModule::StartupModule()
{
	FXJMusicPluginStyle::Initialize();
	FXJMusicPluginStyle::ReloadTextures();

	FXJMusicPluginCommands::Register();
	
	PluginCommands = MakeShareable(new FUICommandList);

	PluginCommands->MapAction(
		FXJMusicPluginCommands::Get().PluginAction,
		FExecuteAction::CreateRaw(this, &FXJMusicPluginModule::PluginButtonClicked),
		FCanExecuteAction());

	UToolMenus::RegisterStartupCallback(FSimpleMulticastDelegate::FDelegate::CreateRaw(this, &FXJMusicPluginModule::RegisterMenus));

	
	if (ISettingsModule* SettingModule = FModuleManager::GetModulePtr<ISettingsModule>("Settings"))
	{
		SettingModule->RegisterSettings("Project", "Plugins", "XJSettings",
			LOCTEXT("RuntimeSettingsName", "XJ Music Settings"),
			LOCTEXT("RuntimeSettingsDescription", "Configure XJ music plugin settings"),
			GetMutableDefault<UXJMusicDefaultSettings>());
	}

    WorldPostInitializeHandle =	FWorldDelegates::OnPostWorldInitialization.AddRaw(this, &FXJMusicPluginModule::OnPostWorldInitialization);
}


void FXJMusicPluginModule::OnPostWorldInitialization(UWorld* World, const UWorld::InitializationValues Ivs)
{
	if (!World)
	{
		return;
	}

	//TODO Add OnWorldBeginPlay delegate remove after level change

	LastWorld = World;
    WorldBeginPlayHandle = World->OnWorldBeginPlay.AddRaw(this, &FXJMusicPluginModule::OnLevelBeginPlay);
}

void FXJMusicPluginModule::OnLevelBeginPlay()
{
	if (!LastWorld)
	{
		return;
	}

	UXjMusicInstanceSubsystem* XjSubsystem = LastWorld->GetGameInstance()->GetSubsystem<UXjMusicInstanceSubsystem>();
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

void FXJMusicPluginModule::RegisterMenus()
{
	FToolMenuOwnerScoped OwnerScoped(this);

	{
		UToolMenu* Menu = UToolMenus::Get()->ExtendMenu("LevelEditor.MainMenu.Window");
		{
			FToolMenuSection& Section = Menu->FindOrAddSection("WindowLayout");
			Section.AddMenuEntryWithCommandList(FXJMusicPluginCommands::Get().PluginAction, PluginCommands);
		}
	}

	{
		UToolMenu* ToolbarMenu = UToolMenus::Get()->ExtendMenu("LevelEditor.LevelEditorToolBar");
		{
			FToolMenuSection& Section = ToolbarMenu->FindOrAddSection("Settings");
			{
				FToolMenuEntry& Entry = Section.AddEntry(FToolMenuEntry::InitToolBarButton(FXJMusicPluginCommands::Get().PluginAction));
				Entry.SetCommandList(PluginCommands);
			}
		}
	}
}

#undef LOCTEXT_NAMESPACE
	
IMPLEMENT_MODULE(FXJMusicPluginModule, XJMusicPlugin)