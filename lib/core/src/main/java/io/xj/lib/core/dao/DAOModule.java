// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.core.dao;

import com.google.inject.AbstractModule;

public class DAOModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(AccountDAO.class).to(AccountDAOImpl.class);
    bind(AccountUserDAO.class).to(AccountUserDAOImpl.class);
    bind(ChainDAO.class).to(ChainDAOImpl.class);
    bind(ChainBindingDAO.class).to(ChainBindingDAOImpl.class);
    bind(ChainConfigDAO.class).to(ChainConfigDAOImpl.class);
    bind(InstrumentDAO.class).to(InstrumentDAOImpl.class);
    bind(InstrumentAudioDAO.class).to(InstrumentAudioDAOImpl.class);
    bind(InstrumentAudioChordDAO.class).to(InstrumentAudioChordDAOImpl.class);
    bind(InstrumentAudioEventDAO.class).to(InstrumentAudioEventDAOImpl.class);
    bind(InstrumentMemeDAO.class).to(InstrumentMemeDAOImpl.class);
    bind(LibraryDAO.class).to(LibraryDAOImpl.class);
    bind(PlatformMessageDAO.class).to(PlatformMessageDAOImpl.class);
    bind(ProgramDAO.class).to(ProgramDAOImpl.class);
    bind(ProgramSequencePatternEventDAO.class).to(ProgramSequencePatternEventDAOImpl.class);
    bind(ProgramMemeDAO.class).to(ProgramMemeDAOImpl.class);
    bind(ProgramSequencePatternDAO.class).to(ProgramSequencePatternDAOImpl.class);
    bind(ProgramSequenceDAO.class).to(ProgramSequenceDAOImpl.class);
    bind(ProgramSequenceBindingDAO.class).to(ProgramSequenceBindingDAOImpl.class);
    bind(ProgramSequenceBindingMemeDAO.class).to(ProgramSequenceBindingMemeDAOImpl.class);
    bind(ProgramSequenceChordDAO.class).to(ProgramSequenceChordDAOImpl.class);
    bind(ProgramVoiceTrackDAO.class).to(ProgramVoiceTrackDAOImpl.class);
    bind(ProgramVoiceDAO.class).to(ProgramVoiceDAOImpl.class);
    bind(SegmentDAO.class).to(SegmentDAOImpl.class);
    bind(SegmentChoiceArrangementDAO.class).to(SegmentChoiceArrangementDAOImpl.class);
    bind(SegmentChoiceDAO.class).to(SegmentChoiceDAOImpl.class);
    bind(SegmentChordDAO.class).to(SegmentChordDAOImpl.class);
    bind(SegmentMemeDAO.class).to(SegmentMemeDAOImpl.class);
    bind(SegmentMessageDAO.class).to(SegmentMessageDAOImpl.class);
    bind(SegmentChoiceArrangementPickDAO.class).to(SegmentChoiceArrangementPickDAOImpl.class);
    bind(UserDAO.class).to(UserDAOImpl.class);
    bind(WorkDAO.class).to(WorkDAOImpl.class);
  }

}
