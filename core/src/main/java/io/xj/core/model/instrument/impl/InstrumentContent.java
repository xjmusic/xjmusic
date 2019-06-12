//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.core.model.instrument.impl;

import com.google.common.collect.Lists;
import io.xj.core.model.entity.SuperEntityContent;
import io.xj.core.model.instrument.Instrument;
import io.xj.core.model.instrument.sub.Audio;
import io.xj.core.model.instrument.sub.AudioChord;
import io.xj.core.model.instrument.sub.AudioEvent;
import io.xj.core.model.instrument.sub.InstrumentMeme;

import java.util.Collection;

/**
 [#166708597] Instrument model handles all of its own entities
 <p>
 POJO for persisting data in memory AS json
 <p>
 NOTE: There can only be ONE of any getter/setter (with the same # of input params)
 */
public class InstrumentContent implements SuperEntityContent {
  private final Collection<InstrumentMeme> memes = Lists.newArrayList();
  private final Collection<Audio> audios = Lists.newArrayList();
  private final Collection<AudioChord> audioChords = Lists.newArrayList();
  private final Collection<AudioEvent> audioEvents = Lists.newArrayList();

  /**
   Create a instance of InstrumentContent, used for transporting the content of a instrument

   @param instrument to get content of
   @return instrument content
   */
  public static InstrumentContent of(Instrument instrument) {
    InstrumentContent content = new InstrumentContent();
    content.setAudios(instrument.getAudios());
    content.setAudioChords(instrument.getAudioChords());
    content.setMemes(instrument.getMemes());
    content.setAudioEvents(instrument.getAudioEvents());
    return content;
  }

  /**
   Get Memes

   @return Memes
   */
  public Collection<InstrumentMeme> getMemes() {
    return memes;
  }

  /**
   Get Audios

   @return audios
   */
  public Collection<Audio> getAudios() {
    return audios;
  }

  /**
   Get Audio Chords

   @return audio chords
   */
  public Collection<AudioChord> getAudioChords() {
    return audioChords;
  }

  /**
   Get Audio Events

   @return audio events
   */
  public Collection<AudioEvent> getAudioEvents() {
    return audioEvents;
  }

  /**
   Set Memes

   @param memes to set
   */
  public void setMemes(Collection<InstrumentMeme> memes) {
    this.memes.clear();
    this.memes.addAll(memes);
  }

  /**
   Set Audio Events

   @param audioEvents to set
   */
  public void setAudioEvents(Collection<AudioEvent> audioEvents) {
    this.audioEvents.clear();
    this.audioEvents.addAll(audioEvents);
  }

  /**
   Set Audio Chords

   @param audioChords to set
   */
  public void setAudioChords(Collection<AudioChord> audioChords) {
    this.audioChords.clear();
    this.audioChords.addAll(audioChords);
  }

  /**
   Set Audios

   @param audios to set
   */
  public void setAudios(Collection<Audio> audios) {
    this.audios.clear();
    this.audios.addAll(audios);
  }

}
