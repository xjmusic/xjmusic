package io.xj.nexus.craft.arrangement;

import com.typesafe.config.Config;
import io.xj.Chain;
import io.xj.Segment;
import io.xj.nexus.NexusException;
import io.xj.nexus.dao.ChainConfig;
import io.xj.nexus.fabricator.Fabricator;
import io.xj.nexus.testing.NexusTestConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.UUID;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ArrangementCraftImplTest {
  private ArrangementCraftImpl subject;
  private Chain chain;
  private ChainConfig chainConfig;

  @Mock
  public Fabricator fabricator;

  @Before
  public void setUp() throws Exception {
    Config config = NexusTestConfiguration.getDefault();
    chain = Chain.newBuilder()
      .setId(UUID.randomUUID().toString())
      .build();
    chainConfig = new ChainConfig(chain, config);
    when(fabricator.getChainConfig()).thenReturn(chainConfig);
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
  }
}
