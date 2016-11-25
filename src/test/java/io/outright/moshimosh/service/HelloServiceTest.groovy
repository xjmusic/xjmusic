package io.outright.moshimosh.service

import org.junit.Test

// Copyright (c) 2016, Outright Mental Inc. (http://outright.io) All Rights Reserved.
class HelloServiceTest extends GroovyTestCase {
    void setUp() {
        super.setUp()

    }

    void tearDown() {

    }

    @Test
    void testHello() {
        assert new HelloService().hello("nuBBub 15%").toString() == "Hello, Nubbub15!";
    }
}
