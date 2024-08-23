// Fill out your copyright notice in the Description page of Project Settings.


#include "Assets/XjProjectTypeActions.h"
#include "Types/XjProject.h"

UClass* FXjProjectTypeActions::GetSupportedClass() const
{
	return UXjProject::StaticClass();
}

FText FXjProjectTypeActions::GetName() const
{
	return INVTEXT("XJ Project");
}

FColor FXjProjectTypeActions::GetTypeColor() const
{
	return FColor::Magenta;
}

uint32 FXjProjectTypeActions::GetCategories()
{
	return EAssetTypeCategories::Sounds;
}
