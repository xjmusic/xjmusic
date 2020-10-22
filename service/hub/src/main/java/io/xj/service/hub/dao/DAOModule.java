// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.dao;

import com.google.inject.AbstractModule;

public class DAOModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(AccountDAO.class).to(AccountDAOImpl.class);
    bind(AccountUserDAO.class).to(AccountUserDAOImpl.class);
    bind(InstrumentDAO.class).to(InstrumentDAOImpl.class);
    bind(InstrumentAudioDAO.class).to(InstrumentAudioDAOImpl.class);
    bind(InstrumentAudioChordDAO.class).to(InstrumentAudioChordDAOImpl.class);
    bind(InstrumentAudioEventDAO.class).to(InstrumentAudioEventDAOImpl.class);
    bind(InstrumentMemeDAO.class).to(InstrumentMemeDAOImpl.class);
    bind(LibraryDAO.class).to(LibraryDAOImpl.class);
    bind(ProgramDAO.class).to(ProgramDAOImpl.class);
    bind(ProgramSequencePatternEventDAO.class).to(ProgramSequencePatternEventDAOImpl.class);
    bind(ProgramMemeDAO.class).to(ProgramMemeDAOImpl.class);
    bind(ProgramSequencePatternDAO.class).to(ProgramSequencePatternDAOImpl.class);
    bind(ProgramSequenceDAO.class).to(ProgramSequenceDAOImpl.class);
    bind(ProgramSequenceBindingDAO.class).to(ProgramSequenceBindingDAOImpl.class);
    bind(ProgramSequenceBindingMemeDAO.class).to(ProgramSequenceBindingMemeDAOImpl.class);
    bind(ProgramSequenceChordDAO.class).to(ProgramSequenceChordDAOImpl.class);
    bind(ProgramSequenceChordVoicingDAO.class).to(ProgramSequenceChordVoicingDAOImpl.class);
    bind(ProgramVoiceTrackDAO.class).to(ProgramVoiceTrackDAOImpl.class);
    bind(ProgramVoiceDAO.class).to(ProgramVoiceDAOImpl.class);
    bind(UserDAO.class).to(UserDAOImpl.class);
  }

}
