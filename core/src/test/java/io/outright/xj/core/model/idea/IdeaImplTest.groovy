// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.model.idea

import io.outright.xj.core.model.credit.Credit
import io.outright.xj.core.model.credit.CreditImpl
import io.outright.xj.core.model.user.User
import io.outright.xj.core.model.user.UserImpl

class IdeaImplTest extends GroovyTestCase {
  User user
  Credit credit
  Idea idea

  void setUp() {
    super.setUp()
    user = new UserImpl()
    credit = new CreditImpl(user)
    idea = new IdeaImpl(
      "Big Idea",
      credit,
      Type.MAIN,
      (float) 0.854,
      "C Major",
      (float) 125,
    )
  }

  void tearDown() {
    idea = null
  }

  void testName() {
    assert idea.Name() == "Big Idea"
  }

  void testCredit() {
    assert idea.Credit() == credit
  }

  void testType() {
    assert idea.Type() == Type.MAIN
  }

  void testDensity() {
    assert idea.Density() == (float) 0.854
  }

  void testKey() {
    assert idea.Key() == "C Major"
  }

  void testTempo() {
    assert idea.Tempo() == (float) 125
  }

  void testMemes() {
    assert idea.Memes().length == 0
  }

  void testPhases() {
    assert idea.Phases().length == 0
  }
}
