// Fill out your copyright notice in the Description page of Project Settings.

#include "Tests/MockDataEngine.h"
#include "Engine/DataTable.h"
#include "Algo/RandomShuffle.h"

TArray<FAudioPlayer> TMockDataEngine::RunCycle(const uint64 ChainMicros)
{
    if (LatencyBetweenCyclesInSeconds > 0.0f)
    {
        float Difference = (ChainMicros - LastMicros) / 1000000.0f;
        
        if (Difference < LatencyBetweenCyclesInSeconds)
        {
            return {};
        }
    }

    LastMicros = ChainMicros;

    TArray<FAudioPlayer> OutputPlayers;

    int Itr;

    if (MaxAudiosOutputPerCycle > 0 && LastItr < ScheduledAudios.Num())
    {
        Itr = LastItr;
    }
    else
    {
        Itr = 0;
    }

    int LocalItr = 0;

    for (; Itr < ScheduledAudios.Num(); ++Itr)
    {
        const FMockAudioTableRow& Element = ScheduledAudios[Itr];

        if (MaxAudiosOutputPerCycle > 0 && LocalItr >= MaxAudiosOutputPerCycle)
        {
            break;
        }

        ++LocalItr;

        if ((uint64)Element.SchedulePastCertainChainMicros > ChainMicros)
        {
            continue;
        }

        FAudioPlayer Player;
        Player.Name = Element.Name;
        Player.Id = Element.Id;
        Player.WaveId = Element.Id;
        Player.StartTime.SetInMicros(Element.StartTimeAtChainMicros);
        Player.EndTime.SetInMicros(Element.EndTimeAtChainMicros);
        Player.Event = EAudioEventType::Create;

        OutputPlayers.Add(Player);
    }

    LastItr = Itr;

    return OutputPlayers;
}

void TMockDataEngine::SetMockData(UDataTable* DataTable)
{
    if (!DataTable)
    {
        return;
    }

    for (const TPair<FName, uint8*>& Row : DataTable->GetRowMap())
    {
        FMockAudioTableRow* Info = (FMockAudioTableRow*)Row.Value;

        if (!Info)
        {
            continue;
        }

        ScheduledAudios.Add(*Info);
    }
}
