package io.xj.core.evaluation;// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.

import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.dao.LibraryDAO;
import io.xj.core.evaluation.digest_chords.DigestChords;
import io.xj.core.evaluation.digest_memes.DigestMemes;
import io.xj.core.model.library.Library;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

/**
 [#154234716] Architect wants evaluation of library contents, to modularize graph mathematics used during craft, and provide the Artist with useful insight for developing the library.
 */
@RunWith(MockitoJUnitRunner.class)
public class EvaluationFactoryImplTest {
  @Mock LibraryDAO libraryDAO;
  private DigestFactory digest;
  private Injector injector;
  private EvaluationFactory subject;

  @Before
  public void setUp() throws Exception {
    injector = createInjector();
    digest = injector.getInstance(DigestFactory.class);
    subject = injector.getInstance(EvaluationFactory.class);
  }

  @After
  public void tearDown() throws Exception {
    subject = null;
    injector = null;
  }

  private Injector createInjector() {
    return Guice.createInjector(Modules.override(new CoreModule()).with(
      new AbstractModule() {
        @Override
        public void configure() {
          bind(LibraryDAO.class).toInstance(libraryDAO);
        }
      }));
  }

  @Test
  public void doEntityEvaluation_LibraryMeme() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    BigInteger entityId = BigInteger.valueOf(17);
    when(libraryDAO.readOne(access, entityId)).thenReturn(new Library(17).setName("My Test Library"));

    DigestMemes result = digest.memesOf(subject.of(access, ImmutableList.of(new Library(entityId))));

    assertNotNull(result);
  }

  @Test
  public void doEntityEvaluation_LibraryChord() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    BigInteger entityId = BigInteger.valueOf(17);
    when(libraryDAO.readOne(access, entityId)).thenReturn(new Library(17).setName("My Test Library"));

    DigestChords result = digest.chordsOf(subject.of(access, ImmutableList.of(new Library(entityId))));

    assertNotNull(result);
  }

  @Test
  public void evaluationType() throws Exception {
    assertEquals(DigestType.DigestMemes, DigestType.validate("DigestMemes"));
    assertEquals(DigestType.DigestChords, DigestType.validate("DigestChords"));
  }

}
