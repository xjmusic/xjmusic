// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface Purify {
    Pattern nonAlphabet = Pattern.compile("[^a-zA-Z]");

    static String Slug(String raw) {
        Matcher m = nonAlphabet.matcher(raw);
        return m.replaceAll("");
    }

    static String Slug(String raw, String defaultValue) {
        String slug = Slug(raw);
        return slug.length() > 0 ? slug : defaultValue;
    }

    static String ProperSlug(String raw) {
        return toProper(Slug(raw));
    }

    static String ProperSlug(String raw, String defaultValue) {
        return toProper(Slug(raw, defaultValue));
    }

    static String toProper(String from) {
        if (from.length() > 1) {
            String lower = from.toLowerCase();
            return lower.substring(0, 1).toUpperCase() + lower.substring(1);
        } else if (from.length() > 0) {
            return from.toUpperCase();
        } else {
            return "";
        }
    }
}
