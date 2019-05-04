//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.core.model.segment;

import com.google.common.collect.Maps;
import io.xj.core.model.arrangement.Arrangement;
import io.xj.core.model.choice.Choice;
import io.xj.core.model.message.MessageType;
import io.xj.core.model.pick.Pick;
import io.xj.core.model.segment_chord.SegmentChord;
import io.xj.core.model.segment_meme.SegmentMeme;
import io.xj.core.model.segment_message.SegmentMessage;
import io.xj.core.model.sequence.SequenceType;

import java.math.BigInteger;
import java.util.Map;
import java.util.UUID;

/**
 Store a set of randomly generated UUID by easy-to-reference integer id
 */
public class SegmentTestHelper {
  Map<Integer, UUID> cache = Maps.newConcurrentMap();

  /**
   Get a UUID by its id, or generate one if none has been requested yet, and return the same one for that id in the future.

   @param id to get UUID for
   @return UUID
   */
  public UUID getUuid(Integer id) {
    if (!(cache.containsKey(id))) cache.put(id, UUID.randomUUID());
    return cache.get(id);
  }

  public Choice getChoice(Integer id, SequenceType type, Integer targetId, Integer transpose) {
    Choice choice = new Choice()
      .setUuid(getUuid(id))
      .setTypeEnum(type)
      .setTranspose(transpose);
    switch (type) {
      case Macro:
      case Main:
        choice.setSequencePatternId(BigInteger.valueOf(targetId));
        break;
      case Rhythm:
      case Detail:
        choice.setSequenceId(BigInteger.valueOf(targetId));
        break;
    }
    return choice;
  }

  public SegmentChord getChord(double position, String name) {
    return new SegmentChord()
      .setPosition(position)
      .setName(name);
  }

  public SegmentMeme getMeme(String name) {
    return new SegmentMeme()
      .setName(name);
  }

  public SegmentMessage getMessage(MessageType type, String body) {
    return new SegmentMessage()
      .setTypeEnum(type)
      .setBody(body);
  }

  public Choice getChoice(Integer id) {
    return new Choice()
      .setUuid(getUuid(id))
      .setType("Main")
      .setSequencePatternId(BigInteger.valueOf(20))
      .setTranspose(-2);
  }

  public Arrangement getArrangement(Integer id, Integer choiceId) {
    return new Arrangement()
      .setUuid(getUuid(id))
      .setChoiceUuid(getUuid(choiceId))
      .setVoiceId(BigInteger.valueOf(354L))
      .setInstrumentId(BigInteger.valueOf(432L));
  }

  public Arrangement getArrangement(Integer choiceId) {
    return new Arrangement()
      .setChoiceUuid(getUuid(choiceId))
      .setVoiceId(BigInteger.valueOf(354L))
      .setInstrumentId(BigInteger.valueOf(432L));
  }

  public Pick getPick(Integer arrangementId) {
    return new Pick()
      .setArrangementUuid(getUuid(arrangementId))
      .setPatternEventId(BigInteger.valueOf(723L))
      .setAudioId(BigInteger.valueOf(6329L))
      .setVoiceId(BigInteger.valueOf(782L))
      .setInflection("TANG")
      .setStart(0.92)
      .setLength(2.7)
      .setAmplitude(0.84)
      .setPitch(42.9);
  }


}
