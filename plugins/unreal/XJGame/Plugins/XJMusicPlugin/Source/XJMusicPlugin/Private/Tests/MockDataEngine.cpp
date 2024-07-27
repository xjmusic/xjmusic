// Fill out your copyright notice in the Description page of Project Settings.

#include "Tests/MockDataEngine.h"
#include "Engine/DataTable.h"
#include "Algo/RandomShuffle.h"

TSet<FAudioPlayer> TMockDataEngine::RunCycle(const uint64 ChainMicros)
{
    if (LatencyBetweenCyclesInSeconds > 0.0f)
    {
        float Difference = (ChainMicros - LastMicros) / 1000000.0f;
        LastMicros = ChainMicros;

        if (Difference < LatencyBetweenCyclesInSeconds && LastMicros != 0)
        {
            return {};
        }
    }

    if (MaxAudiosOutputPerCycle > 0)
    {
        Algo::RandomShuffle(ScheduledAudios);
    }

    TSet<FAudioPlayer> OutputPlayers;

    int Itr = 1;

    for (const FMockAudioTableRow& Element : ScheduledAudios)
    {
        if (MaxAudiosOutputPerCycle > 0 && Itr > MaxAudiosOutputPerCycle)
        {
            break;
        }

        if ((uint64)Element.SchedulePastCertainChainMicros > ChainMicros)
        {
            continue;
        }

        FAudioPlayer Player;
        Player.Name = Element.Name;
        Player.Id = Element.Id;
        Player.StartTime.SetInMicros(Element.StartTimeAtChainMicros);
        Player.EndTime.SetInMicros(Element.EndTimeAtChainMicros);

        OutputPlayers.Add(Player);

        ++Itr;
    }

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
