// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.lib.lock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.security.SecureRandom;

@Service
public class LockGeneratorImpl implements LockGenerator {

  @Autowired
  public LockGeneratorImpl() {
  }

  @Override
  public String get() {
    SecureRandom random = new SecureRandom();
    byte[] randomBytes = new byte[128];
    random.nextBytes(randomBytes);
    BigInteger bi = new BigInteger(1, randomBytes);
    return bi.toString(16);
  }
}
