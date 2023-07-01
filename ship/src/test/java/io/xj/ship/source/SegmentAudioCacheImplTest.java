// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.ship.source;


import io.xj.hub.enums.TemplateType;
import io.xj.lib.filestore.FileStoreException;
import io.xj.lib.http.HttpClientProvider;
import io.xj.lib.mixer.InternalResource;
import io.xj.nexus.model.Chain;
import io.xj.nexus.model.ChainState;
import io.xj.nexus.model.ChainType;
import io.xj.nexus.model.Segment;
import io.xj.nexus.model.SegmentState;
import io.xj.ship.ShipException;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.Objects;

import static io.xj.hub.IntegrationTestingFixtures.buildAccount;
import static io.xj.hub.IntegrationTestingFixtures.buildTemplate;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildChain;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildSegment;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SegmentAudioCacheImplTest {
  @Mock(lenient = true)
  public HttpClientProvider httpClientProvider;
  @Mock(lenient = true)
  public CloseableHttpClient httpClient;
  @Mock(lenient = true)
  public CloseableHttpResponse httpResponse;
  @Mock(lenient = true)
  public HttpEntity httpResponseEntity;
  @Mock(lenient = true)
  public StatusLine httpStatusLine;
  private Segment segment1;
  private SegmentAudioCache subject;

  @Before
  public void setUp() throws IOException {
    var account1 = buildAccount("Test");
    var template1 = buildTemplate(account1, TemplateType.Production, "Test 123", "test123");
    Chain chain1 = buildChain(
      account1,
      template1,
      "Test Print #1",
      ChainType.PRODUCTION,
      ChainState.FABRICATE,
      Instant.parse("2014-08-12T12:17:02.527142Z"));
    segment1 = buildSegment(
      chain1,
      1,
      SegmentState.CRAFTED,
      Instant.parse("2017-12-12T01:00:08.000000Z"),
      Instant.parse("2017-12-12T01:00:16.000000Z"),
      "F major",
      8,
      0.6,
      120.0,
      "seg123",
      "ogg");
    segment1.setWaveformPreroll(1.7306228);
    segment1.setWaveformPostroll(1.205893);

    when(httpClientProvider.getClient()).thenReturn(httpClient);
    when(httpClient.execute(any())).thenReturn(httpResponse);
    when(httpResponse.getEntity()).thenReturn(httpResponseEntity);
    when(httpResponse.getStatusLine()).thenReturn(httpStatusLine);

    subject = new SegmentAudioCacheImpl(httpClientProvider,"","","");
  }

  @Test
  public void downloadAndDecompress() throws FileStoreException, ShipException, IOException, InterruptedException {
    when(httpResponseEntity.getContent())
      .thenAnswer((Answer<InputStream>) invocation -> new FileInputStream(Objects.requireNonNull(
        new InternalResource("ogg_decoding/coolair-1633586832900943.ogg").getFile())));
    when(httpStatusLine.getStatusCode()).thenReturn(200);

    var result = subject.downloadAndDecompress(segment1);

    assertEquals("/tmp/cache/seg123.wav", result);
  }
}
