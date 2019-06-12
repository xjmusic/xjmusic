//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.core.model.instrument;

import io.xj.core.model.entity.SubEntity;
import io.xj.core.model.entity.SuperEntity;
import io.xj.core.model.instrument.impl.InstrumentContent;
import io.xj.core.model.instrument.sub.Audio;
import io.xj.core.model.instrument.sub.AudioChord;
import io.xj.core.model.instrument.sub.AudioEvent;
import io.xj.core.model.instrument.sub.InstrumentMeme;

import java.math.BigInteger;
import java.util.Collection;

public interface Instrument extends SuperEntity {

  /**
   Add an InstrumentMeme (assigning its ID)
   + If there are any exceptions, store them in the SuperEntity errors

   @param meme to add
   @return InstrumentMeme with newly assigned ID
   */
  InstrumentMeme add(InstrumentMeme meme);

  /**
   Add an Audio (assigning its ID)
   + If there are any exceptions, store them in the SuperEntity errors

   @param audio to add
   @return Audio with newly assigned ID
   */
  Audio add(Audio audio);

  /**
   Add an AudioChord (assigning its ID)
   + If there are any exceptions, store them in the SuperEntity errors

   @param audioChord to add
   @return AudioChord with newly assigned ID
   */
  AudioChord add(AudioChord audioChord);

  /**
   Add an AudioEvent (assigning its ID)
   + If there are any exceptions, store them in the SuperEntity errors

   @param audioEvent to add
   @return AudioEvent with newly assigned ID
   */
  AudioEvent add(AudioEvent audioEvent);

  @Override
  Collection<SubEntity> getAllSubEntities();

  /**
   get Audio Chords

   @return Audio Chords
   */
  Collection<AudioChord> getAudioChords();

  /**
   get Audio Events

   @return Audio Events
   */
  Collection<AudioEvent> getAudioEvents();

  /**
   get Audios

   @return Audios
   */
  Collection<Audio> getAudios();

  /**
   Get Content

   @return Content
   */
  InstrumentContent getContent();

  /**
   get (computed) Density
   Instrument doesn't actually have density property; it's computed by averaging the density of all its sub entities

   @return Density
   */
  Double getDensity();

  /**
   get Description

   @return Description
   */
  String getDescription();

  /**
   get Library Id

   @return Library Id
   */
  BigInteger getLibraryId();

  /**
   get Memes

   @return Memes
   */
  Collection<InstrumentMeme> getMemes();

  /**
   get State

   @return State
   */
  InstrumentState getState();

  /**
   get Type

   @return Type
   */
  InstrumentType getType();

  /**
   Get User
   
   @return User
   */
  BigInteger getUserId();

  /**
   Set all audio chords
   + If there are any exceptions, store them in the SuperEntity errors

   @param audioChords to set
   @return this Instrument (for chaining methods)
   */
  Instrument setAudioChords(Collection<AudioChord> audioChords);

  /**
   Set all audio events
   + If there are any exceptions, store them in the SuperEntity errors

   @param audioEvents to set
   @return this Instrument (for chaining methods)
   */
  Instrument setAudioEvents(Collection<AudioEvent> audioEvents);

  /**
   Set all audio
   + If there are any exceptions, store them in the SuperEntity errors

   @param audios to set
   @return this Instrument (for chaining methods)
   */
  Instrument setAudios(Collection<Audio> audios);

  /**
   Set content
   + If there are any exceptions, store them in the SuperEntity errors

   @param json content to set
   @return this Instrument (for chaining setters)
   */
  Instrument setContent(String json);

  /**
   Set all content of instrument, cloned from another source instrument, with all new UUID, preserving relationships.
   + If there are any exceptions, store them in the SuperEntity errors

   @param from instrument
   @return this Instrument (for chaining setters)
   */
  Instrument setContentCloned(Instrument from);

  /**
   Set Description

   @param description to set
   @return this Instrument (for chaining Setters)
   */
  Instrument setDescription(String description);

  /**
   Set Density (this is a no-op to prevent problems with payload deserialization)

   @param density to (not actually) set
   @return this Instrument (for chaining Setters)
   */
  Instrument setDensity(Double density);

  /**
   Set LibraryId

   @param libraryId to set
   @return this Instrument (for chaining Setters)
   */
  Instrument setLibraryId(BigInteger libraryId);

  /**
   Set all memes
   + If there are any exceptions, store them in the SuperEntity errors

   @param memes to set
   @return this Instrument (for chaining Setters)
   */
  Instrument setMemes(Collection<InstrumentMeme> memes);

  /**
   Set state

   @param state to set
   @return this Instrument (for chaining Setters)
   */
  Instrument setState(String state);

  /**
   Set state by enum

   @param state to set
   @return this Instrument (for chaining setters)
   */
  Instrument setStateEnum(InstrumentState state);

  /**
   Set Type

   @param type to set
   @return this Instrument (for chaining setters)
   */
  Instrument setType(String type);

  /**
   Set type enum

   @param type to set
   @return this Instrument (for chaining setters)
   */
  Instrument setTypeEnum(InstrumentType type);

  /**
   Set UserId

   @param userId to set
   @return this Instrument (for chaining Setters)
   */
  Instrument setUserId(BigInteger userId);

}
