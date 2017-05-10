// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.core.internal;

import io.outright.xj.core.CoreModule;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.model.doc.Doc;

import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;

public class DocProviderImplTest {
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private DocProvider docProvider;
  private Injector injector;

  @Before
  public void setUp() throws Exception {
    createInjector();
    docProvider = injector.getInstance(DocProvider.class);
  }

  @After
  public void tearDown() throws Exception {
    docProvider = null;
  }

  @Test
  public void fetchOne() throws Exception {
    Doc result = docProvider.fetchOne("test-doc");

    assertEquals("test-doc", result.getKey());
    assertEquals("Test Doc", result.getName());
    assertEquals("# Test Doc\n\nThat's all, folks!\n", result.getContent());
  }

  @Test
  public void fetchOne_failsOnBadKey() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Cannot open doc: test-smock");

    docProvider.fetchOne("test-smock");
  }

  @Test
  public void fetchAvailable() throws Exception {
    JSONArray result = docProvider.keysToJSONArray(Lists.newArrayList(
      "test-one",
      "test-two"
    ));

    JSONObject resultOne = (JSONObject) result.get(0);
    assertEquals("test-one", resultOne.get(Doc.KEY_KEY));
    assertEquals("Test One", resultOne.get(Doc.KEY_NAME));
    JSONObject resultTwo = (JSONObject) result.get(1);
    assertEquals("test-two", resultTwo.get(Doc.KEY_KEY));
    assertEquals("Test Two", resultTwo.get(Doc.KEY_NAME));
  }

  private void createInjector() {
    injector = Guice.createInjector(Modules.override(new CoreModule()).with(
      new AbstractModule() {
        @Override
        public void configure() {
//          bind(TokenGenerator.class).toInstance(tokenGenerator);
        }
      }));
  }

}
