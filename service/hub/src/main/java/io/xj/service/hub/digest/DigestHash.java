// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.digest;

public interface DigestHash extends Digest {

  /**
   Output entire hash as a SHA-256 hash

   @return string
   */
  String sha256();

  /**
   Output entire hash as a String

   @return string
   */
  String toString();

}
