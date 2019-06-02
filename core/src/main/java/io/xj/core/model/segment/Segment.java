//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.core.model.segment;

import io.xj.core.exception.CoreException;
import io.xj.core.fabricator.FabricatorType;
import io.xj.core.model.arrangement.Arrangement;
import io.xj.core.model.choice.Choice;
import io.xj.core.model.entity.Entity;
import io.xj.core.model.pick.Pick;
import io.xj.core.model.segment.impl.SegmentContent;
import io.xj.core.model.segment_chord.SegmentChord;
import io.xj.core.model.segment_meme.SegmentMeme;
import io.xj.core.model.segment_message.SegmentMessage;
import io.xj.core.model.sequence.SequenceType;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Map;

public interface Segment extends Entity {
  String KEY_ONE = "segment";
  String KEY_MANY = "segments";

  /**
   Add an Arrangement to Segment

   @param arrangement to add
   @return Arrangement with newly added unique-to-segment id
   */
  Arrangement add(Arrangement arrangement) throws CoreException;

  /**
   Add a Choice to Segment

   @param choice to add
   @return Choice with newly added unique-to-segment id
   */
  Choice add(Choice choice) throws CoreException;

  /**
   Add a Pick to Segment

   @param pick to add
   @return Pick with newly added unique-to-segment id
   */
  Pick add(Pick pick) throws CoreException;

  /**
   Add a SegmentChord to Segment

   @param chord to add
   @return SegmentChord with newly added unique-to-segment id
   */
  SegmentChord add(SegmentChord chord) throws CoreException;

  /**
   Add a SegmentMeme to Segment

   @param meme to add
   @return SegmentMeme with newly added unique-to-segment id
   */
  SegmentMeme add(SegmentMeme meme) throws CoreException;

  /**
   Add a SegmentMessage to Segment

   @param message to add
   @return SegmentMessage with newly added unique-to-segment id
   */
  SegmentMessage add(SegmentMessage message) throws CoreException;

  /**
   Get all entities

   @return collection of entities
   */
  Collection<SegmentEntity> getAllEntities();

  /**
   Get Arrangements

   @return Arrangements
   */
  Collection<Arrangement> getArrangements();

  /**
   Get Arrangements for a given Choice

   @param choice to get arrangements for
   @return Arrangements
   */
  Collection<Arrangement> getArrangementsForChoice(Choice choice);

  /**
   Get begin-at time of segment

   @return begin-at time
   */
  Timestamp getBeginAt();

  /**
   Get Chain ID

   @return chain id
   */
  BigInteger getChainId();

  /**
   Get one Choice of Segment (from Content) of a given type

   @param type of choice to get
   @return choice of given type
   @throws CoreException if no such Choice exists for this Segment
   */
  Choice getChoiceOfType(SequenceType type) throws CoreException;

  /**
   Get Choices

   @return Choices
   */
  Collection<Choice> getChoices();

  /**
   Get all Choices of Segment (from Content) of a given type

   @param type of choice to get
   @return choice of given type
   */
  Collection<Choice> getChoicesOfType(SequenceType type);

  /**
   Get Chords

   @return Chords
   */
  Collection<SegmentChord> getChords();

  /**
   [#162361525] read content of prior Segment
   */
  SegmentContent getContent();

  /**
   Get density of segment

   @return density
   */
  Double getDensity();

  /**
   Get end-at time for Segment

   @return end-at time
   */
  Timestamp getEndAt();

  /**
   Get key for the segment

   @return key
   */
  String getKey();

  /**
   Get Memes

   @return Memes
   */
  Collection<SegmentMeme> getMemes();

  /**
   Get Messages

   @return Messages
   */
  Collection<SegmentMessage> getMessages();

  /**
   Get offset

   @return offset
   */
  BigInteger getOffset();

  /**
   Get Picks

   @return Picks
   */
  Collection<Pick> getPicks();

  /**
   get offset of previous segment

   @return previous segment offset
   */
  BigInteger getPreviousOffset() throws CoreException;

  /**
   Gtr ing

   @return ing
   */
  Map<String, Object> getReport();

  /**
   Get state of Segment

   @return state
   */
  SegmentState getState();

  /**
   Get tempo of segment

   @return tempo
   */
  Double getTempo();

  /**
   Get the total length of the segment

   @return total length
   */
  Integer getTotal();

  /**
   Get Type

   @return Type
   */
  FabricatorType getType();

  /**
   Get waveform key of segment

   @return waveform key
   */
  String getWaveformKey();

  /**
   Whether this Segment is at offset 0

   @return true if offset is 0
   */
  boolean isInitial();

  /**
   Put a key/value into the report

   @param key to put into report
   */
  void putReport(String key, String value);

  /**
   [#158610991] Segment is reverted on failure for fault tolerance of Chain
   [#166293178] Segment revert does not delete any SegmentMessage
   */
  void revert();

  /**
   Set Arrangement

   @param arrangements to set
   */
  void setArrangements(Collection<Arrangement> arrangements) throws CoreException;

  /**
   Set begin-at time for Segment

   @param beginAt time to set
   @return this Segment (for chaining setters)
   */
  Segment setBeginAt(String beginAt);

  /**
   Set end-at time for Segment

   @param beginAt time to set
   @return this Segment (for chaining setters)
   */
  Segment setBeginAtTimestamp(Timestamp beginAt);

  /**
   Set Chain ID

   @param chainId to set
   @return this Segment (for chaining setters)
   */
  Segment setChainId(BigInteger chainId);

  /**
   Set Choices; copy in contents, to preserve mutability of data persistent internally for this class.

   @param choices to set
   */
  void setChoices(Collection<Choice> choices) throws CoreException;

  /**
   Set Chords

   @param chords to set
   */
  void setChords(Collection<SegmentChord> chords) throws CoreException;

  /**
   [#162361525] persist Segment content as JSON
   [#166132897] SegmentContent POJO via gson only (no JSONObject)

   @return Segment for chaining setters
   */
  Segment setContent(String json) throws CoreException;

  /**
   Set density of segment

   @param density to set
   @return this Segment (for chaining setters)
   */
  Segment setDensity(Double density);

  /**
   Set end-at time for Segment

   @param endAt time to set
   @return this Segment (for chaining setters)
   */
  Segment setEndAt(String endAt);

  /**
   Set end-at time of Segment

   @param endAt time to set
   @return this Segment (for chaining setters)
   */
  Segment setEndAtTimestamp(Timestamp endAt);

  /**
   Set the key for the segment

   @param key to set
   @return this Segment (for chaining setters)
   */
  Segment setKey(String key);

  /**
   Set Memes

   @param memes to set
   */
  void setMemes(Collection<SegmentMeme> memes) throws CoreException;

  /**
   Set Messages

   @param messages to set
   */
  void setMessages(Collection<SegmentMessage> messages) throws CoreException;

  /**
   Set offset of segment

   @param offset to set
   @return this Segment (for chaining setters)
   */
  Segment setOffset(BigInteger offset);

  /**
   Set Picks

   @param picks to set
   */
  void setPicks(Collection<Pick> picks) throws CoreException;

  /**
   Set Report

   @param input to set
   */
  void setReport(Map<String, Object> input);

  /**
   Set state of Segment

   @param value to set
   @return this Segment (for chaining setters)
   */
  Segment setState(String value) throws CoreException;

  /**
   Set state of Segment

   @param value to set
   @return this Segment (for chaining setters)
   */
  Segment setStateEnum(SegmentState value);

  /**
   Set tempo of segment

   @param tempo to set
   @return this Segment (for chaining setters)
   */
  Segment setTempo(Double tempo);

  /**
   Set total length of segment

   @param total to set
   @return this Segment (for chaining setters)
   */
  Segment setTotal(Integer total);

  /**
   Set Type

   @param type to set
   */
  void setType(String type);

  /**
   Set TypeEnum

   @param type to set
   */
  void setTypeEnum(FabricatorType type);

  /**
   Set waveform key of segment

   @param waveformKey to set
   @return this Segment (for chaining setters)
   */
  Segment setWaveformKey(String waveformKey);

  /**
   Validate that all entities have an id,
   that none of the entities provided share an id, and that relation ids are OK

   @throws CoreException if invalid attributes, or child entities have duplicate ids or bad relations are detected
   */
  void validateContent() throws CoreException;
}
