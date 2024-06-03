// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.model.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static io.xj.model.util.StringUtils.formatMultiline;

public enum Command {
  ;
  static final Logger LOG = LoggerFactory.getLogger(Command.class);

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
    List<String> outputLines = new ArrayList<>();
    BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
    while ((line = stdError.readLine()) != null) outputLines.add(line);
    var output = formatMultiline(outputLines.toArray());
    if (0 != proc.waitFor()) {
      throw new IOException(String.format("Failed %s: %s\n\n%s", descriptiveInfinitive, cmd, output));
    }
    LOG.debug("\n\n\nEXECUTE\n\n{}\n\n\nOUTPUT\n\n{}\n\n\n", cmd, output);
  }
}
