// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.service.hub.ingest;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import io.xj.service.hub.access.HubAccess;
import io.xj.service.hub.entity.*;
import org.junit.Before;
import org.junit.Test;

import java.time.Instant;

import static org.junit.Assert.assertTrue;

public class HubHubIngestCacheKeyTest {
  private Account account1;
  private User user;
  private Library library1;
  private Library library2;
  private Instrument instrument1a;
  private Program program1a;
  private Program program1b;
  private UserAuth userAuth;

  @Before
  public void setUp() {
    account1 = Account.create();
    user = User.create();
    userAuth = UserAuth.create(user, UserAuthType.Google);
    library1 = Library.create(account1, "Apples", Instant.now());
    library2 = Library.create(account1, "Bananas", Instant.now());
    instrument1a = Instrument.create(user, library1, InstrumentType.Harmonic, InstrumentState.Published, "Mango");
    program1a = Program.create(user, library1, ProgramType.Macro, ProgramState.Published, "Shims", "D", 120, 0.6);
    program1b = Program.create(user, library1, ProgramType.Main, ProgramState.Published, "Mill", "G", 120, 0.6);
  }

  @Test
  public void ofAccess() {
    HubAccess hubAccess = HubAccess.create(user, userAuth, ImmutableList.of(account1), "user,engineer");

    String result = HubIngestCacheKey.of(hubAccess);

    assertTrue(result.contains(String.format("User-%s", user.getId())));
    assertTrue(result.contains(String.format("UserAuth-%s", userAuth.getId())));
    assertTrue(result.contains(String.format("Account-%s", account1.getId())));
    assertTrue(result.contains("Role-Engineer"));
    assertTrue(result.contains("Role-User"));
  }

  @Test
  public void ofAccess_okayWithoutRoles() {
    HubAccess hubAccess = HubAccess.create(user, userAuth, ImmutableList.of(account1));

    String result = HubIngestCacheKey.of(hubAccess);

    assertTrue(result.contains(String.format("User-%s", user.getId())));
    assertTrue(result.contains(String.format("UserAuth-%s", userAuth.getId())));
    assertTrue(result.contains(String.format("Account-%s", account1.getId())));
  }

  @Test
  public void ofAccess_okayWithoutUserAuth() {
    HubAccess hubAccess = HubAccess.create(user, ImmutableList.of(account1));

    String result = HubIngestCacheKey.of(hubAccess);

    assertTrue(result.contains(String.format("User-%s", user.getId())));
    assertTrue(result.contains(String.format("Account-%s", account1.getId())));
  }

  @Test
  public void ofEntities() {
    String result = HubIngestCacheKey.of(ImmutableList.of(library1, library2, instrument1a, program1a, program1b));

    assertTrue(result.contains(String.format("Instrument-%s", instrument1a.getId())));
    assertTrue(result.contains(String.format("Library-%s", library1.getId())));
    assertTrue(result.contains(String.format("Library-%s", library2.getId())));
    assertTrue(result.contains(String.format("Program-%s", program1a.getId())));
    assertTrue(result.contains(String.format("Program-%s", program1a.getId())));
  }

  @Test
  public void ofAccessEntities() {
    Account account2 = Account.create();
    UserAuth userAuth = UserAuth.create();
    HubAccess hubAccess = HubAccess.create(user, userAuth, ImmutableList.of(account1, account2), "user,engineer");

    String result = HubIngestCacheKey.of(hubAccess, ImmutableList.of(library1, library2, instrument1a, program1a, program1b));

    assertTrue(result.contains("Role-Engineer"));
    assertTrue(result.contains("Role-User"));
    assertTrue(result.contains(String.format("Account-%s", account1.getId())));
    assertTrue(result.contains(String.format("Account-%s", account2.getId())));
    assertTrue(result.contains(String.format("User-%s", user.getId())));
    assertTrue(result.contains(String.format("UserAuth-%s", userAuth.getId())));
    assertTrue(result.contains(String.format("Instrument-%s", instrument1a.getId())));
    assertTrue(result.contains(String.format("Library-%s", library1.getId())));
    assertTrue(result.contains(String.format("Library-%s", library2.getId())));
    assertTrue(result.contains(String.format("Program-%s", program1a.getId())));
    assertTrue(result.contains(String.format("Program-%s", program1a.getId())));
  }

  @Test
  public void ofSpecifiedEntities() {
    Library library1 = Library.create();
    Library library2 = Library.create();
    Program program1 = Program.create();
    Program program2 = Program.create();
    Instrument instrument1 = Instrument.create();
    Instrument instrument2 = Instrument.create();

    String result = HubIngestCacheKey.of(
      ImmutableSet.of(library1.getId(), library2.getId()),
      ImmutableSet.of(program1.getId(), program2.getId()),
      ImmutableSet.of(instrument1.getId(), instrument2.getId()));

    assertTrue(result.contains(String.format("Library-%s", library1.getId())));
    assertTrue(result.contains(String.format("Library-%s", library2.getId())));
    assertTrue(result.contains(String.format("Program-%s", program1.getId())));
    assertTrue(result.contains(String.format("Program-%s", program2.getId())));
    assertTrue(result.contains(String.format("Instrument-%s", instrument1.getId())));
    assertTrue(result.contains(String.format("Instrument-%s", instrument2.getId())));
  }
}
