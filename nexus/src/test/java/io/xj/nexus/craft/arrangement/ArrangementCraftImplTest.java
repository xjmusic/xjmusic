package io.xj.nexus.craft.arrangement;

import com.typesafe.config.Config;
import io.xj.api.Chain;
import io.xj.api.Library;
import io.xj.api.Program;
import io.xj.api.ProgramType;
import io.xj.api.ProgramVoice;
import io.xj.api.SegmentChoice;
import io.xj.api.SegmentType;
import io.xj.api.TemplateBinding;
import io.xj.lib.entity.common.TemplateConfig;
import io.xj.nexus.NexusException;
import io.xj.nexus.NexusTestConfiguration;
import io.xj.nexus.fabricator.Fabricator;
import io.xj.nexus.fabricator.SegmentRetrospective;
import io.xj.nexus.hub_client.client.HubContent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.UUID;
import java.util.function.Predicate;

import static io.xj.hub.IntegrationTestingFixtures.buildTemplate;
import static io.xj.hub.IntegrationTestingFixtures.buildTemplateBinding;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildAccount;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildLibrary;
import static io.xj.nexus.craft.detail.DetailCraftImpl.DETAIL_INSTRUMENT_TYPES;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ArrangementCraftImplTest {
  @Mock
  public Fabricator fabricator;
  @Mock
  public HubContent hubContent;
  @Mock
  public SegmentRetrospective retrospective;
  private ArrangementCraftImpl subject;

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
    var account1 = buildAccount("fish");
    Library library1 = buildLibrary(account1, "test");
    var template1 = buildTemplate(account1, "Test Template 1", "test1");
    TemplateBinding templateBinding1 = buildTemplateBinding(template1, library1);
    TemplateConfig templateConfig = new TemplateConfig(template1, config);
    when(fabricator.getTemplateConfig()).thenReturn(templateConfig);
    when(fabricator.retrospective()).thenReturn(retrospective);
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
