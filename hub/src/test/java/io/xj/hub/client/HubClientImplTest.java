// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.hub.client;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.Fault;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValueFactory;
import io.xj.Account;
import io.xj.User;
import io.xj.UserAuth;
import io.xj.UserRole;
import io.xj.hub.HubContentFixtures;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.entity.EntityModule;
import io.xj.lib.entity.common.Topology;
import io.xj.lib.jsonapi.JsonApiException;
import io.xj.lib.jsonapi.JsonApiModule;
import io.xj.lib.jsonapi.MediaType;
import io.xj.lib.jsonapi.PayloadFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.Collection;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static org.junit.Assert.assertEquals;

/**
 [#171553408] XJ Lab Distributed Architecture
 <p>
 HubClient** allows a service that depends on Hub (e.g. Nexus) to connect to the Hub REST API via an HTTP client and deserialize results into usable entities
 */
public class HubClientImplTest {
  private static final String INGEST_RETRY_SCENARIO = "Ingest Retry";
  private static final String SECOND_ATTEMPT_STATE = "Second Attempt";
  @Rule
  public WireMockRule wireMockRule = new WireMockRule(8089); // No-args constructor defaults to port 8080

  PayloadFactory payloadFactory;

  HubClient subject;
  private HubContentFixtures content;
  private Collection<?> hubEntities;

  @Before
  public void setUp() throws Exception {
    Config config = ConfigFactory.parseResources("test.conf")
      .withValue("access.tokenName", ConfigValueFactory.fromAnyRef("access_token"))
      .withValue("hub.baseUrl", ConfigValueFactory.fromAnyRef("http://localhost:8089/"))
      .withValue("hub.internalToken", ConfigValueFactory.fromAnyRef("internal_secret_456"));

    var injector = Guice.createInjector(
      ImmutableSet.of(
        new HubClientModule(),
        new EntityModule(),
        new JsonApiModule(),
        new AbstractModule() {
          @Override
          protected void configure() {
            bind(Config.class).toInstance(config);
          }
        }));
    Topology.buildHubApiTopology(injector.getInstance(EntityFactory.class));
    content = new HubContentFixtures();
    payloadFactory = injector.getInstance(PayloadFactory.class);
    subject = injector.getInstance(HubClient.class);
    hubEntities = content.setupFixtureB1(false);
  }

  @Test
  public void ingest() throws JsonApiException, HubClientException {
    String hubContentBody = payloadFactory.serialize(payloadFactory.newJsonapiPayload()
      .setDataMany(payloadFactory.toPayloadObjects(hubEntities)));
    stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlPathEqualTo("/api/1/ingest"))
      .withQueryParams(ImmutableMap.of(
        "libraryIds", new EqualToPattern(content.library2.getId()),
        "programIds", new EqualToPattern(""),
        "instrumentIds", new EqualToPattern("")
      ))
      .withHeader("Cookie", equalTo("access_token=internal_secret_456"))
      .willReturn(aResponse()
        .withStatus(200)
        .withHeader("Content-Type", MediaType.APPLICATION_JSON)
        .withBody(hubContentBody)));
    HubClientAccess access = HubContentFixtures.buildHubClientAccess(content.user2, ImmutableList.of(content.account1), "Artist")
      .setToken("secret_token_123");

    HubContent result = subject.ingest(access, ImmutableSet.of(content.library2.getId()), ImmutableSet.of(), ImmutableSet.of());

    assertEquals(48, result.getAll().size());
    assertEquals(0, result.getAllInstrumentAudioChords().size());
    assertEquals(0, result.getAllInstrumentAudios().size());
    assertEquals(0, result.getAllInstrumentAudioEvents().size());
    assertEquals(0, result.getAllInstruments().size());
    assertEquals(0, result.getAllInstrumentMemes().size());
    assertEquals(5, result.getAllPrograms().size());
    assertEquals(8, result.getAllProgramSequencePatternEvents().size());
    assertEquals(3, result.getAllProgramMemes().size());
    assertEquals(2, result.getAllProgramSequencePatterns().size());
    assertEquals(5, result.getAllProgramSequenceBindings().size());
    assertEquals(6, result.getAllProgramSequenceBindingMemes().size());
    assertEquals(5, result.getAllProgramSequenceChords().size());
    assertEquals(3, result.getAllProgramSequenceChordVoicings().size());
    assertEquals(6, result.getAllProgramSequences().size());
    assertEquals(4, result.getAllProgramVoiceTracks().size());
    assertEquals(1, result.getAllProgramVoices().size());
  }

  @Test
  public void ingest_retriesIfUnavailable() throws JsonApiException, HubClientException {
    String hubContentBody = payloadFactory.serialize(payloadFactory.newJsonapiPayload()
      .setDataMany(payloadFactory.toPayloadObjects(hubEntities)));
    var hubQueryParams = ImmutableMap.<String, StringValuePattern>of(
      "libraryIds", new EqualToPattern(content.library2.getId()),
      "programIds", new EqualToPattern(""),
      "instrumentIds", new EqualToPattern("")
    );
    HubClientAccess access = HubContentFixtures
      .buildHubClientAccess(content.user2, ImmutableList.of(content.account1), "Artist")
      .setToken("secret_token_123");

    // first attempt fails
    stubFor(WireMock.get(urlPathEqualTo("/api/1/ingest"))
      .withQueryParams(hubQueryParams)
      .withHeader("Cookie", equalTo("access_token=internal_secret_456"))
      .inScenario(INGEST_RETRY_SCENARIO)
      .whenScenarioStateIs(STARTED)
      .willSetStateTo(SECOND_ATTEMPT_STATE)
      .willReturn(aResponse()
        .withFault(Fault.CONNECTION_RESET_BY_PEER)));

    // second attempt succeeds
    stubFor(WireMock.get(urlPathEqualTo("/api/1/ingest"))
      .withQueryParams(hubQueryParams)
      .withHeader("Cookie", equalTo("access_token=internal_secret_456"))
      .inScenario(INGEST_RETRY_SCENARIO)
      .whenScenarioStateIs(SECOND_ATTEMPT_STATE)
      .willReturn(aResponse()
        .withStatus(200)
        .withHeader("Content-Type", MediaType.APPLICATION_JSON)
        .withBody(hubContentBody)));

    HubContent result = subject.ingest(access, ImmutableSet.of(content.library2.getId()), ImmutableSet.of(), ImmutableSet.of());

    assertEquals(48, result.getAll().size());
  }

  @Test
  public void auth() throws HubClientException {
    User user1 = User.newBuilder()
      .setId(UUID.randomUUID().toString())
      .build();
    var account1 = Account.newBuilder()
      .setId(UUID.randomUUID().toString())
      .build();
    UserAuth userAuth1 = UserAuth.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setUserId(user1.getId())
      .setType(UserAuth.Type.Google)
      .build();
    stubFor(com.github.tomakehurst.wiremock.client.WireMock.any(urlEqualTo("/auth"))
      .withHeader("Cookie", equalTo("access_token=secret_token_123"))
      .willReturn(aResponse()
        .withStatus(200)
        .withHeader("Content-Type", MediaType.APPLICATION_JSON)
        .withBody(String.format("{\"userAuthId\":\"%s\",\"roles\":\"User,Admin\",\"accountIds\":[\"%s\"],\"userId\":\"%s\"}",
          userAuth1.getId(), account1.getId(), user1.getId()))));

    HubClientAccess result = subject.auth("secret_token_123");

    assertEquals(2, result.getRoleTypes().size());
    assertEquals(UserRole.Type.User, result.getRoleTypes().toArray()[0]);
    assertEquals(UserRole.Type.Admin, result.getRoleTypes().toArray()[1]);
    assertEquals(1, result.getAccountIds().size());
    assertEquals(account1.getId(), result.getAccountIds().toArray()[0]);
    assertEquals(user1.getId(), result.getUserId());
    assertEquals(userAuth1.getId(), result.getUserAuthId());
  }

}
