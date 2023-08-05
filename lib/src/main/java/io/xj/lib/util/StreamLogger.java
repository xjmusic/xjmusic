// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.lib.util;

import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class StreamLogger implements Runnable {
  static final String THREAD_NAME = "ffmpeg";
  final Logger LOG;
  final InputStream _inputStream;
  final String name;

  public StreamLogger(Logger LOG, InputStream is, String name) {
    this.LOG = LOG;
    _inputStream = is;
    this.name = name;
  }

  public static void close(Reader reader) {
    try {
      if (reader != null) {
        reader.close();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void run() {
    InputStreamReader isr = null;
    BufferedReader br = null;
    final Thread currentThread = Thread.currentThread();
    final String oldName = currentThread.getName();
    try {
      currentThread.setName(THREAD_NAME);
      isr = new InputStreamReader(_inputStream);
      br = new BufferedReader(isr);
      String line;
      while ((line = br.readLine()) != null) {
        LOG.debug("[{}] {}", name, line);
      }
    } catch (IOException e) {
      LOG.error("[{}}] Failed to read from stream!", name, e);
    } finally {
      LOG.debug("[{}}] Done reading stream", name);
      close(isr);
      close(br);
      currentThread.setName(oldName);
    }
  }
}
