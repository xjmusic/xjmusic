// Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import com.google.inject.AbstractModule;
import io.xj.core.dao.impl.AccountDAOImpl;
import io.xj.core.dao.impl.AccountUserDAOImpl;
import io.xj.core.dao.impl.ChainDAOImpl;
import io.xj.core.dao.impl.ChainBindingDAOImpl;
import io.xj.core.dao.impl.ChainConfigDAOImpl;
import io.xj.core.dao.impl.InstrumentDAOImpl;
import io.xj.core.dao.impl.InstrumentAudioDAOImpl;
import io.xj.core.dao.impl.InstrumentAudioChordDAOImpl;
import io.xj.core.dao.impl.InstrumentAudioEventDAOImpl;
import io.xj.core.dao.impl.InstrumentMemeDAOImpl;
import io.xj.core.dao.impl.LibraryDAOImpl;
import io.xj.core.dao.impl.PlatformMessageDAOImpl;
import io.xj.core.dao.impl.ProgramDAOImpl;
import io.xj.core.dao.impl.ProgramSequencePatternEventDAOImpl;
import io.xj.core.dao.impl.ProgramMemeDAOImpl;
import io.xj.core.dao.impl.ProgramSequencePatternDAOImpl;
import io.xj.core.dao.impl.ProgramSequenceDAOImpl;
import io.xj.core.dao.impl.ProgramSequenceBindingDAOImpl;
import io.xj.core.dao.impl.ProgramSequenceBindingMemeDAOImpl;
import io.xj.core.dao.impl.ProgramSequenceChordDAOImpl;
import io.xj.core.dao.impl.ProgramVoiceTrackDAOImpl;
import io.xj.core.dao.impl.ProgramVoiceDAOImpl;
import io.xj.core.dao.impl.SegmentDAOImpl;
import io.xj.core.dao.impl.SegmentChoiceArrangementDAOImpl;
import io.xj.core.dao.impl.SegmentChoiceDAOImpl;
import io.xj.core.dao.impl.SegmentChordDAOImpl;
import io.xj.core.dao.impl.SegmentMemeDAOImpl;
import io.xj.core.dao.impl.SegmentMessageDAOImpl;
import io.xj.core.dao.impl.SegmentChoiceArrangementPickDAOImpl;
import io.xj.core.dao.impl.UserDAOImpl;
import io.xj.core.dao.impl.WorkDAOImpl;

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
