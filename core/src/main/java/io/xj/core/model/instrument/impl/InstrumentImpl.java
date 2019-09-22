//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.instrument.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import io.xj.core.exception.CoreException;
import io.xj.core.model.entity.SubEntity;
import io.xj.core.model.entity.impl.SuperEntityImpl;
import io.xj.core.model.instrument.Instrument;
import io.xj.core.model.instrument.InstrumentState;
import io.xj.core.model.instrument.InstrumentType;
import io.xj.core.model.instrument.sub.Audio;
import io.xj.core.model.instrument.sub.AudioChord;
import io.xj.core.model.instrument.sub.AudioEvent;
import io.xj.core.model.instrument.sub.InstrumentMeme;
import io.xj.core.model.library.Library;
import io.xj.core.model.payload.Payload;
import io.xj.core.model.user.User;
import io.xj.core.transport.GsonProvider;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 [#166708597] Instrument model handles all of its own entities
 <p>
 POJO for persisting data in memory AS json
 <p>
 NOTE: There can only be ONE of any getter/setter (with the same # of input params)
 */
public class InstrumentImpl extends SuperEntityImpl implements Instrument {
  private final GsonProvider gsonProvider;
  private final Map<UUID, InstrumentMeme> memeMap = Maps.newHashMap();
  private final Map<UUID, Audio> audioMap = Maps.newHashMap();
  private final Map<UUID, AudioEvent> audioEventMap = Maps.newHashMap();
  private final Map<UUID, AudioChord> audioChordMap = Maps.newHashMap();
  private InstrumentState state;
  private String name;
  private InstrumentType type;
  private BigInteger userId;
  private BigInteger libraryId;
  private Exception stateException;
  private Exception typeException;

  /**
   Constructor with Instrument id
   */
  @AssistedInject
  public InstrumentImpl(
    @Assisted("id") BigInteger id,
    GsonProvider gsonProvider
  ) {
    this.id = id;
    this.gsonProvider = gsonProvider;
  }

  /**
   Constructor with no id
   */
  @AssistedInject
  public InstrumentImpl(
    GsonProvider gsonProvider
  ) {
    this.gsonProvider = gsonProvider;
  }


  @Override
  public InstrumentMeme add(InstrumentMeme meme) {
    try {
      requireId("before adding InstrumentMeme");
      meme.setInstrumentId(getId());
      return SubEntity.add(memeMap, meme);
    } catch (CoreException e) {
      add(e);
      return meme;
    }

  }

  @Override
  public Audio add(Audio audio) {
    try {
      requireId("before adding Audio");
      audio.setInstrumentId(getId());
      return SubEntity.add(audioMap, audio);
    } catch (CoreException e) {
      add(e);
      return audio;
    }

  }

  @Override
  public AudioChord add(AudioChord audioChord) {
    try {
      requireId("before adding AudioChord");
      audioChord.setInstrumentId(getId());
      ensureRelations(audioChord);
      return SubEntity.add(audioChordMap, audioChord);
    } catch (CoreException e) {
      add(e);
      return audioChord;
    }

  }

  @Override
  public AudioEvent add(AudioEvent audioEvent) {
    try {
      requireId("before adding AudioEvent");
      audioEvent.setInstrumentId(getId());
      ensureRelations(audioEvent);
      return SubEntity.add(audioEventMap, audioEvent);
    } catch (CoreException e) {
      add(e);
      return audioEvent;
    }

  }

  @Override
  public Instrument consume(Payload payload) throws CoreException {
    super.consume(payload);
    syncSubEntities(payload, memeMap, InstrumentMeme.class);
    syncSubEntities(payload, audioMap, Audio.class);
    syncSubEntities(payload, audioEventMap, AudioEvent.class);
    syncSubEntities(payload, audioChordMap, AudioChord.class);
    return this;
  }

  @Override
  public Collection<SubEntity> getAllSubEntities() {
    Collection<SubEntity> out = Lists.newArrayList();
    out.addAll(getMemes());
    out.addAll(getAudios());
    out.addAll(getAudioChords());
    out.addAll(getAudioEvents());
    return out;
  }

  @Override
  public Double getDensity() {
    double DL = 0;
    double L = 0;
    for (Audio audio : getAudios()) {
      DL += audio.getDensity() * audio.getLength();
      L += audio.getLength();
    }
    return 0 < L ? DL / L : 0;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public BigInteger getLibraryId() {
    return libraryId;
  }

  @Override
  public Collection<InstrumentMeme> getMemes() {
    return memeMap.values();
  }

  @Override
  public Collection<AudioChord> getAudioChords() {
    return audioChordMap.values();
  }

  @Override
  public Collection<AudioEvent> getAudioEvents() {
    return audioEventMap.values();
  }

  @Override
  public Collection<Audio> getAudios() {
    return audioMap.values();
  }

  @Override
  public InstrumentContent getContent() {
    return InstrumentContent.of(this);
  }

  @Override
  public BigInteger getParentId() {
    return libraryId;
  }

  @Override
  public ImmutableList<String> getResourceAttributeNames() {
    return ImmutableList.<String>builder()
      .addAll(super.getResourceAttributeNames())
      .add("state")
      .add("type")
      .add("name")
      .add("density")
      .build();
  }

  @Override
  public ImmutableList<Class> getResourceBelongsTo() {
    return ImmutableList.<Class>builder()
      .addAll(super.getResourceBelongsTo())
      .add(User.class)
      .add(Library.class)
      .build();
  }

  @Override
  public ImmutableList<Class> getResourceHasMany() {
    return ImmutableList.<Class>builder()
      .addAll(super.getResourceHasMany())
      .add(Audio.class)
      .add(InstrumentMeme.class)
      .build();
  }

  @Override
  public InstrumentState getState() {
    return state;
  }

  @Override
  public InstrumentType getType() {
    return type;
  }

  @Override
  public BigInteger getUserId() {
    return userId;
  }

  @Override
  public Instrument setAudioChords(Collection<AudioChord> audioChords) {
    audioChordMap.clear();
    for (AudioChord audioChord : audioChords) {
      add(audioChord);
    }
    return this;
  }

  @Override
  public Instrument setAudioEvents(Collection<AudioEvent> audioEvents) {
    audioEventMap.clear();
    for (AudioEvent audioEvent : audioEvents) {
      add(audioEvent);
    }
    return this;
  }

  @Override
  public Instrument setAudios(Collection<Audio> audios) {
    audioMap.clear();
    for (Audio audio : audios) {
      add(audio);
    }
    return this;
  }

  @Override
  public Instrument setContent(String json) {
    InstrumentContent content = gsonProvider.gson().fromJson(json, InstrumentContent.class);
    setMemes(content.getMemes());
    setAudios(content.getAudios());
    setAudioEvents(content.getAudioEvents());
    setAudioChords(content.getAudioChords());
    return this;
  }

  @Override
  public Instrument setContentCloned(Instrument from) {
    setMemes(from.getMemes());
    setAudios(from.getAudios());
    setAudioEvents(from.getAudioEvents());
    setAudioChords(from.getAudioChords());
    return this;
  }

  @Override
  public Instrument setName(String name) {
    this.name = name;
    return this;
  }

  @Override
  public Instrument setDensity(Double density) {
    // no op
    return this;
  }

  @Override
  public Instrument setLibraryId(BigInteger libraryId) {
    this.libraryId = libraryId;
    return this;
  }

  @Override
  public Instrument setMemes(Collection<InstrumentMeme> memes) {
    memeMap.clear();
    for (InstrumentMeme meme : memes) {
      add(meme);
    }
    return this;
  }

  @Override
  public Instrument setState(String state) {
    try {
      this.state = InstrumentState.validate(state);
    } catch (CoreException e) {
      stateException = e;
    }
    return this;
  }

  @Override
  public Instrument setStateEnum(InstrumentState state) {
    this.state = state;
    return this;
  }

  @Override
  public Instrument setType(String type) {
    try {
      this.type = InstrumentType.validate(type);
    } catch (CoreException e) {
      typeException = e;
    }
    return this;
  }

  @Override
  public Instrument setTypeEnum(InstrumentType type) {
    this.type = type;
    return this;
  }

  @Override
  public Instrument setUserId(BigInteger userId) {
    this.userId = userId;
    return this;
  }

  @Override
  public String toString() {
    return name + " " + "(" + type + ")";
  }

  @Override
  public Instrument validate() throws CoreException {
    require(userId, "User ID");
    require(libraryId, "Library ID");
    require(name, "Name");

    requireNo(typeException, "Type");
    require(type, "Type");

    requireNo(stateException, "State");
    require(state, "State");

    return validateContent();
  }

  @Override
  public Instrument validateContent() throws CoreException {
    SubEntity.validate(this.getAllSubEntities());
    for (AudioChord audioChord : getAudioChords()) ensureRelations(audioChord);
    for (AudioEvent audioEvent : getAudioEvents()) ensureRelations(audioEvent);
    return this;
  }

  /**
   Ensure that an AudioChord relates to an existing Audio stored in the Program

   @param audioChord to ensure existing relations of
   @throws CoreException if no such Audio exists
   */
  private void ensureRelations(AudioChord audioChord) throws CoreException {
    if (Objects.isNull(audioChord.getAudioId())) {
      throw new CoreException(String.format("AudioChord id=%s has null audioId", audioChord.getId()));
    }
    if (!audioMap.containsKey(audioChord.getAudioId())) {
      throw new CoreException(String.format("AudioChord id=%s has nonexistent audioId=%s", audioChord.getId(), audioChord.getAudioId()));
    }
  }

  /**
   Ensure that an AudioEvent relates to an existing Audio stored in the Program

   @param audioEvent to ensure existing relations of
   @throws CoreException if no such Audio exists
   */
  private void ensureRelations(AudioEvent audioEvent) throws CoreException {
    if (Objects.isNull(audioEvent.getAudioId())) {
      throw new CoreException(String.format("AudioEvent id=%s has null audioId", audioEvent.getId()));
    }
    if (!audioMap.containsKey(audioEvent.getAudioId())) {
      throw new CoreException(String.format("AudioEvent id=%s has nonexistent audioId=%s", audioEvent.getId(), audioEvent.getAudioId()));
    }
  }

}
