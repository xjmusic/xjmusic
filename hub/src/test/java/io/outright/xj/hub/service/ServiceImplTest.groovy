package io.outright.xj.hub.service

import org.junit.Test

// Copyright (c) 2016, Outright Mental Inc. (http://outright.io) All Rights Reserved.
class ServiceImplTest extends GroovyTestCase {
    void setUp() {
        super.setUp()

    }

    void tearDown() {

    }

    @Test
    void testHello() {
        assert new ServiceImpl().get("/").toString() == "{}";
      assert new ServiceImpl().get("/engines").toString() == "{engines:true}";
    }
}
