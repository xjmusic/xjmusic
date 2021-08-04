package io.xj.nexus.craft.arrangement;

import com.google.common.collect.ImmutableList;
import com.typesafe.config.Config;
import io.xj.Chain;
import io.xj.Program;
import io.xj.ProgramVoice;
import io.xj.Segment;
import io.xj.nexus.NexusException;
import io.xj.nexus.dao.ChainConfig;
import io.xj.nexus.fabricator.Fabricator;
import io.xj.nexus.hub_client.client.HubContent;
import io.xj.nexus.testing.NexusTestConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.UUID;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ArrangementCraftImplTest {
  private ArrangementCraftImpl subject;

  @Mock
  public Fabricator fabricator;

  @Mock
  public HubContent hubContent;

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
    when(hubContent.getAllProgramVoices()).thenReturn(ImmutableList.of(programVoice1, programVoice2));
    subject = new ArrangementCraftImpl(fabricator);
  }

  @Test
  public void precomputeRhythmDeltas() throws NexusException {
    when(fabricator.getType()).thenReturn(Segment.Type.NextMain);
    String programId = UUID.randomUUID().toString();
    subject.precomputeRhythmDeltas(programId);
  }

  @Test
  public void precomputeDetailDeltas() throws NexusException {
    when(fabricator.getType()).thenReturn(Segment.Type.NextMain);
    subject.precomputeDetailDeltas();

    ArgumentCaptor<String> arg = ArgumentCaptor.forClass(String.class);
    verify(fabricator,times(2)).addMessageInfo(arg.capture());
  }
}
