// Fill out your copyright notice in the Description page of Project Settings.

#pragma once

class TimeRecord
{
public:
	TimeRecord() = default;

	void SetInMicros(const uint64 NewTime)
	{
		Micros = NewTime;
		Seconds = NewTime / 1000000.0f;
	}
	
	void SetInMillie(const float NewTime)
	{
		Seconds = NewTime / 1000.0f;
		Micros = Seconds * 1000000;
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

	uint32 GetSamples(const uint32 SampleRate, const uint32 Channels) const
	{
		return Seconds * SampleRate * Channels;
	}

	FString ToString() const
	{
		FString Out = FString::Printf(TEXT("%f s (%lld micro)"), Seconds, Micros);
		return Out;
	}

	TimeRecord operator + (const TimeRecord& Other)
	{
		TimeRecord Out;
		Out.Seconds = Seconds + Other.Seconds;
		Out.Micros = Micros + Other.Micros;

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

enum class EAudioEventType
{
	Create,
	Update,
	Delete
};

struct FAudioPlayer
{
	TimeRecord StartTime;
	TimeRecord EndTime;

	TimeRecord ReleaseTime;

	FString Name;
	FString Id;
	FString WaveId;

	EAudioEventType Event;

	float FromVolume;
	float ToVolume;

	bool operator == (const FAudioPlayer& Other) const
	{
		return Id == Other.Id && Name == Other.Name;
	}
};

static uint32 GetTypeHash(const FAudioPlayer& Player)
{
	return GetTypeHash(Player.Name);
}

static FString FloatToString(const float Value)
{
	return FString::Printf(TEXT("%.2f"), Value);
}