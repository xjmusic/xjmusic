// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <SDL2/SDL.h>

#include <iostream>

#include "xjmusic/Engine.h"
#include "xjmusic/util/CsvUtils.h"
#include "XJPlayer.h"

/**
 * Main entry point of the application.
 * @param argc  The number of arguments passed to the application.
 * @param argv  The arguments passed to the application.
 * @return    The exit code of the application.
 */
int main(int argc, char *argv[]) {
  // Check if at least one argument was passed
  if (argc <= 1) {
    std::cout << "Must pass the path to an XJ music workstation .xj project as the first argument!" << std::endl;
    return -1;
  }
  std::string pathToProjectFile = argv[1];
  std::cout << "Will open project: " << pathToProjectFile << std::endl;

  try {
    XJPlayer player(pathToProjectFile);
    player.Start();

  } catch (const std::exception &e) {
    std::cerr << "Error: " << e.what() << std::endl;
    return -1;
  }
  return 0;
}

