// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.core.cache;// Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.

import com.google.common.collect.ImmutableList;
import io.xj.core.access.Access;
import io.xj.core.model.Account;
import io.xj.core.model.Instrument;
import io.xj.core.model.InstrumentState;
import io.xj.core.model.InstrumentType;
import io.xj.core.model.Library;
import io.xj.core.model.Program;
import io.xj.core.model.ProgramState;
import io.xj.core.model.ProgramType;
import io.xj.core.model.User;
import io.xj.core.model.UserAuth;
import io.xj.core.model.UserAuthType;
import io.xj.core.testing.InternalResources;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class CacheKeyTest {
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
    library1 = Library.create(account1, "Apples", InternalResources.now());
    library2 = Library.create(account1, "Bananas", InternalResources.now());
    instrument1a = Instrument.create(user, library1, InstrumentType.Harmonic, InstrumentState.Published, "Mango");
    program1a = Program.create(user, library1, ProgramType.Macro, ProgramState.Published, "Shims", "D", 120, 0.6);
    program1b = Program.create(user, library1, ProgramType.Main, ProgramState.Published, "Mill", "G", 120, 0.6);
  }

  @Test
  public void ofAccess() {
    Access access = Access.create(user, userAuth, ImmutableList.of(account1), "user,engineer");

    String result = CacheKey.of(access);

    assertTrue(result.contains(String.format("User-%s", user.getId())));
    assertTrue(result.contains(String.format("UserAuth-%s", userAuth.getId())));
    assertTrue(result.contains(String.format("Account-%s", account1.getId())));
    assertTrue(result.contains("Role-Engineer"));
    assertTrue(result.contains("Role-User"));
  }

  @Test
  public void ofAccess_okayWithoutRoles() {
    Access access = Access.create(user, userAuth, ImmutableList.of(account1));

    String result = CacheKey.of(access);

    assertTrue(result.contains(String.format("User-%s", user.getId())));
    assertTrue(result.contains(String.format("UserAuth-%s", userAuth.getId())));
    assertTrue(result.contains(String.format("Account-%s", account1.getId())));
  }

  @Test
  public void ofAccess_okayWithoutUserAuth() {
    Access access = Access.create(user, ImmutableList.of(account1));

    String result = CacheKey.of(access);

    assertTrue(result.contains(String.format("User-%s", user.getId())));
    assertTrue(result.contains(String.format("Account-%s", account1.getId())));
  }

  @Test
  public void ofEntities() {
    String result = CacheKey.of(ImmutableList.of(library1, library2, instrument1a, program1a, program1b));

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
    Access access = Access.create(user, userAuth, ImmutableList.of(account1, account2), "user,engineer");

    String result = CacheKey.of(access, ImmutableList.of(library1, library2, instrument1a, program1a, program1b));

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

}
