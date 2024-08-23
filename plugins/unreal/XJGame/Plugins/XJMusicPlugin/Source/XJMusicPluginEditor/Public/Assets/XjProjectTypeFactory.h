// Fill out your copyright notice in the Description page of Project Settings.

#pragma once

#include "CoreMinimal.h"
#include "Factories/Factory.h"
#include "XjProjectTypeFactory.generated.h"

UCLASS()
class XJMUSICPLUGINEDITOR_API UXjProjectTypeFactory : public UFactory
{
	GENERATED_BODY()

public:
	UXjProjectTypeFactory();
	
	virtual UObject* FactoryCreateNew(UClass* InClass, UObject* InParent, FName InName, EObjectFlags Flags, UObject* Context, FFeedbackContext* Warn) override;
};
