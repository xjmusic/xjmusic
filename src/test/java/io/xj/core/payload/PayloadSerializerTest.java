package io.xj.core.payload;

import com.google.common.collect.ImmutableList;
import com.google.inject.Injector;
import com.typesafe.config.Config;
import io.xj.core.CoreModule;
import io.xj.core.app.AppConfiguration;
import io.xj.core.exception.CoreException;
import io.xj.core.model.Account;
import io.xj.core.model.AccountUser;
import io.xj.core.model.Chain;
import io.xj.core.model.ChainBinding;
import io.xj.core.model.ChainConfig;
import io.xj.core.model.ChainConfigType;
import io.xj.core.model.ChainState;
import io.xj.core.model.ChainType;
import io.xj.core.model.Library;
import io.xj.core.model.User;
import io.xj.core.testing.AppTestConfiguration;
import io.xj.core.testing.InternalResources;
import io.xj.core.transport.GsonProvider;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.time.Instant;

import static io.xj.core.testing.AssertPayload.assertPayload;

public class PayloadSerializerTest {
  private GsonProvider gsonProvider;

  @Before
  public void setUp() {
    Config config = AppTestConfiguration.getDefault();
    Injector injector = AppConfiguration.inject(config, ImmutableList.of(new CoreModule()));
    gsonProvider = injector.getInstance(GsonProvider.class);
  }

  @Test
  public void serialize() throws IOException {
    Chain chain = Chain.create(Account.create(), "Test Print #1", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), null, "test_print");
    Payload payload = new Payload().setDataEntity(chain);

    String result = gsonProvider.gson().toJson(payload);

    assertPayload(InternalResources.deserializePayload(result))
      .hasDataOne("chains", chain.getId().toString());
  }

  @Test
  public void serializeOne() throws IOException {
    Payload payload = new Payload();
    Instant at = Instant.parse("2019-07-18T21:28:07Z");
    Account account = Account.create("Test Account");
    account
      .setCreatedAtInstant(at)
      .setUpdatedAtInstant(at);
    payload.setDataEntity(account);

    String result = gsonProvider.gson().toJson(payload);

    assertPayload(InternalResources.deserializePayload(result))
      .hasDataOne("accounts", account.getId().toString());
  }

  @Test
  public void serializeOne_withBelongsTo() throws IOException {
    Payload payload = new Payload();
    Instant at = Instant.parse("2019-07-18T21:28:07Z");
    User user = User.create();
    Account account = Account.create();
    AccountUser accountUser = AccountUser.create(account, user);
    accountUser
      .setCreatedAtInstant(at)
      .setUpdatedAtInstant(at);
    payload.setDataEntity(accountUser);

    String result = gsonProvider.gson().toJson(payload);

    assertPayload(InternalResources.deserializePayload(result))
      .hasDataOne("account-users", accountUser.getId().toString())
      .belongsTo(User.class, user.getId().toString())
      .belongsTo(Account.class, account.getId().toString());
  }

  @Test
  public void serializeOne_withHasMany() throws CoreException, IOException {
    Payload payload = new Payload();
    Instant at = Instant.parse("2019-07-18T21:28:07Z");
    Chain chain = Chain.create(Account.create(), "Test Print #1", ChainType.Production, ChainState.Ready, at, null, "test_print");
    ChainConfig chainConfig1 = ChainConfig.create(chain, ChainConfigType.OutputContainer, "AAC");
    ChainConfig chainConfig2 = ChainConfig.create(chain, ChainConfigType.OutputChannels, "4");
    ChainBinding chainBinding1 = ChainBinding.create(chain, Library.create());
    payload.setDataEntity(chain);
    payload.addIncluded(chainConfig1.toPayloadObject());
    payload.addIncluded(chainConfig2.toPayloadObject());
    payload.addIncluded(chainBinding1.toPayloadObject());

    String result = gsonProvider.gson().toJson(payload);

    assertPayload(InternalResources.deserializePayload(result))
      .hasIncluded("chain-configs", ImmutableList.of(chainConfig1, chainConfig2))
      .hasDataOne("chains", chain.getId().toString())
      .hasMany(ChainConfig.class, ImmutableList.of(chainConfig1, chainConfig2))
      .hasMany(ChainBinding.class, ImmutableList.of(chainBinding1));
  }

  @Test
  public void serializeMany() throws IOException {
    Payload payload = new Payload();
    Instant at = Instant.parse("2019-07-18T21:28:07Z");
    Account accountA = Account.create("Test Account A", at);
    Account accountB = Account.create("Test Account B", at);
    Account accountC = Account.create("Test Account C", at);
    payload.setDataEntities(ImmutableList.of(accountA, accountB, accountC));

    String result = gsonProvider.gson().toJson(payload);

    assertPayload(InternalResources.deserializePayload(result))
      .hasDataMany("accounts", ImmutableList.of(
        accountA.getId().toString(),
        accountB.getId().toString(),
        accountC.getId().toString()))
      .hasIncluded("accounts", ImmutableList.of());
  }

}
