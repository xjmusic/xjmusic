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

    void RegisterMenus();

    TSharedPtr<class FUICommandList> PluginCommands;

private:
    const FString ProjectsLocalPath = "/Game/XJ/";

    TSharedPtr<FXjProjectTypeActions> XjProjectTypeActions;
};