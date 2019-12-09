package io.xj.core.payload;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import io.xj.core.model.Account;
import io.xj.core.model.User;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;
import java.util.UUID;

import static io.xj.core.testing.Assert.assertSameItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PayloadObjectTest  {
  PayloadObject subject;

  @Before
  public void setUp() throws Exception {
    subject = new PayloadObject();
  }

  @Test
  public void add() {
    Account account1= Account.create("Test Account");
    subject.add("account", new Payload().setDataEntity(account1));

    assertTrue(subject.getRelationships().get("account").getDataOne().isPresent());
    assertEquals(account1.getId().toString(), subject.getRelationships().get("account").getDataOne().get().getId());
  }

  @Test
  public void getAttribute_setAttribute() {
    subject
      .setAttribute("swanky", 23)
      .setAttribute("times", 52);

    assertTrue(subject.getAttribute("swanky").isPresent());
    assertEquals(23, subject.getAttribute("swanky").get());
  }

  @Test
  public void getAttributes_setAttributes() {
    Map<String, Object> attr = ImmutableMap.of(
      "kittens", "cute",
      "puppies", 5
    );
    subject.setAttributes(attr);

    assertSameItems(attr, subject.getAttributes());
  }

  @Test
  public void getId_setId() {
    subject.setId("15");

    assertEquals("15", subject.getId());
  }

  @Test
  public void getLinks_setLinks() {
    Map<String, String> links = ImmutableMap.of(
      "kittens", "https://kittens.com/",
      "puppies", "https://puppies.com/"
    );
    subject.setLinks(links);

    assertSameItems(links, subject.getLinks());
  }

  @Test
  public void getRelationshipObject() {
    Account account1 = Account.create("Test Account");
    subject.add("account", new Payload().setDataEntity(account1));

    assertTrue(subject.getRelationshipDataOne("account").isPresent());
    assertEquals(account1.getId().toString(), subject.getRelationshipDataOne("account").get().getId());
  }

  @Test
  public void getRelationships_setRelationships() {
    subject.setRelationships(ImmutableMap.of(
      "account", new Payload().setDataEntity(Account.create("Test Account")),
      "user", new Payload().setDataEntity(User.create("Test User", "user@email.com", "http://images.com/avatar.jpg"))
    ));

    assertEquals(2, subject.getRelationships().size());
  }

  @Test
  public void getType_setType() {
    subject.setType("account-users");

    assertEquals("account-users", subject.getType());
  }

  @Test
  public void setAttributes_nullValueSetsNullValueInstance() {
    Map<String, Object> attr = Maps.newHashMap();
    attr.put("kittens", "cute");
    attr.put("puppies", null);
    subject.setAttributes(attr);

    assertSameItems(attr, subject.getAttributes());
  }

  @Test
  public void isSame() {
    UUID id = UUID.randomUUID();
    subject.setId(id.toString());
    subject.setType("accounts");

    assertTrue(subject.isSame(new Account().setId(id)));
    assertFalse(subject.isSame(new Account().setId(UUID.randomUUID())));
    assertFalse(subject.isSame(new User().setId(id)));
  }

}
