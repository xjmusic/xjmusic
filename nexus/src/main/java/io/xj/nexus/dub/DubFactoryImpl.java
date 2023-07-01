package io.xj.nexus.dub;

import io.xj.lib.filestore.FileStoreProvider;
import io.xj.lib.mixer.MixerFactory;
import io.xj.nexus.NexusException;
import io.xj.nexus.fabricator.Fabricator;
import io.xj.nexus.persistence.FilePathProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DubFactoryImpl implements DubFactory {
  private final DubAudioCache dubAudioCache;
  private final FileStoreProvider fileStoreProvider;
  private final MixerFactory mixerFactory;
  private final FilePathProvider filePathProvider;

  @Autowired
  public DubFactoryImpl(
    DubAudioCache dubAudioCache,
    FilePathProvider filePathProvider,
    FileStoreProvider fileStoreProvider,
    MixerFactory mixerFactory
  ) {
    this.dubAudioCache = dubAudioCache;
    this.filePathProvider = filePathProvider;
    this.fileStoreProvider = fileStoreProvider;
    this.mixerFactory = mixerFactory;
  }

  @Override
  public DubMaster master(Fabricator fabricator) throws NexusException {
    return new DubMasterImpl(dubAudioCache, filePathProvider, mixerFactory, fabricator);
  }

  @Override
  public DubUpload upload(Fabricator fabricator) {
    return new DubUploadImpl(filePathProvider, fileStoreProvider, fabricator,"",1,false);
  }
}
