// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.services;

import io.xj.hub.HubContent;
import io.xj.hub.enums.ProgramType;
import io.xj.hub.tables.pojos.Account;
import io.xj.hub.tables.pojos.Template;
import io.xj.nexus.InputMode;
import io.xj.nexus.ControlMode;
import io.xj.nexus.model.Chain;
import io.xj.nexus.model.ChainState;
import io.xj.nexus.model.ChainType;
import io.xj.nexus.model.Segment;
import io.xj.nexus.model.SegmentState;
import io.xj.nexus.persistence.NexusEntityStore;
import io.xj.nexus.work.WorkManager;
import javafx.application.HostServices;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static io.xj.gui.GuiHubIntegrationTestingFixtures.buildAccount;
import static io.xj.gui.GuiHubIntegrationTestingFixtures.buildMainProgramWithBarBeats;
import static io.xj.gui.GuiHubIntegrationTestingFixtures.buildTemplate;
import static io.xj.gui.GuiIntegrationTestingFixtures.buildChain;
import static io.xj.gui.GuiIntegrationTestingFixtures.buildSegment;
import static io.xj.gui.GuiIntegrationTestingFixtures.buildSegmentChoice;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FabricationServiceImplTest {
  int defaultTimelineSegmentViewLimit = 10;
  int defaultCraftAheadSeconds = 5;
  int defaultDubAheadSeconds = 5;
  int defaultMixerLengthSeconds = 10;
  String defaultInputTemplateKey = "slaps_lofi";
  int defaultOutputChannels = 2;
  private final String defaultMacroMode = ControlMode.AUTO.toString();
  String defaultInputMode = InputMode.PRODUCTION.toString();
  int defaultOutputFrameRate = 48000;

  @Mock
  HostServices hostServices;

  @Mock
  LabService labService;

  @Mock
  private NexusEntityStore entityStore;

  @Mock
  private WorkManager workManager;

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
      defaultCraftAheadSeconds,
      defaultDubAheadSeconds,
      defaultMixerLengthSeconds,
      defaultTimelineSegmentViewLimit,
      defaultInputTemplateKey,
      defaultOutputChannels,
      defaultOutputFrameRate,
      defaultMacroMode,
      defaultInputMode,
      labService,
      workManager
    );
  }

  @Test
  void formatTotalBars() {
    var segment4 = prepareStore(4);
    assertEquals("1 bar", subject.formatTotalBars(segment4, 4));
    assertEquals("1¼ bar", subject.formatTotalBars(segment4, 5));
    assertEquals("1½ bar", subject.formatTotalBars(segment4, 6));
    assertEquals("2 bars", subject.formatTotalBars(segment4, 8));
    assertEquals("3 bars", subject.formatTotalBars(segment4, 12));

    var segment3 = prepareStore(3);
    assertEquals("4 bars", subject.formatTotalBars(segment3, 12));
  }

  @Test
  void formatPositionBarBeats() {
    var segment4 = prepareStore(4);
    assertEquals("1.1", subject.formatPositionBarBeats(segment4, 0.0));
    assertEquals("2.1", subject.formatPositionBarBeats(segment4, 4.0));
    assertEquals("2.2", subject.formatPositionBarBeats(segment4, 5.0));
    assertEquals("2.3", subject.formatPositionBarBeats(segment4, 6.0));
    assertEquals("2.3.5", subject.formatPositionBarBeats(segment4, 6.5));
    assertEquals("2.3.523", subject.formatPositionBarBeats(segment4, 6.523));
    assertEquals("3.1", subject.formatPositionBarBeats(segment4, 8.0));
    assertEquals("4.1", subject.formatPositionBarBeats(segment4, 12.0));

    var segment3 = prepareStore(3);
    assertEquals("5.1", subject.formatPositionBarBeats(segment3, 12.0));
    assertEquals("5.3.5", subject.formatPositionBarBeats(segment3, 14.5));
  }

  private Segment prepareStore(int barBeats) {
    var segment = buildSegment(
      chain,
      offset.getAndIncrement(),
      SegmentState.PLANNED,
      "C",
      8,
      0.8f,
      120.0f,
      "chain-segment-0.wav");
    var program = buildMainProgramWithBarBeats(barBeats);
    var sourceMaterial = new HubContent(List.of(program));
    var choice = buildSegmentChoice(segment, program);
    when(workManager.getSourceMaterial()).thenReturn(sourceMaterial);
    when(workManager.getEntityStore()).thenReturn(entityStore);
    when(entityStore.readChoice(eq(segment.getId()), eq(ProgramType.Main))).thenReturn(Optional.of(choice));
    return segment;
  }
}
