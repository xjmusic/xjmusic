// Copyright (c) 2018, Outright Mental Inc. (https://outrightmental.com) All Rights Reserved.

import {Coordinates} from "coordinates";

/**
 * Game master system
 */
export class Game {
  name = "California!™";

  // Death Valley Junction, CA
  beginAt = new Coordinates(36.304453, -116.415362);

  /**
   * Instantiate a new Game
   */
  constructor() {
    console.info(this.name + " begins now.");
  }

}
