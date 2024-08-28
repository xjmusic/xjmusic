﻿#include "XJMusicPluginEditor.h"
#include "AssetToolsModule.h"
#include "ISettingsModule.h"
#include "Settings/XJMusicDefaultSettings.h"
#include "ToolMenus.h"
#include "XJMusicPluginStyle.h"
#include "Assets/XjProjectTypeFactory.h"
#include "Types/XjProject.h"
#include "IDesktopPlatform.h"
#include "DesktopPlatformModule.h"

#define LOCTEXT_NAMESPACE "FXJMusicPluginEditorModule"

void FXJMusicPluginEditorModule::StartupModule()
{
	FXJMusicPluginStyle::Initialize();
	FXJMusicPluginStyle::ReloadTextures();
	
	PluginCommands = MakeShareable(new FUICommandList);

	UToolMenus::RegisterStartupCallback(FSimpleMulticastDelegate::FDelegate::CreateRaw(this, &FXJMusicPluginEditorModule::RegisterMenus));
	
	if (ISettingsModule* SettingModule = FModuleManager::GetModulePtr<ISettingsModule>("Settings"))
	{
		SettingModule->RegisterSettings("Project", "Plugins", "XJSettings",
			LOCTEXT("RuntimeSettingsName", "XJ Music Settings"),
			LOCTEXT("RuntimeSettingsDescription", "Configure XJ music plugin settings"),
			GetMutableDefault<UXJMusicDefaultSettings>());
	}

	XjProjectTypeActions = MakeShared<FXjProjectTypeActions>();
	FAssetToolsModule::GetModule().Get().RegisterAssetTypeActions(XjProjectTypeActions.ToSharedRef());

	LastSelectedBuildDirectory = FPaths::ProjectDir();
}

void FXJMusicPluginEditorModule::ShutdownModule()
{
	UToolMenus::UnRegisterStartupCallback(this);

	UToolMenus::UnregisterOwner(this);

	FXJMusicPluginStyle::Shutdown();

	if (ISettingsModule* SettingsModule = FModuleManager::GetModulePtr<ISettingsModule>("Settings"))
	{
		SettingsModule->UnregisterSettings("Project", "Plugins", "XJSettings");
	}

	if(FModuleManager::Get().IsModuleLoaded("AssetTools"))
	{
		FAssetToolsModule::GetModule().Get().UnregisterAssetTypeActions(XjProjectTypeActions.ToSharedRef());
	}
}

void FXJMusicPluginEditorModule::PluginButtonClicked()
{
	UXJMusicDefaultSettings* XjSettings = GetMutableDefault<UXJMusicDefaultSettings>();
	if (!XjSettings)
	{
		return;
	}

	FPlatformProcess::CreateProc(*XjSettings->PathToXjMusicWorkstation, TEXT(""), true, false, false, nullptr, 0, nullptr, nullptr);
}

void FXJMusicPluginEditorModule::BuildButtonClicked()
{
	UXJMusicDefaultSettings* XjSettings = GetMutableDefault<UXJMusicDefaultSettings>();
	if (!XjSettings)
	{
		return;
	}

	TArray<FString> OutFiles;
	OpenFileDialog("Select XJ project directory to build", LastSelectedBuildDirectory, OutFiles);

	if(OutFiles.Num() == 0)
	{
		return;
	}
	
	const FString ProjectPath = OutFiles[0];
	const FString DirectoryContainingProject = FPaths::GetPath(ProjectPath);
	const FString LastFolderName = FPaths::GetBaseFilename(DirectoryContainingProject);
	const FString PathToBuildFolder = FPaths::Combine(DirectoryContainingProject, TEXT("build/"));
	const FString DestinationPath = FPaths::Combine(ProjectsLocalPath, LastFolderName);

	LastSelectedBuildDirectory = DirectoryContainingProject;
	
	IPlatformFile& PlatformFile = FPlatformFileManager::Get().GetPlatformFile();

	if (!PlatformFile.DirectoryExists(*PathToBuildFolder))
	{
		return;
	}

	TArray<FString> FoundAudioFilesPaths;

	PlatformFile.FindFilesRecursively(FoundAudioFilesPaths, *PathToBuildFolder, TEXT(".wav"));

	UAutomatedAssetImportData* ImportData = NewObject<UAutomatedAssetImportData>();
	check(ImportData);
	
	ImportData->bReplaceExisting = true;
	ImportData->DestinationPath = DestinationPath;
	ImportData->Filenames = FoundAudioFilesPaths;


	FAssetToolsModule& AssetToolsModule = FModuleManager::GetModuleChecked<FAssetToolsModule>("AssetTools");

	AssetToolsModule.Get().ImportAssetsAutomated(ImportData);

	UXjProjectTypeFactory* XjProjectTypeFactory = NewObject<UXjProjectTypeFactory>();
	check(XjProjectTypeFactory);

	UXjProject* XjProject = Cast<UXjProject>(AssetToolsModule.Get().CreateAsset(LastFolderName, DestinationPath, UXjProject::StaticClass(), XjProjectTypeFactory));
	check(XjProject);

	XjProject->StoreDirectory(DirectoryContainingProject);
	
	XjProject->ProjectName = LastFolderName;
	XjProject->ProjectPath = DestinationPath;
}

TSharedRef<SWidget> FXJMusicPluginEditorModule::GenerateComboBox()
{
	FMenuBuilder Builder(true, nullptr);

	Builder.AddMenuEntry(FText::FromString("Build"), 
						 FText(), 
						 FSlateIcon(),
						 FExecuteAction::CreateRaw(this, &FXJMusicPluginEditorModule::BuildButtonClicked));

	Builder.AddMenuEntry(FText::FromString("Open Workstation"), 
						 FText(), 
						 FSlateIcon(), 
						 FExecuteAction::CreateRaw(this, &FXJMusicPluginEditorModule::PluginButtonClicked));

	return Builder.MakeWidget();
}

void FXJMusicPluginEditorModule::RegisterMenus()
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
					{}, FOnGetContent::CreateRaw(this, &FXJMusicPluginEditorModule::GenerateComboBox)));
				Entry.Icon = FSlateIcon(FXJMusicPluginStyle::GetStyleSetName(), "XJMusicPlugin.PluginAction");
				Entry.Label = INVTEXT("XJ Music");
			}
		}
	}
}

void FXJMusicPluginEditorModule::OpenFileDialog(const FString& DialogTitle, const FString DefaultPath, TArray<FString>& OutFiles)
{
	void* ParentWindowPtr = FSlateApplication::Get().GetActiveTopLevelWindow()->GetNativeWindow()->GetOSWindowHandle();
	IDesktopPlatform* DesktopPlatform = FDesktopPlatformModule::Get();
	
	if (DesktopPlatform)
	{
		const FString Title = TEXT("Select XJ project to build");
		const FString FileTypes = TEXT("XJ Files (*.xj)|*.xj");

		DesktopPlatform->OpenFileDialog(
			ParentWindowPtr,
			Title,
			DefaultPath,
			TEXT(""),
			FileTypes,
			EFileDialogFlags::None,
			OutFiles
		);
	}
}

#undef LOCTEXT_NAMESPACE
    
IMPLEMENT_MODULE(FXJMusicPluginEditorModule, XJMusicPluginEditor)