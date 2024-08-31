#pragma once

#include "CoreMinimal.h"
#include "Modules/ModuleManager.h"
#include "Assets/XjProjectTypeActions.h"

class FXJMusicPluginEditorModule : public IModuleInterface
{

public:
    
    virtual void StartupModule() override;
    
    virtual void ShutdownModule() override;

    void PluginButtonClicked();

    void BuildButtonClicked();

    TSharedRef<SWidget> GenerateComboBox();

private:

    const FString ProjectsLocalPath = "/Game/XJ/";

    FString LastSelectedBuildDirectory;
    
    TSharedPtr<FXjProjectTypeActions> XjProjectTypeActions;

    void OpenFileDialog(const FString& DialogTitle, const FString DefaultPath, TArray<FString>& OutFiles);

    void AddMenuBarButton(FMenuBuilder& MenuBuilder);

    void RegisterMenu();
};