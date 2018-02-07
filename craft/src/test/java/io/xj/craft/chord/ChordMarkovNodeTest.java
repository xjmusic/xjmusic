package io.xj.craft.chord;// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.

import io.xj.craft.chord.ChordMarkovNode;
import io.xj.craft.chord.ChordNode;

import com.google.common.collect.ImmutableList;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ChordMarkovNodeTest {

  @Test
  public void getPrecedentState() {
    ChordMarkovNode subject = new ChordMarkovNode(ImmutableList.of(
      new ChordNode("5|Major"),
      new ChordNode("7|Major"),
      new ChordNode("9|Minor Seven"),
      new ChordNode("5|Minor Diminished")
    ));

    assertEquals(4, subject.getPrecedentState().size());
  }

  @Test
  public void getObservations() {
    ChordMarkovNode subject = new ChordMarkovNode(ImmutableList.of(
      new ChordNode("5|Major"),
      new ChordNode("7|Major"),
      new ChordNode("9|Minor Seven"),
      new ChordNode("5|Minor Diminished")
    ));
    subject.addObservation(new ChordNode("7|Major"));
    subject.addObservation(new ChordNode("9|Major"));

    assertEquals(2, subject.getNodes().size());
  }

  @Test
  public void getRandomObservation() {
    ChordMarkovNode subject = new ChordMarkovNode(ImmutableList.of(
      new ChordNode("5|Major"),
      new ChordNode("7|Major"),
      new ChordNode("9|Minor Seven"),
      new ChordNode("5|Minor Diminished")
    ));
    subject.addObservation(new ChordNode("7|Major"));
    subject.addObservation(new ChordNode("9|Major"));

    ChordNode result = subject.getRandomObservation();

    Integer resultDelta = result.getDelta();
    assertNotNull(resultDelta);
    assertTrue(7 == resultDelta || 9 == resultDelta);
    assertEquals("Major", result.getForm());
  }

  @Test
  public void addObservation() {
    ChordMarkovNode subject = new ChordMarkovNode(ImmutableList.of(
      new ChordNode("5|Major"),
      new ChordNode("7|Major"),
      new ChordNode("9|Minor Seven"),
      new ChordNode("5|Minor Diminished")
    ));

    subject.addObservation(new ChordNode("7|Major"));
    subject.addObservation(new ChordNode("9|Major"));

    assertEquals(2, subject.getNodes().size());
  }


  @Test
  public void precedentStateDescriptor() {
    assertEquals("5|Major:7|Major:9|Minor Seven:5|Minor Diminished", new ChordMarkovNode(ImmutableList.of(
      new ChordNode("5|Major"),
      new ChordNode("7|Major"),
      new ChordNode("9|Minor Seven"),
      new ChordNode("5|Minor Diminished")
    )).precedentStateDescriptor());

    assertEquals("---:7|Major:9|Minor Seven:5|Minor Diminished", new ChordMarkovNode(ImmutableList.of(
      new ChordNode(),
      new ChordNode("7|Major"),
      new ChordNode("9|Minor Seven"),
      new ChordNode("5|Minor Diminished")
    )).precedentStateDescriptor());

    assertEquals("5|Major:7|Major:9|Minor Seven:---", new ChordMarkovNode(ImmutableList.of(
      new ChordNode("5|Major"),
      new ChordNode("7|Major"),
      new ChordNode("9|Minor Seven"),
      new ChordNode()
    )).precedentStateDescriptor());

  }


}
