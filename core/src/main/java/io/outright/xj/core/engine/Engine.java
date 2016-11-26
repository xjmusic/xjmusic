// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.engine;

import io.outright.xj.core.model.chain.Chain;

public interface Engine {
    public Chain Chain();
    public Class Class();
    public boolean IsAlive();
    public boolean IsRunning();
    public boolean IsStopped();
    public String Verb();
    public int Offset();
    public void RunCycle();
    public void Chain(Engine to);
    public void Start();
//    public State State();
    public String Status();
    public void Stop();
}

//  type State string
//
//  const (
//  Neutral  State = "Neutral"
//  Starting State = "Starting"
//  Running  State = "Running"
//  Failed   State = "Failed"
//  Done     State = "Done"
//  )
