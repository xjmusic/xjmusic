package io.xj.nexus.craft.arrangement;

import com.google.common.collect.ImmutableList;
import com.typesafe.config.Config;
import io.xj.Chain;
import io.xj.Program;
import io.xj.ProgramVoice;
import io.xj.Segment;
import io.xj.SegmentChoice;
import io.xj.nexus.NexusException;
import io.xj.nexus.dao.ChainConfig;
import io.xj.nexus.fabricator.Fabricator;
import io.xj.nexus.fabricator.SegmentRetrospective;
import io.xj.nexus.hub_client.client.HubContent;
import io.xj.nexus.testing.NexusTestConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.UUID;
import java.util.function.Predicate;

import static io.xj.nexus.craft.detail.DetailCraftImpl.DETAIL_INSTRUMENT_TYPES;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ArrangementCraftImplTest {
  private ArrangementCraftImpl subject;

  @Mock
  public Fabricator fabricator;

  @Mock
  public HubContent hubContent;

  @Mock
  public SegmentRetrospective retrospective;

  @Before
  public void setUp() throws Exception {
    Config config = NexusTestConfiguration.getDefault();
    Chain chain = Chain.newBuilder()
      .setId(UUID.randomUUID().toString())
      .build();
    Program program = Program.newBuilder()
      .setId(UUID.randomUUID().toString())
      .build();
    ProgramVoice programVoice1 = ProgramVoice.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setProgramId(program.getId())
      .build();
    ProgramVoice programVoice2 = ProgramVoice.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setProgramId(program.getId())
      .build();
    ChainConfig chainConfig = new ChainConfig(chain, config);
    when(fabricator.getChainConfig()).thenReturn(chainConfig);
    when(fabricator.sourceMaterial()).thenReturn(hubContent);
    when(fabricator.retrospective()).thenReturn(retrospective);
    when(hubContent.getAllProgramVoices()).thenReturn(ImmutableList.of(programVoice1, programVoice2));
    subject = new ArrangementCraftImpl(fabricator);
  }

  @Test
  public void precomputeDeltas() throws NexusException {
    when(fabricator.getType()).thenReturn(Segment.Type.NextMain);
    ArrangementCraftImpl.ChoiceIndexProvider choiceIndexProvider = (SegmentChoice choice) -> choice.getInstrumentType().toString();
    Predicate<SegmentChoice> choiceFilter = (SegmentChoice choice) -> Program.Type.Detail.equals(choice.getProgramType());
    subject.precomputeDeltas(choiceFilter, choiceIndexProvider, DETAIL_INSTRUMENT_TYPES, 0.38);
  }
}
