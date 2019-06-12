//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.core.model.segment.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.xj.core.fabricator.FabricatorType;
import io.xj.core.model.segment.sub.Arrangement;
import io.xj.core.model.segment.sub.Choice;
import io.xj.core.model.entity.SuperEntityContent;
import io.xj.core.model.segment.sub.Pick;
import io.xj.core.model.segment.Segment;
import io.xj.core.model.segment.sub.SegmentChord;
import io.xj.core.model.segment.sub.SegmentMeme;
import io.xj.core.model.segment.sub.SegmentMessage;

import java.util.Collection;
import java.util.Map;

/**
 [#166129999] SegmentContent is a discrete class used for data persistence.
 [#166132897] SegmentContent POJO via gson only (no JSONObject)
 <p>
 Maps to JSON:
 {
 "type": "Continue",
 "memes": [],
 "choices": [],
 "arrangements": [],
 "chords": [],
 "picks": [],
 "report": {}
 }
 */
public class SegmentContent implements SuperEntityContent {
  private final Map<String, Object> report = Maps.newHashMap();
  private final Collection<SegmentMeme> memes = Lists.newArrayList();
  private final Collection<SegmentMessage> messages = Lists.newArrayList();
  private final Collection<Choice> choices = Lists.newArrayList();
  private final Collection<Arrangement> arrangements = Lists.newArrayList();
  private final Collection<SegmentChord> chords = Lists.newArrayList();
  private final Collection<Pick> picks = Lists.newArrayList();
  private FabricatorType type;

  /**
   Create a instance of SegmentContent, used for transporting the content of a segment

   @param segment to get content of
   @return segment content
   */
  public static SegmentContent of(Segment segment) {
    SegmentContent content = new SegmentContent();
    content.setReport(segment.getReport());
    content.setMemes(segment.getMemes());
    content.setMessages(segment.getMessages());
    content.setChoices(segment.getChoices());
    content.setArrangements(segment.getArrangements());
    content.setChords(segment.getChords());
    content.setPicks(segment.getPicks());
    content.setTypeEnum(segment.getType());
    return content;
  }

  /**
   Set type by enum

   @param type to set
   */
  private void setTypeEnum(FabricatorType type) {
    this.type = type;
  }

  /**
   Get Arrangements

   @return Arrangements
   */
  public Collection<Arrangement> getArrangements() {
    return arrangements;
  }

  /**
   Get Choices

   @return Choices
   */
  public Collection<Choice> getChoices() {
    return choices;
  }

  /**
   Get Chords

   @return Chords
   */
  public Collection<SegmentChord> getChords() {
    return chords;
  }

  /**
   Get Memes

   @return Memes
   */
  public Collection<SegmentMeme> getMemes() {
    return memes;
  }

  /**
   Get Messages

   @return Messages
   */
  public Collection<SegmentMessage> getMessages() {
    return messages;
  }

  /**
   Get Picks

   @return Picks
   */
  public Collection<Pick> getPicks() {
    return picks;
  }

  /**
   Get report

   @return ing
   */
  public Map<String, Object> getReport() {
    return report;
  }

  /**
   Get Type

   @return Type
   */
  public FabricatorType getType() {
    return type;
  }

  /**
   Set Arrangement

   @param arrangements to set
   */
  public void setArrangements(Collection<Arrangement> arrangements) {
    this.arrangements.clear();
    this.arrangements.addAll(arrangements);
  }

  /**
   Set Choices; copy in contents, to preserve mutability of data persistent internally for this class.

   @param choices to set
   */
  public void setChoices(Collection<Choice> choices) {
    this.choices.clear();
    this.choices.addAll(choices);
  }

  /**
   Set Chords

   @param chords to set
   */
  public void setChords(Collection<SegmentChord> chords) {
    this.chords.clear();
    this.chords.addAll(chords);
  }

  /**
   Set Memes

   @param memes to set
   */
  public void setMemes(Collection<SegmentMeme> memes) {
    this.memes.clear();
    this.memes.addAll(memes);
  }

  /**
   Set Messages

   @param messages to set
   */
  public void setMessages(Collection<SegmentMessage> messages) {
    this.messages.clear();
    this.messages.addAll(messages);
  }

  /**
   Set Picks

   @param picks to set
   */
  public void setPicks(Collection<Pick> picks) {
    this.picks.clear();
    this.picks.addAll(picks);
  }

  /**
   Set Report

   @param report to set
   */
  public void setReport(Map<String, Object> report) {
    this.report.clear();
    this.report.putAll(report);
  }

  /**
   Set Type

   @param type to set
   */
  public void setType(String type) {
    this.type = FabricatorType.valueOf(type);
  }

}
