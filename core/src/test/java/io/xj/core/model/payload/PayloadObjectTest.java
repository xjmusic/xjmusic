package io.xj.core.model.payload;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import io.xj.core.CoreTest;
import io.xj.core.model.account.Account;
import io.xj.core.model.user.User;
import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;
import java.util.Map;

import static io.xj.core.testing.Assert.assertSameItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PayloadObjectTest extends CoreTest {
  PayloadObject subject;

  @Before
  public void setUp() throws Exception {
    subject = new PayloadObject();
  }

  @Test
  public void add() {
    subject.add("account", new Payload().setDataEntity(newAccount(17, "Test Account")));

    assertTrue(subject.getRelationships().get("account").getDataOne().isPresent());
    assertEquals("17", subject.getRelationships().get("account").getDataOne().get().getId());
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
    subject.add("account", new Payload().setDataEntity(newAccount(17, "Test Account")));

    assertTrue(subject.getRelationshipDataOne("account").isPresent());
    assertEquals("17", subject.getRelationshipDataOne("account").get().getId());
  }

  @Test
  public void getRelationships_setRelationships() {
    subject.setRelationships(ImmutableMap.of(
      "account", new Payload().setDataEntity(newAccount(17, "Test Account")),
      "user", new Payload().setDataEntity(newUser(25, "Test User", "user@email.com", "http://images.com/avatar.jpg"))
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
    subject.setId("57");
    subject.setType("accounts");

    assertTrue(subject.isSame(new Account().setId(BigInteger.valueOf(57))));
    assertFalse(subject.isSame(new Account().setId(BigInteger.valueOf(52))));
    assertFalse(subject.isSame(new User().setId(BigInteger.valueOf(57))));
  }

}
