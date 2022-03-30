// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.ship;

import io.xj.hub.tables.pojos.Account;
import io.xj.hub.tables.pojos.Library;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 https://www.pivotaltracker.com/story/show/165954673 Integration tests use shared scenario fixtures as much as possible
 <p>
 Testing the hypothesis that, while unit tests are all independent,
 integration tests ought be as much about testing all features around a consensus model of the platform
 as they are about testing all resources.
 */
public class ShipIntegrationTestingFixtures {
  private static final Logger log = LoggerFactory.getLogger(ShipIntegrationTestingFixtures.class);

  public Account account1;
  public Library library1;
}
