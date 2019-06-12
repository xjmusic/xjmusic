package io.xj.core.model.payload;

import com.google.common.collect.ImmutableList;
import io.xj.core.CoreTest;
import io.xj.core.exception.CoreException;
import io.xj.core.model.account.Account;
import io.xj.core.model.account.AccountUser;
import io.xj.core.model.chain.Chain;
import io.xj.core.model.chain.ChainState;
import io.xj.core.model.chain.ChainType;
import io.xj.core.model.chain.sub.ChainBinding;
import io.xj.core.model.chain.sub.ChainConfig;
import io.xj.core.model.chain.ChainConfigType;
import io.xj.core.model.user.User;
import org.junit.Test;

import java.io.IOException;
import java.math.BigInteger;
import java.time.Instant;

import static io.xj.core.testing.AssertPayload.assertPayload;

public class PayloadSerializerTest extends CoreTest {

  @Test
  public void serialize() throws IOException {
    Payload payload = new Payload().setDataEntity(newChain(17, 25, "Test Print #1", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), null, "test_print", Instant.parse("2014-08-12T12:17:02.527142Z")));

    String result = gsonProvider.gson().toJson(payload);

    assertPayload(deserializePayload(result))
      .hasDataOne("chains", "17");
  }

  @Test
  public void serializeOne() throws IOException {
    Payload payload = new Payload();
    Instant at = Instant.parse("2019-07-18T21:28:07Z");
    Account account = newAccount(12, "Test Account");
    account
      .setCreatedAtInstant(at)
      .setUpdatedAtInstant(at);
    payload.setDataEntity(account);

    String result = gsonProvider.gson().toJson(payload);

    assertPayload(deserializePayload(result))
      .hasDataOne("accounts", "12");
  }

  @Test
  public void serializeOne_withBelongsTo() throws IOException {
    Payload payload = new Payload();
    Instant at = Instant.parse("2019-07-18T21:28:07Z");
    AccountUser accountUser = newAccountUser(12, 17);
    accountUser
      .setId(BigInteger.valueOf(674))
      .setCreatedAtInstant(at)
      .setUpdatedAtInstant(at);
    payload.setDataEntity(accountUser);

    String result = gsonProvider.gson().toJson(payload);

    assertPayload(deserializePayload(result))
      .hasDataOne("account-users", "674")
      .belongsTo(User.class, "17")
      .belongsTo(Account.class, "12");
  }

  @Test
  public void serializeOne_withHasMany() throws CoreException, IOException {
    Payload payload = new Payload();
    Instant at = Instant.parse("2019-07-18T21:28:07Z");
    ChainConfig chainConfig1 = newChainConfig(ChainConfigType.OutputContainer, "AAC");
    ChainConfig chainConfig2 = newChainConfig(ChainConfigType.OutputChannels, "4");
    ChainBinding chainBinding1 = newChainBinding("Library", 5);
    Chain chain = newChain(17, 25, "Test Print #1", ChainType.Production, ChainState.Ready, at, null, "test_print", at);
    chain.add(chainConfig1);
    chain.add(chainConfig2);
    chain.add(chainBinding1);
    payload.setDataEntity(chain);

    String result = gsonProvider.gson().toJson(payload);

    assertPayload(deserializePayload(result))
      .hasIncluded("chain-configs", ImmutableList.of(chainConfig1, chainConfig2))
      .hasDataOne("chains", "17")
      .hasMany(ChainConfig.class, ImmutableList.of(chainConfig1, chainConfig2))
      .hasMany(ChainBinding.class, ImmutableList.of(chainBinding1));
  }

  @Test
  public void serializeMany() throws IOException {
    Payload payload = new Payload();
    Instant at = Instant.parse("2019-07-18T21:28:07Z");
    payload.setDataEntities(ImmutableList.of(
      newAccount(12, "Test Account A", at),
      newAccount(14, "Test Account B", at),
      newAccount(17, "Test Account C", at)
    ), false);

    String result = gsonProvider.gson().toJson(payload);

    assertPayload(deserializePayload(result))
      .hasDataMany("accounts", ImmutableList.of("12", "14", "17"))
      .hasIncluded("accounts", ImmutableList.of());
  }

}
