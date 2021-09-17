package io.xj.nexus.craft.arrangement;

import com.typesafe.config.Config;
import io.xj.api.SegmentChoice;
import io.xj.api.SegmentType;
import io.xj.hub.TemplateConfig;
import io.xj.hub.enums.ProgramType;
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

import java.util.function.Predicate;

import static io.xj.hub.IntegrationTestingFixtures.buildAccount;
import static io.xj.hub.IntegrationTestingFixtures.buildTemplate;
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
//    Chain chain = new Chain();
//    chain.setId(UUID.randomUUID());
//    Program program = new Program();
//    program.setId(UUID.randomUUID());
//    ProgramVoice programVoice1 = new ProgramVoice();
//    programVoice1.setId(UUID.randomUUID());
//    programVoice1.setProgramId(program.getId());
//    ProgramVoice programVoice2 = new ProgramVoice();
//    programVoice2.setId(UUID.randomUUID());
//    programVoice2.setProgramId(program.getId());
    var account1 = buildAccount("fish");
//    Library library1 = buildLibrary(account1, "test");
    var template1 = buildTemplate(account1, "Test Template 1", "test1");
//    TemplateBinding templateBinding1 = buildTemplateBinding(template1, library1);
    TemplateConfig templateConfig = new TemplateConfig(template1, config);
    when(fabricator.getTemplateConfig()).thenReturn(templateConfig);
    when(fabricator.retrospective()).thenReturn(retrospective);
    subject = new ArrangementCraftImpl(fabricator);
  }

  @Test
  public void precomputeDeltas() throws NexusException {
    when(fabricator.getType()).thenReturn(SegmentType.NEXTMAIN);
    ArrangementCraftImpl.ChoiceIndexProvider choiceIndexProvider = SegmentChoice::getInstrumentType;
    Predicate<SegmentChoice> choiceFilter = (SegmentChoice choice) -> ProgramType.Detail.toString().equals(choice.getProgramType());
    subject.precomputeDeltas(choiceFilter, choiceIndexProvider, DETAIL_INSTRUMENT_TYPES, 0.38);
  }
}
