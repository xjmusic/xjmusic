// Copyright (c) 2018, Outright Mental Inc. (https://outrightmental.com) All Rights Reserved.

import {Coordinates} from "coordinates";

// Styles
const textCoordinatesStyle = {fontFamily: "Courier, monospace", fontSize: "1vw", fill: 'black'};

/**
 * [#155142244] Player wants viewport with GPS coordinates and a map of California displayed in background, ability to scroll/zoom, to be inside the game, and plan a strategy.
 */
export class Board {
  pixi;

  /**
   * @type {Game} in play
   */
  game;

  /**
   * @type {Coordinates} Current GPS Coordinates
   */
  nowAt;

  /**
   * @type {Coordinates} Southwest (minimum) Map Boundary
   */
  boundarySW = new Coordinates(32.555914, -124.685902);

  /**
   * @type {Coordinates} Northeast (maximum) Map Boundary
   */
  boundaryNE = new Coordinates(42.168888, -113.999952);

  /**
   * @type {number} Renderer Width
   */
  width;

  /**
   * @type {number} Renderer Height
   */
  height;

  /**
   * Instantiate Viewport
   *
   * @param {Game} game to play
   */
  constructor(game) {
    this.game = game;
    this.initGraphicsFramework();
    this.initKeyboardNavigation();

    // Text of current GPS Coordinates
    this.textCoordinates = new PIXI.Text("", textCoordinatesStyle);
    this.pixi.stage.addChild(this.textCoordinates);

    // Go to the starting coordinates
    this.goTo(this.game.beginAt)
  }

  /**
   * Initialize the Graphics Framework (Pixi.JS)
   */
  initGraphicsFramework() {
    this.pixi = new PIXI.Application(computeWindowWidth(), computeWindowHeight(), {
      transparent: true
    });
    document.body.appendChild(this.pixi.view);

    let board = this;
    this.pixi.ticker.add(function () {
      board.update();
    });
    window.addEventListener("resize", function () {
      board.pixi.renderer.resize(computeWindowWidth(), computeWindowHeight());
    });
  }

  /**
   * Initialize the keyboard navigation
   */
  initKeyboardNavigation() {
    let board = this;
    document.addEventListener('keydown', function (key) {
      board.onKeyDown(key);
    });
  }

  /**
   * Handle a key press
   * @param {{keyCode:number}} key that was pressed
   */
  onKeyDown(key) {
    if (key.keyCode === KeyCode.W || key.keyCode === KeyCode.Up) {
      this.goDelta(new Coordinates(1, 0));

    } else if (key.keyCode === KeyCode.S || key.keyCode === KeyCode.Down) {
      this.goDelta(new Coordinates(-1, 0));

    } else if (key.keyCode === KeyCode.A || key.keyCode === KeyCode.Left) {
      this.goDelta(new Coordinates(0, -1));

    } else if (key.keyCode === KeyCode.D || key.keyCode === KeyCode.Right) {
      this.goDelta(new Coordinates(0, 1));
    }
  }

  /**
   * Update the viewport (e.g. each Pixi ticker).
   * Determine if width or height of renderer has changed.
   */
  update() {
    if (this.pixi.renderer.width !== this.width || this.pixi.renderer.height !== this.height) {
      this.width = this.pixi.renderer.width;
      this.height = this.pixi.renderer.height;
      this.updateSize();
    }
  }

  /**
   * Size the board to the viewport (e.g. after a renderer width/height change)
   */
  updateSize() {

    // Text of current GPS Coordinates
    this.textCoordinates.x = this.width / 2 - this.textCoordinates.width / 2;
    this.textCoordinates.y = this.height - this.textCoordinates.height * 2;
  }

  /**
   * Go to a particular longitude and latitude
   * @param {Coordinates} coordinates to go to
   */
  goTo(coordinates) {
    // TODO: go to coordinates
    console.info("Viewport to", coordinates.toString());
    this.nowAt = coordinates;

    this.textCoordinates.setText(coordinates.toString());
  }

  /**
   * Go a particular longitude and latitude delta
   * @param {Coordinates} delta to go
   */
  goDelta(delta) {
    this.goTo(new Coordinates(
      Math.max(this.boundarySW.lat, Math.min(this.boundaryNE.lat, this.nowAt.lat + delta.lat)),
      Math.max(this.boundarySW.lon, Math.min(this.boundaryNE.lon, this.nowAt.lon + delta.lon))
    ));
  }

}

/**
 Compute window width
 @returns {number}
 */
function computeWindowWidth() {
  return window.innerWidth - 20;
}

/**
 Compute window height
 @returns {number}
 */
function computeWindowHeight() {
  return window.innerHeight - 20;
}

/**
 * Key codes
 * @type {object}
 */
const KeyCode = {
  W: 87,
  D: 68,
  S: 83,
  A: 65,
  Up: 38,
  Down: 40,
  Left: 37,
  Right: 39,
};
