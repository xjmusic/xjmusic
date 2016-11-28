// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.model.instrument

import io.outright.xj.core.model.credit.Credit
import io.outright.xj.core.model.credit.CreditImpl
import io.outright.xj.core.model.user.User
import io.outright.xj.core.model.user.UserImpl

class InstrumentImplTest extends GroovyTestCase {
  User user
  Credit credit
  Instrument instrument

  void setUp() {
    super.setUp()
    user = new UserImpl()
    credit = new CreditImpl(user)
    instrument = new InstrumentImpl(
      Type.PERCUSSIVE,
      "Roland TR-808",
      credit,
      (float) 0.64
    )
  }

  void tearDown() {
    user = null
    credit = null
    instrument = null
  }

  void testType() {
    assert instrument.Type() == Type.PERCUSSIVE
  }

  void testDescription() {
    assert instrument.Description() == "Roland TR-808"
  }

  void testCredit() {
    assert instrument.Credit() == credit
  }

  void testDensity() {
    assert instrument.Density() == (float) 0.64
  }

  void testMemes() {
    assert instrument.Memes().length == 0
  }

  void testAudios() {
    assert instrument.Audios().length == 0
  }
}
