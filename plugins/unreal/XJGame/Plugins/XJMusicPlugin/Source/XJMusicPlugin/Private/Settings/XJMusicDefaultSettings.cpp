// Fill out your copyright notice in the Description page of Project Settings.



#include <XJMusicPlugin/Public/Settings/XJMusicDefaultSettings.h>

UXJMusicDefaultSettings::UXJMusicDefaultSettings(const FObjectInitializer& obj)
{
	XjMusicPath = "";
	XjWorkDirectory = "Content/XjMusic/";
}

FString UXJMusicDefaultSettings::GetFullWorkPath() const
{
	FString BaseDirRelative = FPaths::ProjectDir();
	FString BaseDirAbsolute = IFileManager::Get().ConvertToAbsolutePathForExternalAppForRead(*BaseDirRelative);
	BaseDirAbsolute += XjWorkDirectory;

	return BaseDirAbsolute;
}
