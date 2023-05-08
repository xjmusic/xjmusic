// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.lock;

import io.xj.lib.filestore.FileStoreException;
import io.xj.lib.filestore.FileStoreProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FabricatorLockTest {
  LockProvider subject;
  @Mock
  private LockGenerator lockGenerator;
  @Mock
  private FileStoreProvider fileStoreProvider;

  @Before
  public void setUp() {
    subject = new LockProviderImpl(fileStoreProvider, lockGenerator);
  }

  @Test
  public void acquire() throws FileStoreException, LockException {
    when(lockGenerator.get()).thenReturn("at03wth4");

    subject.acquire("jams", "strawberry");

    verify(fileStoreProvider).putS3ObjectFromString("at03wth4", "jams", "strawberry.lock", "text/plain", null);
  }

  @Test
  public void acquire_exactlyOncePerKey() throws FileStoreException, LockException {
    when(lockGenerator.get()).thenReturn("at03wth4");

    subject.acquire("jams", "strawberry");
    subject.acquire("jams", "strawberry");

    verify(fileStoreProvider, times(1)).putS3ObjectFromString("at03wth4", "jams", "strawberry.lock", "text/plain", null);
    verify(lockGenerator, times(1)).get();
  }

  @Test
  public void check() throws FileStoreException, LockException {
    when(lockGenerator.get()).thenReturn("at03wth4");
    when(fileStoreProvider.getS3Object("jams", "strawberry.lock")).thenReturn("at03wth4");
    subject.acquire("jams", "strawberry");

    subject.check("jams", "strawberry");

    verify(fileStoreProvider).getS3Object("jams", "strawberry.lock");
    verify(fileStoreProvider).putS3ObjectFromString("at03wth4", "jams", "strawberry.lock", "text/plain", null);
    verify(lockGenerator, times(1)).get();
  }

  @Test
  public void check_exceptionWhenChanged() throws LockException {
    when(lockGenerator.get()).thenReturn("at03wth4");
    when(fileStoreProvider.getS3Object("jams", "strawberry.lock")).thenReturn("changed");
    subject.acquire("jams", "strawberry");

    assertThrows(LockException.class, () -> subject.check("jams", "strawberry"));
  }
}
