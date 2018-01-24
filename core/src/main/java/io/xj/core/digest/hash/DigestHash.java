// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.digest.hash;

import io.xj.core.digest.Digest;

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
