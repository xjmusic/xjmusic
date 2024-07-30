// Fill out your copyright notice in the Description page of Project Settings.

#pragma once

class TimeRecord
{
public:
	TimeRecord() = default;

	TimeRecord(const uint64 NewTime)
	{
		SetInMicros(NewTime);
	}

	TimeRecord(const int NewTime)
	{
		SetInMicros(NewTime);
	}

	TimeRecord(const float NewTime)
	{
		SetInSeconds(NewTime);
	}

	void SetInMicros(const uint64 NewTime)
	{
		Micros = NewTime;
		Seconds = NewTime / 1000000.0f;
	}

	void SetInSeconds(const float NewTime)
	{
		Seconds = NewTime;
		Micros = NewTime * 1000000;
	}

	uint64 GetMicros() const
	{
		return Micros;
	}

	float GetMillie() const
	{
		return Seconds * 1000.0f;
	}

	float GetSeconds() const
	{
		return Seconds;
	}

	FString ToString() const
	{
		FString Out = FString::Printf(TEXT("%f s (%lld micro)"), Seconds, Micros);
		return Out;
	}

	bool operator > (const TimeRecord& Other) const
	{
		return this->Micros > Other.Micros;
	}

	bool operator < (const TimeRecord& Other) const
	{
		return this->Micros < Other.Micros;
	}

	bool operator >= (const TimeRecord& Other) const
	{
		return this->Micros >= Other.Micros;
	}

	bool operator <= (const TimeRecord& Other) const
	{
		return this->Micros <= Other.Micros;
	}

	bool operator == (const TimeRecord& Other) const
	{
		return this->Micros == Other.Micros;
	}

	void operator = (const uint64) = delete;
	void operator = (const float) = delete;

private:
	uint64 Micros = 0;
	float Seconds = 0.0f;
};

static uint32 GetTypeHash(const TimeRecord& Record)
{
	return GetTypeHash(Record.GetMicros());
}

struct FAudioPlayer
{
	TimeRecord StartTime;
	TimeRecord EndTime;

	bool bIsPlaying = false;

	FString Name;
	FString Id;

	bool operator == (const FAudioPlayer& Other) const
	{
		return Id == Other.Id && Name == Other.Name;
	}
};

static uint32 GetTypeHash(const FAudioPlayer& Player)
{
	return GetTypeHash(Player.Name);
}