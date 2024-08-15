// Fill out your copyright notice in the Description page of Project Settings.

#pragma once

#include "CoreMinimal.h"
#include "Containers/Queue.h"
#include "UObject/Object.h"

#include "XjObjectPool.generated.h"

USTRUCT()
struct XJMUSICPLUGIN_API FXjObjectPool
{
	GENERATED_BODY();

	UObject* GetObject();

	void AddObject(UObject* Object);

	void FreeObject(UObject* Object);

	void Reset();

	uint32 GetNumberOfRuntimeCreatedObjects() const
	{
		return RuntimeCreatedObjects;
	}

	uint32 GetNumberOfInUseObjects() const
	{
		return InUseObjects.Num();
	}

	bool IsInUse(UObject* Object) const
	{
		if (!IsValid(Object))
		{
			return false;
		}

		return InUseObjects.Contains(Object);
	}

private:
	uint32 MaxAvailableObjects = 16;

	UPROPERTY()
	TArray<UObject*> AvailableObjects;

	UPROPERTY()
	TSet<UObject*> InUseObjects;

	uint32 RuntimeCreatedObjects = 0;
};
