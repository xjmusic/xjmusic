// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.craft.digest.hash;

import io.xj.craft.digest.Digest;

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
