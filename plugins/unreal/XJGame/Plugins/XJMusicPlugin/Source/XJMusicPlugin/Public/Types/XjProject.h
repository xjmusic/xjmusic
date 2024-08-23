// Fill out your copyright notice in the Description page of Project Settings.

#pragma once

#include "CoreMinimal.h"
#include "UObject/Object.h"
#include "XjProject.generated.h"

USTRUCT(BlueprintType)
struct FFileData
{
	GENERATED_BODY()

	UPROPERTY(BlueprintReadOnly)
	FString Path;

	UPROPERTY(BlueprintReadOnly)
	TArray<uint8> Content;
};

UCLASS(BlueprintType)
class XJMUSICPLUGIN_API UXjProject : public UObject
{
	GENERATED_BODY()

public:
	
	UPROPERTY(VisibleAnywhere, BlueprintReadOnly)
	FString ProjectName;

	UPROPERTY(VisibleAnywhere, BlueprintReadOnly)
	FString ProjectPath;
	
	UFUNCTION()
	void StoreDirectory(const FString& DirectoryPath);

	UFUNCTION()
	void RestoreDirectory(const FString& DestinationPath) const;

	UFUNCTION()
	void DeleteDirectory(const FString& DestinationPath) const;

private:

	UPROPERTY()
	TArray<FFileData> Files;
	
	TArray<uint8> ReadFile(const FString& FilePath) const;

	void WriteFile(const FString& FilePath, const TArray<uint8>& Content) const;
};
