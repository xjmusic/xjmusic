//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.core.transport.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.xj.core.CoreModule;
import io.xj.core.exception.CoreException;
import io.xj.core.model.sequence_meme.SequenceMeme;
import io.xj.core.transport.HttpResponseProvider;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.math.BigInteger;

import static org.junit.Assert.assertEquals;

public class HttpResponseProviderImplTest {
  Injector injector = Guice.createInjector(new CoreModule());
  HttpResponseProvider subject;

  private static SequenceMeme meme(String name) {
    return new SequenceMeme()
      .setName(name)
      .setSequenceId(BigInteger.valueOf(25))
      .setId(BigInteger.valueOf(23));
  }

  @Before
  public void setUp() throws Exception {
    subject = injector.getInstance(HttpResponseProvider.class);
  }

  @Test
  public void internalRedirect() {
    Response result = subject.internalRedirect("kingdom-come");

    assertEquals(307, result.getStatus());
    assertEquals("/kingdom-come", result.getLocation().getPath());
  }

  @Test
  public void internalRedirectWithCookie() {
    NewCookie cookie = NewCookie.valueOf("Fun=97");
    Response result = subject.internalRedirectWithCookie("kingdom-come", cookie);

    assertEquals("Fun=97;Version=1", result.getCookies().get("Fun").toString());
  }

  @Test
  public void unauthorized() {
    Response result = subject.unauthorized();

    assertEquals(401, result.getStatus());
  }

  @Test
  public void notFound() {
    Response result = subject.notFound("Thing");

    assertEquals(404, result.getStatus());
  }

  @Test
  public void failure() {
    Response result = subject.failure(new CoreException("Fails"));

    assertEquals(400, result.getStatus());
  }

  @Test
  public void failure_andCode() {
    Response result = subject.failure(new CoreException("Fails"), 422);

    assertEquals(422, result.getStatus());
  }

  @Test
  public void failure_andCode_serverFailureUnknownException() {
    Response result = subject.failure(new Exception("Low Level"), 422);

    assertEquals(500, result.getStatus());
  }

  @Test
  public void failureToCreate() {
    Response result = subject.failureToCreate(new CoreException("Fails"));

    assertEquals(422, result.getStatus());
  }

  @Test
  public void failureToUpdate() {
    Response result = subject.failureToUpdate(new CoreException("Fails"));

    assertEquals(422, result.getStatus());
  }

  @Test
  public void notAcceptable() {
    Response result = subject.notAcceptable("at all");

    assertEquals(406, result.getStatus());
  }

  @Test
  public void readOne() {
    Response result = subject.readOne("meme", meme("Red"));

    assertEquals(202, result.getStatus());
  }

  @Test
  public void readMany() {
    Response result = subject.readOne("memes", ImmutableList.of(meme("Red"), meme("Green"), meme("Blue")));

    assertEquals(202, result.getStatus());
    assertEquals("{\"memes\":[{\"sequenceId\":25,\"name\":\"Red\",\"id\":23},{\"sequenceId\":25,\"name\":\"Green\",\"id\":23},{\"sequenceId\":25,\"name\":\"Blue\",\"id\":23}]}", result.getEntity());
  }

  @Test
  public void readMany_emptyCollection() throws Exception {
    Response result = subject.readMany("memes", ImmutableList.of());

    assertEquals(202, result.getStatus());
    assertEquals("{\"memes\":[]}", result.getEntity());
  }

  @Test
  public void create() {
    Response result = subject.create("memes", "meme", meme("Purple"));

    assertEquals(201, result.getStatus());
  }
}
