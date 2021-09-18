// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.dao;

import com.google.inject.AbstractModule;

public class DAOModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(AccountDAO.class).to(AccountDAOImpl.class);
    bind(AccountUserDAO.class).to(AccountUserDAOImpl.class);
    bind(InstrumentAudioDAO.class).to(InstrumentAudioDAOImpl.class);
    bind(InstrumentAuthorshipDAO.class).to(InstrumentAuthorshipDAOImpl.class);
    bind(InstrumentDAO.class).to(InstrumentDAOImpl.class);
    bind(InstrumentMemeDAO.class).to(InstrumentMemeDAOImpl.class);
    bind(InstrumentMessageDAO.class).to(InstrumentMessageDAOImpl.class);
    bind(LibraryDAO.class).to(LibraryDAOImpl.class);
    bind(ProgramAuthorshipDAO.class).to(ProgramAuthorshipDAOImpl.class);
    bind(ProgramDAO.class).to(ProgramDAOImpl.class);
    bind(ProgramMemeDAO.class).to(ProgramMemeDAOImpl.class);
    bind(ProgramMessageDAO.class).to(ProgramMessageDAOImpl.class);
    bind(ProgramSequenceBindingDAO.class).to(ProgramSequenceBindingDAOImpl.class);
    bind(ProgramSequenceBindingMemeDAO.class).to(ProgramSequenceBindingMemeDAOImpl.class);
    bind(ProgramSequenceChordDAO.class).to(ProgramSequenceChordDAOImpl.class);
    bind(ProgramSequenceChordVoicingDAO.class).to(ProgramSequenceChordVoicingDAOImpl.class);
    bind(ProgramSequenceDAO.class).to(ProgramSequenceDAOImpl.class);
    bind(ProgramSequencePatternDAO.class).to(ProgramSequencePatternDAOImpl.class);
    bind(ProgramSequencePatternEventDAO.class).to(ProgramSequencePatternEventDAOImpl.class);
    bind(ProgramVoiceDAO.class).to(ProgramVoiceDAOImpl.class);
    bind(ProgramVoiceTrackDAO.class).to(ProgramVoiceTrackDAOImpl.class);
    bind(TemplateBindingDAO.class).to(TemplateBindingDAOImpl.class);
    bind(TemplateDAO.class).to(TemplateDAOImpl.class);
    bind(TemplatePlaybackDAO.class).to(TemplatePlaybackDAOImpl.class);
    bind(UserDAO.class).to(UserDAOImpl.class);
  }

}
