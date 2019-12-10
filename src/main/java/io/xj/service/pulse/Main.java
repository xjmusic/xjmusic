// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.service.pulse;

public interface Main {
  /**
   Pulse method.

   @param args arguments
   */
  static void main(String[] args) throws Exception {
    Pulse pulse = new Pulse();
    pulse.send();
  }
}
