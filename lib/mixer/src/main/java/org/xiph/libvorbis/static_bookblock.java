// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package org.xiph.libvorbis;

public class static_bookblock {

  static_codebook[][] books;


  public static_bookblock(static_codebook[][] _books) {

    books = new static_codebook[12][3];

    for (int i = 0; i < _books.length; i++)
      System.arraycopy(_books[i], 0, books[i], 0, _books[i].length);
  }

  public static_bookblock(static_bookblock src) {

    this(src.books);
  }
}
