package io.xj.nexus.dub;

import io.xj.lib.filestore.FileStoreException;
import io.xj.lib.http.HttpClientProvider;
import io.xj.nexus.NexusException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class DubAudioCacheItemFactoryImpl implements DubAudioCacheItemFactory {
  final HttpClientProvider httpClientProvider;
  final String audioFileBucket;
  final String audioBaseUrl;

  @Autowired
  public DubAudioCacheItemFactoryImpl(HttpClientProvider httpClientProvider,
                                      @Value("${audio.file.bucket}") String audioFileBucket,
                                      @Value("${audio.base.url}") String audioBaseUrl) {
    this.httpClientProvider = httpClientProvider;
    this.audioFileBucket = audioFileBucket;
    this.audioBaseUrl = audioBaseUrl;
  }

  @Override
  public DubAudioCacheItem load(String cachePathPrefix, String waveformKey, int targetFrameRate) throws IOException, FileStoreException, NexusException {
    return new DubAudioCacheItem(audioBaseUrl, audioFileBucket, httpClientProvider, waveformKey, cachePathPrefix, targetFrameRate);
  }
}
