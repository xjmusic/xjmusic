// Copyright (c) 2018, Outright Mental Inc. (https://outrightmental.com) All Rights Reserved.

/**
 * GPS Coordinates
 */
export class Coordinates {

  /**
   * @type {number}
   */
  lat;

  /**
   * @type {number}
   */
  lon;

  /**
   * Instantiate GPS Coordinates with a Latitude and Longitude
   *
   * @param {number} latitude of new GPS coordinates
   * @param {number} longitude of new GPS coordinates
   */
  constructor(latitude, longitude) {
    this.lat = latitude;
    this.lon = longitude;
  }

  /**
   * Coordinates to a readable string, e.g. "32.555914, -113.999952"
   */
  toString() {
    return this.lat + ', ' + this.lon;
  }

}
