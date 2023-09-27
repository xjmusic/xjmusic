// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.services;

import io.xj.hub.HubContent;
import io.xj.hub.enums.ProgramType;
import io.xj.hub.tables.pojos.Account;
import io.xj.hub.tables.pojos.Template;
import io.xj.nexus.OutputFileMode;
import io.xj.nexus.OutputMode;
import io.xj.nexus.model.*;
import io.xj.nexus.persistence.ManagerFatalException;
import io.xj.nexus.persistence.SegmentManager;
import io.xj.nexus.work.WorkFactory;
import javafx.application.HostServices;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static io.xj.gui.GuiHubIntegrationTestingFixtures.*;
import static io.xj.gui.GuiIntegrationTestingFixtures.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FabricationServiceImplTest {
  Integer defaultTimelineSegmentViewLimit = 10;
  Integer defaultCraftAheadSeconds = 5;
  Integer defaultDubAheadSeconds = 5;
  Integer defaultShipAheadSeconds = 5;
  String defaultInputTemplateKey = "slaps_lofi";
  int defaultOutputChannels = 2;
  String defaultOutputFileMode = OutputFileMode.CONTINUOUS.toString();
  double defaultOutputFrameRate = 48000;
  String defaultOutputMode = OutputMode.PLAYBACK.toString();
  Integer defaultOutputSeconds = 300;

  @Mock
  HostServices hostServices;

  @Mock
  WorkFactory workFactory;

  @Mock
  LabService labService;

  @Mock
  private SegmentManager segmentManager;

  FabricationServiceImpl subject;
  private Chain chain;
  private final AtomicInteger offset = new AtomicInteger(0);

  @BeforeEach
  void setUp() {
    Account account = buildAccount();
    Template template = buildTemplate(account, "Test Template");
    chain = buildChain(
      account,
      "Test",
      ChainType.PRODUCTION,
      ChainState.FABRICATE,
      template
    );
    subject = new FabricationServiceImpl(
      hostServices,
      defaultTimelineSegmentViewLimit,
      defaultCraftAheadSeconds,
      defaultDubAheadSeconds,
      defaultShipAheadSeconds,
      defaultInputTemplateKey,
      defaultOutputChannels,
      defaultOutputFileMode,
      defaultOutputFrameRate,
      defaultOutputMode,
      defaultOutputSeconds,
      workFactory,
      labService
    );

    when(workFactory.getSegmentManager()).thenReturn(segmentManager);
  }

  @Test
  void formatTotalBars() throws ManagerFatalException {
    var segment4 = prepareSegmentManager(4);
    assertEquals("1 bar", subject.formatTotalBars(segment4, 4));
    assertEquals("1¼ bar", subject.formatTotalBars(segment4, 5));
    assertEquals("1½ bar", subject.formatTotalBars(segment4, 6));
    assertEquals("2 bars", subject.formatTotalBars(segment4, 8));
    assertEquals("3 bars", subject.formatTotalBars(segment4, 12));

    var segment3 = prepareSegmentManager(3);
    assertEquals("4 bars", subject.formatTotalBars(segment3, 12));
  }

  @Test
  void formatPositionBarBeats() throws ManagerFatalException {
    var segment4 = prepareSegmentManager(4);
    assertEquals("1.1", subject.formatPositionBarBeats(segment4, 0.0));
    assertEquals("2.1", subject.formatPositionBarBeats(segment4, 4.0));
    assertEquals("2.2", subject.formatPositionBarBeats(segment4, 5.0));
    assertEquals("2.3", subject.formatPositionBarBeats(segment4, 6.0));
    assertEquals("2.3.5", subject.formatPositionBarBeats(segment4, 6.5));
    assertEquals("2.3.523", subject.formatPositionBarBeats(segment4, 6.523));
    assertEquals("3.1", subject.formatPositionBarBeats(segment4, 8.0));
    assertEquals("4.1", subject.formatPositionBarBeats(segment4, 12.0));

    var segment3 = prepareSegmentManager(3);
    assertEquals("5.1", subject.formatPositionBarBeats(segment3, 12.0));
    assertEquals("5.3.5", subject.formatPositionBarBeats(segment3, 14.5));
  }

  private Segment prepareSegmentManager(int barBeats) throws ManagerFatalException {
    var segment = buildSegment(
      chain,
      offset.getAndIncrement(),
      SegmentState.PLANNED,
      "C",
      8,
      0.8,
      120.0,
      "chain-segment-0.wav");
    var program = buildMainProgramWithBarBeats(barBeats);
    var sourceMaterial = new HubContent(List.of(program));
    var choice = buildSegmentChoice(segment, program);
    when(segmentManager.readChoice(eq(segment.getId()), eq(ProgramType.Main))).thenReturn(Optional.of(choice));
    when(workFactory.getSourceMaterial()).thenReturn(sourceMaterial);
    return segment;
  }
}
