package io.xj.core.payload;

import com.google.common.collect.ImmutableList;
import io.xj.core.CoreTest;
import io.xj.core.model.Account;
import io.xj.core.model.ChainConfig;
import io.xj.core.model.User;
import org.junit.Test;

import java.io.IOException;
import java.util.UUID;

import static io.xj.core.testing.AssertPayload.assertPayload;

public class PayloadDeserializerTest extends CoreTest {

  @Test
  public void deserializeOneIncludingEmbeddedEntities() throws IOException {
    Payload result = deserializePayload(readResourceFile("model/payload/deserializeOneIncludingEmbeddedEntities.json"));

    assertPayload(result)
      .hasDataOne("chains", "12")
      .belongsTo(Account.class, "1")
      .hasMany(ChainConfig.class, ImmutableList.of(new ChainConfig().setId(UUID.fromString("9b862c2f-192f-4041-b849-442a2ec50218"))));
  }

  @Test
  public void deserializeOneWithRelationship() throws IOException {
    Payload result = deserializePayload(readResourceFile("model/payload/deserializeOneWithRelationship.json"));

    assertPayload(result)
      .hasDataOne("account-users", "14")
      .belongsTo(Account.class, "1")
      .belongsTo(User.class, "5");
  }

  @Test
  public void deserializeOne() throws IOException {
    Payload result = deserializePayload(readResourceFile("model/payload/deserializeOne.json"));

    assertPayload(result)
      .hasDataOne("accounts", "67");
  }

  @Test
  public void deserializeErrors() throws IOException {
    Payload result = deserializePayload(readResourceFile("model/payload/deserializeErrors.json"));

    assertPayload(result)
      .hasErrorCount(1);
  }

  @Test
  public void deserializeOneWithNullAttributeValue() throws IOException {
    Payload result = deserializePayload(readResourceFile("model/payload/deserializeOneWithNullAttributeValue.json"));

    assertPayload(result)
      .hasDataOne("chains", "17");
  }

  @Test
  public void deserializeMany() throws IOException {
    Payload result = deserializePayload(readResourceFile("model/payload/deserializeMany.json"));

    assertPayload(result)
      .hasDataMany("accounts", ImmutableList.of("5"));
  }

  @Test
  public void deserializeMany_emptyTypeHasMany() throws IOException {
    String json = "{\"data\":[]}";

    Payload result = deserializePayload(json);

    assertPayload(result)
      .hasDataManyEmpty();
  }

  @Test
  public void deserializeOne_nullDataSetsTypeToHasOne() throws IOException {
    String json = "{\"data\":null}";

    Payload result = deserializePayload(json);

    assertPayload(result)
      .hasDataOneEmpty();
  }
}
