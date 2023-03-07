package io.xj.nexus.dub;

import io.xj.lib.app.AppEnvironment;
import io.xj.lib.filestore.FileStoreException;
import io.xj.lib.http.HttpClientProvider;
import io.xj.nexus.NexusException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class DubAudioCacheItemFactoryImpl implements DubAudioCacheItemFactory {
  private final AppEnvironment env;
  private final HttpClientProvider httpClientProvider;

  @Autowired
  public DubAudioCacheItemFactoryImpl(AppEnvironment env, HttpClientProvider httpClientProvider) {
    this.env = env;
    this.httpClientProvider = httpClientProvider;
  }

  @Override
  public DubAudioCacheItem load(String key, String path) throws IOException, FileStoreException, NexusException {
    return new DubAudioCacheItem(env, httpClientProvider, key, path);
  }
}
