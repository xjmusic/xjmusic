// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.model.choice

import io.outright.xj.core.model.arrangement.Arrangement
import io.outright.xj.core.model.arrangement.ArrangementImpl
import io.outright.xj.core.model.credit.Credit
import io.outright.xj.core.model.credit.CreditImpl
import io.outright.xj.core.model.idea.Idea
import io.outright.xj.core.model.idea.IdeaImpl
import io.outright.xj.core.model.phase.Phase
import io.outright.xj.core.model.phase.PhaseImpl
import io.outright.xj.core.model.user.User
import io.outright.xj.core.model.user.UserImpl

class ChoiceImplTest extends GroovyTestCase {
  Arrangement arrangement
  User user
  Credit credit
  Idea idea
  Phase phase
  Choice choice

  void setUp() {
    super.setUp()
    arrangement = new ArrangementImpl()
    user = new UserImpl()
    credit = new CreditImpl(user)
    idea = new IdeaImpl(
      "Big Idea",
      credit,
      io.outright.xj.core.model.idea.Type.MAIN,
      (float) 0.854,
      "C Major",
      (float) 125,
    )
    phase = new PhaseImpl(
      "Phase Q",
      2,
      (float) 8,
      (float) 0.43,
      "Dm",
      (float) 86
    )
    choice = new ChoiceImpl(
      Type.PRIMARY,
      idea,
      arrangement,
      2,
      -5
    )
  }

  void tearDown() {
    arrangement = null
    user = null
    credit = null
    idea = null
    phase = null
  }

  void testType() {
    assert choice.Type() == Type.PRIMARY
  }

  void testIdea() {
    assert choice.Idea() == idea
  }

  void testArrangement() {
    assert choice.Arrangement() == arrangement
  }

  void testPhaseOffset() {
    assert choice.PhaseOffset() == 2
  }

  void testTranspose() {
    assert choice.Transpose() == -5
  }
}
