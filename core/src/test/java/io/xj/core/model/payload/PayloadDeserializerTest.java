package io.xj.core.model.payload;

import com.google.common.collect.ImmutableList;
import io.xj.core.CoreTest;
import io.xj.core.model.account.Account;
import io.xj.core.model.chain.sub.ChainConfig;
import io.xj.core.model.user.User;
import org.junit.Test;

import java.io.IOException;
import java.util.UUID;

import static io.xj.core.testing.AssertPayload.assertPayload;

public class PayloadDeserializerTest extends CoreTest {

  @Test
  public void payload_deserializeOneIncludingEmbeddedEntities() throws IOException {
    String json = "{\"data\":{\"id\":\"12\",\"attributes\":{\"name\":\"Cool Ambience\",\"state\":\"Draft\",\"type\":\"Production\",\"startAt\":\"2019-07-23T01:04:12.194172Z\",\"stopAt\":null,\"embedKey\":\"coolambience\"},\"relationships\":{\"account\":{\"data\":{\"type\":\"accounts\",\"id\":\"1\"}},\"chainConfigs\":{\"data\":[{\"type\":\"chain-configs\",\"id\":\"9b862c2f-192f-4041-b849-442a2ec50218\"}]}},\"type\":\"chains\"},\"included\":[{\"attributes\":{\"type\":\"OutputContainer\",\"value\":\"AAC\"},\"relationships\":{\"chain\":{\"data\":{\"type\":\"chains\",\"id\":\"12\"}}},\"type\":\"chain-configs\"}]}";

    Payload result = deserializePayload(json);

    assertPayload(result)
      .hasDataOne("chains", "12")
      .belongsTo(Account.class, "1")
      .hasMany(ChainConfig.class, ImmutableList.of(new ChainConfig().setId(UUID.fromString("9b862c2f-192f-4041-b849-442a2ec50218"))));
  }

  @Test
  public void payload_deserializeOneWithRelationship() throws IOException {
    String json = "{\"data\":{\"relationships\":{\"account\":{\"data\":{\"type\":\"accounts\",\"id\":\"1\"}},\"user\":{\"data\":{\"type\":\"users\",\"id\":\"5\"}}},\"type\":\"account-users\",\"id\":\"14\"}}";

    Payload result = deserializePayload(json);

    assertPayload(result)
      .hasDataOne("account-users", "14")
      .belongsTo(Account.class, "1")
      .belongsTo(User.class, "5");
  }

  @Test
  public void payload_deserializeOne() throws IOException {
    String json = "{\"data\":{\"attributes\":{\"name\":\"Test Account\"},\"type\":\"accounts\",\"id\":\"67\"}}";

    Payload result = deserializePayload(json);

    assertPayload(result)
      .hasDataOne("accounts", "67");
  }

  @Test
  public void payload_deserialize_errors() throws IOException {
    String json = "{\"data\":{\"id\":\"17\",\"type\":\"chains\",\"relationships\":{\"chainBindings\":{\"data\":[]},\"chainConfigs\":{\"data\":[]},\"account\":{\"data\":{\"id\":\"25\",\"type\":\"accounts\"}}},\"attributes\":{\"embedKey\":\"test_print\",\"stopAt\":null,\"createdAt\":\"2014-08-12T12:17:02.527142Z\",\"name\":\"Test Print #1\",\"state\":\"Fabricate\",\"type\":\"Production\",\"startAt\":\"2014-08-12T12:17:02.527142Z\",\"updatedAt\":\"2014-08-12T12:17:02.527142Z\"}},\"errors\":[{\"links\":{},\"code\":\"CoreException\",\"title\":\"Chain OutputChannels requires numeric value!\",\"detail\":null,\"id\":null}]}";

    Payload result = deserializePayload(json);

    assertPayload(result)
      .hasErrorCount(1);
  }

  @Test
  public void payload_deserializeOne_withNullAttributeValue() throws IOException {
    String json = "{\"data\":{\"id\":\"17\",\"type\":\"chains\",\"relationships\":{\"account\":{\"data\":{\"id\":\"25\",\"type\":\"accounts\"}}},\"attributes\":{\"stopAt\":null,\"type\":\"Production\",\"embedKey\":\"test_print\",\"createdAt\":\"2014-08-12T12:17:02.527142Z\",\"name\":\"Test Print #1\",\"state\":\"Fabricate\",\"startAt\":\"2014-08-12T12:17:02.527142Z\",\"updatedAt\":\"2014-08-12T12:17:02.527142Z\"}}}";

    Payload result = deserializePayload(json);

    assertPayload(result)
      .hasDataOne("chains", "17");
  }

  @Test
  public void payload_deserializeMany() throws IOException {
    String json = "{\"data\":[{\"attributes\":{\"name\":\"Test Account\"},\"type\":\"accounts\",\"id\":\"5\"}]}";

    Payload result = deserializePayload(json);

    assertPayload(result)
      .hasDataMany("accounts", ImmutableList.of("5"));
  }

  @Test
  public void payload_deserializeMany_emptyTypeHasMany() throws IOException {
    String json = "{\"data\":[]}";

    Payload result = deserializePayload(json);

    assertPayload(result)
      .hasDataManyEmpty();
  }

  @Test
  public void payload_deserializeOne_nullDataSetsTypeToHasOne() throws IOException {
    String json = "{\"data\":null}";

    Payload result = deserializePayload(json);

    assertPayload(result)
      .hasDataOneEmpty();
  }
}
