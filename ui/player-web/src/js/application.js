// Copyright (c) 2018, Outright Mental Inc. (https://outrightmental.com) All Rights Reserved.

import {Player} from "player";

/**
 Application

 */
export class Application {

    /**
     * @type {Player}
     */
    player;

    /**
     * Create new application
     */
    constructor() {
        this.player = new Player();
    }

}
