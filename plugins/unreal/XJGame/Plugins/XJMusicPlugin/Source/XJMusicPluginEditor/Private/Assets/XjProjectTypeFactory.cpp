// Fill out your copyright notice in the Description page of Project Settings.


#include "Assets/XjProjectTypeFactory.h"
#include "Types/XjProject.h"

UXjProjectTypeFactory::UXjProjectTypeFactory()
{
	SupportedClass = UXjProject::StaticClass();
	bCreateNew = true;
}

UObject* UXjProjectTypeFactory::FactoryCreateNew(UClass* InClass, UObject* InParent, FName InName, EObjectFlags Flags,
	UObject* Context, FFeedbackContext* Warn)
{
	return NewObject<UXjProject>(InParent, InClass, InName, Flags, Context);
}
