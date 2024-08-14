// Fill out your copyright notice in the Description page of Project Settings.


#include "Types/XjObjectPool.h"

UObject* FXjObjectPool::GetObject()
{
	if (AvailableObjects.Num() == 0)
	{
		return nullptr;
	}

	UObject* ReturnedObject = AvailableObjects.Pop();
	InUseObjects.Add(ReturnedObject);

	return ReturnedObject;
}

void FXjObjectPool::AddObject(UObject* Object)
{
	if (!IsValid(Object))
	{
		return;
	}

	InUseObjects.Add(Object);
	RuntimeCreatedObjects++;
}

void FXjObjectPool::FreeObject(UObject* Object)
{
	if ((uint32)AvailableObjects.Num() >= MaxAvailableObjects)
	{
		InUseObjects.Remove(Object);
		return;
	}

	if (!IsValid(Object) || InUseObjects.Contains(Object))
	{
		return;
	}

	AvailableObjects.Add(Object);
	InUseObjects.Remove(Object);
}

void FXjObjectPool::Reset()
{
	AvailableObjects.Empty();
	InUseObjects.Empty();
}
