// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.manager;

import com.google.inject.AbstractModule;
import io.xj.hub.kubernetes.KubernetesModule;

public class ManagerModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(AccountManager.class).to(AccountManagerImpl.class);
    bind(AccountUserManager.class).to(AccountUserManagerImpl.class);
    bind(InstrumentAudioManager.class).to(InstrumentAudioManagerImpl.class);
    bind(InstrumentManager.class).to(InstrumentManagerImpl.class);
    bind(InstrumentMemeManager.class).to(InstrumentMemeManagerImpl.class);
    bind(LibraryManager.class).to(LibraryManagerImpl.class);
    bind(ProgramManager.class).to(ProgramManagerImpl.class);
    bind(ProgramMemeManager.class).to(ProgramMemeManagerImpl.class);
    bind(ProgramSequenceBindingManager.class).to(ProgramSequenceBindingManagerImpl.class);
    bind(ProgramSequenceBindingMemeManager.class).to(ProgramSequenceBindingMemeManagerImpl.class);
    bind(ProgramSequenceChordManager.class).to(ProgramSequenceChordManagerImpl.class);
    bind(ProgramSequenceChordVoicingManager.class).to(ProgramSequenceChordVoicingManagerImpl.class);
    bind(ProgramSequenceManager.class).to(ProgramSequenceManagerImpl.class);
    bind(ProgramSequencePatternManager.class).to(ProgramSequencePatternManagerImpl.class);
    bind(ProgramSequencePatternEventManager.class).to(ProgramSequencePatternEventManagerImpl.class);
    bind(ProgramVoiceManager.class).to(ProgramVoiceManagerImpl.class);
    bind(ProgramVoiceTrackManager.class).to(ProgramVoiceTrackManagerImpl.class);
    bind(TemplateBindingManager.class).to(TemplateBindingManagerImpl.class);
    bind(TemplateManager.class).to(TemplateManagerImpl.class);
    bind(TemplatePlaybackManager.class).to(TemplatePlaybackManagerImpl.class);
    bind(TemplatePublicationManager.class).to(TemplatePublicationManagerImpl.class);
    bind(UserManager.class).to(UserManagerImpl.class);
    install(new KubernetesModule());
  }

}
