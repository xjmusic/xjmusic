// Fill out your copyright notice in the Description page of Project Settings.


#include "Types/XjProject.h"

TArray<uint8> UXjProject::ReadFile(const FString& FilePath) const
{
	TArray<uint8> Content;
	FFileHelper::LoadFileToArray(Content, *FilePath);
	return Content;
}

void UXjProject::WriteFile(const FString& FilePath, const TArray<uint8>& Content) const
{
	FFileHelper::SaveArrayToFile(Content, *FilePath);
}

void UXjProject::StoreDirectory(const FString& DirectoryPath)
{
	IPlatformFile& PlatformFile = FPlatformFileManager::Get().GetPlatformFile();

	TArray<FString> FilesArray;

	PlatformFile.FindFilesRecursively(FilesArray, *DirectoryPath, nullptr);

	for (const FString& FilePath : FilesArray)
	{
		FFileData FileData;
		FileData.Path = FilePath.RightChop(DirectoryPath.Len());

		if (FPaths::GetExtension(FilePath) != "wav")
		{
			FileData.Content = ReadFile(FilePath);
		}

		Files.Add(FileData);
	}
}

void UXjProject::RestoreDirectory(const FString& DestinationPath) const
{
	IPlatformFile& PlatformFile = FPlatformFileManager::Get().GetPlatformFile();
	for (const FFileData& FileData : Files)
	{
		FString FullPath = FPaths::Combine(DestinationPath, FileData.Path);
		FString Directory = FPaths::GetPath(FullPath);
		PlatformFile.CreateDirectoryTree(*Directory);
		WriteFile(FullPath, FileData.Content);
	}
}

void UXjProject::DeleteDirectory(const FString& DirectoryPath) const
{
	IPlatformFile& PlatformFile = FPlatformFileManager::Get().GetPlatformFile();
	PlatformFile.DeleteDirectoryRecursively(*DirectoryPath);
}
