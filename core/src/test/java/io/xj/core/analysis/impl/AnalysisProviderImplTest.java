package io.xj.core.analysis.impl;// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.

import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.dao.LibraryDAO;
import io.xj.core.model.analysis.AnalysisType;
import io.xj.core.model.analysis.library_chord.LibraryChordAnalysis;
import io.xj.core.model.analysis.library_meme.LibraryMemeAnalysis;
import io.xj.core.model.library.Library;

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
 [#154234716] Architect wants analysis of library contents, to modularize graph mathematics used during craft, and provide the Artist with useful insight for developing the library.
 */
@RunWith(MockitoJUnitRunner.class)
public class AnalysisProviderImplTest {
  @Mock LibraryDAO libraryDAO;
  private Injector injector;
  private AnalysisProviderImpl subject;

  @Before
  public void setUp() throws Exception {
    injector = createInjector();
    subject = injector.getInstance(AnalysisProviderImpl.class);
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
  public void doEntityAnalysis_LibraryMeme() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    BigInteger entityId = BigInteger.valueOf(17);
    when(libraryDAO.readOne(access, entityId)).thenReturn(new Library(17).setName("My Test Library"));

    LibraryMemeAnalysis result = subject.analyzeLibraryMemes(access, entityId);

    assertNotNull(result);
  }

  @Test
  public void doEntityAnalysis_LibraryChord() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    BigInteger entityId = BigInteger.valueOf(17);
    when(libraryDAO.readOne(access, entityId)).thenReturn(new Library(17).setName("My Test Library"));

    LibraryChordAnalysis result = subject.analyzeLibraryChords(access, entityId);

    assertNotNull(result);
  }

  @Test
  public void analysisType() throws Exception {
    assertEquals(AnalysisType.LibraryMeme, AnalysisType.validate("LibraryMeme"));
    assertEquals(AnalysisType.LibraryChord, AnalysisType.validate("LibraryChord"));
  }

}
