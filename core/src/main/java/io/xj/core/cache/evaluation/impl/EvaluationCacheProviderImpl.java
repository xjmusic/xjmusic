// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.cache.evaluation.impl;

import io.xj.core.access.impl.Access;
import io.xj.core.cache.evaluation.EvaluationCacheProvider;
import io.xj.core.dao.LibraryDAO;
import io.xj.core.evaluation.DigestFactory;
import io.xj.core.evaluation.EvaluationFactory;
import io.xj.core.evaluation.digest_chords.DigestChords;
import io.xj.core.evaluation.digest_memes.DigestMemes;
import io.xj.core.model.library.Library;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.math.BigInteger;
import java.util.Map;

@Singleton
public class EvaluationCacheProviderImpl implements EvaluationCacheProvider {
  private final EvaluationFactory evaluation;
  private final LibraryDAO libraryDAO;
  private final Map<BigInteger, DigestMemes> _libraryMemes = Maps.newConcurrentMap();
  private final Map<BigInteger, DigestChords> _libraryChords = Maps.newConcurrentMap();
  private final DigestFactory digest;
  private String _libraryMemesHash;
  private String _libraryChordsHash;

  @Inject
  EvaluationCacheProviderImpl(
    DigestFactory digest,
    EvaluationFactory evaluation,
    LibraryDAO libraryDAO
  ) {
    this.digest = digest;
    this.evaluation = evaluation;
    this.libraryDAO = libraryDAO;
  }

  @Override
  public DigestMemes libraryMemes(Access access, BigInteger libraryId) throws Exception {
    String hash = libraryDAO.readHash(access, libraryId).toString();
    if (!Objects.equal(_libraryMemesHash, hash)) {
      _libraryMemes.put(libraryId, digest.memesOf(evaluation.of(access, ImmutableList.of(new Library(libraryId)))));
      _libraryMemesHash = hash;
    }

    return _libraryMemes.get(libraryId);
  }

  @Override
  public DigestChords libraryChords(Access access, BigInteger libraryId) throws Exception {
    String hash = libraryDAO.readHash(access, libraryId).toString();
    if (!Objects.equal(_libraryChordsHash, hash)) {
      _libraryChords.put(libraryId, digest.chordsOf(evaluation.of(access, ImmutableList.of(new Library(libraryId)))));
      _libraryChordsHash = hash;
    }

    return _libraryChords.get(libraryId);
  }

}
