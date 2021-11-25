// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.util;

import com.google.api.client.util.Lists;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import static io.xj.lib.util.Text.formatMultiline;

public enum Command {
  ;

  /**
   Execute the given command

   @param cmdParts command parts to join (space-separated) and execute
   @throws IOException          on failure
   @throws InterruptedException on failure
   */
  public static void execute(String descriptiveInfinitive, List<String> cmdParts) throws IOException, InterruptedException {
    String cmd = String.join(" ", cmdParts);
    var proc = Runtime.getRuntime().exec(cmd);
    String line;
    List<String> output = Lists.newArrayList();
    BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
    while ((line = stdError.readLine()) != null) output.add(line);
    if (0 != proc.waitFor()) {
      throw new IOException(String.format("Failed %s: %s\n\n%s", descriptiveInfinitive, cmd, formatMultiline(output.toArray())));
    }
  }
}
