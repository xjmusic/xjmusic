package io.outright.xj.core.util

import org.junit.Test

// Copyright (c) 2016, Outright Mental Inc. (http://outright.io) All Rights Reserved.
class PurifyTest extends GroovyTestCase {
    void setUp() {
        super.setUp()

    }

    void tearDown() {

    }

    @Test
    void testToString() {
        assert Purify.Slug("jim") == "jim";
        assert Purify.Slug("jim-251") == "jim251";
        assert Purify.Slug("j i m - 2 5 1") == "jim251";
        assert Purify.Slug("j!i\$m%-^2%5*1") == "jim251";
    }

    @Test
    void testProperslug() {
        assert Purify.Properslug("jaMMy") == "Jammy";
        assert Purify.Properslug("j#MMy","neuf") == "Jmmy";
        assert Purify.Properslug("%&(#","neuf") == "Neuf";
        assert Purify.Properslug("%&(#p") == "P";
        assert Purify.Properslug("%&(#") == "";
    }
}
