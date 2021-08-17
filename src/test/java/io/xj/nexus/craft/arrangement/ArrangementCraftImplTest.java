package io.xj.nexus.craft.arrangement;

import com.google.common.collect.ImmutableList;
import com.typesafe.config.Config;
import io.xj.api.Chain;
import io.xj.api.Program;
import io.xj.api.ProgramType;
import io.xj.api.ProgramVoice;
import io.xj.api.SegmentChoice;
import io.xj.api.SegmentType;
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
    Chain chain = new Chain()
      .id(UUID.randomUUID());
    Program program = new Program()
      .id(UUID.randomUUID());
    ProgramVoice programVoice1 = new ProgramVoice()
      .id(UUID.randomUUID())
      .programId(program.getId());
    ProgramVoice programVoice2 = new ProgramVoice()
      .id(UUID.randomUUID())
      .programId(program.getId());
    ChainConfig chainConfig = new ChainConfig(chain, config);
    when(fabricator.getChainConfig()).thenReturn(chainConfig);
    when(fabricator.sourceMaterial()).thenReturn(hubContent);
    when(fabricator.retrospective()).thenReturn(retrospective);
    when(hubContent.getAllProgramVoices()).thenReturn(ImmutableList.of(programVoice1, programVoice2));
    subject = new ArrangementCraftImpl(fabricator);
  }

  @Test
  public void precomputeDeltas() throws NexusException {
    when(fabricator.getType()).thenReturn(SegmentType.NEXTMAIN);
    ArrangementCraftImpl.ChoiceIndexProvider choiceIndexProvider = (SegmentChoice choice) -> choice.getInstrumentType().toString();
    Predicate<SegmentChoice> choiceFilter = (SegmentChoice choice) -> ProgramType.DETAIL.equals(choice.getProgramType());
    subject.precomputeDeltas(choiceFilter, choiceIndexProvider, DETAIL_INSTRUMENT_TYPES, 0.38);
  }
}
