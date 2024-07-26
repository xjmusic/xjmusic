// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <iostream>

#include "XJPlayer.h"
#include "xjmusic/util/CsvUtils.h"

/**
 * Show the usage of the application.
 * @param name  The name of the application.
 */
void showUsage(const std::string &name) {
  std::cout << "Usage: " << name << " <pathToProjectFile> [-template <name>] [-controlMode <mode>] [-craftAheadSeconds <seconds>] [-dubAheadSeconds <seconds>] [-persistenceWindowSeconds <seconds>]" << std::endl;
}

/**
 * Main entry point of the application.
 * @param argc  The number of arguments passed to the application.
 * @param argv  The arguments passed to the application.
 * @return    The exit code of the application.
 */
int main(const int argc, char *argv[]) {
  // Check if at least one argument was passed
  if (argc <= 1) {
    std::cout << "Must pass the path to an XJ music workstation .xj project as the first argument!" << std::endl;
    showUsage(argv[0]);
    return -1;
  }
  std::string pathToProjectFile = argv[1];
  std::cout << "Will open project: " << pathToProjectFile << std::endl;

  // User can specify the control mode, craft ahead seconds, dub ahead seconds, and persistence window seconds
  Fabricator::ControlMode controlMode = Fabricator::ControlMode::Auto;
  std::optional<int> craftAheadSeconds;
  std::optional<int> dubAheadSeconds;
  std::optional<int> persistenceWindowSeconds;
  std::optional<std::string> templateName;

  // Parse all remaining arguments to set the control mode, craft ahead seconds, dub ahead seconds, and persistence window seconds
  for (int i = 2; i < argc; i++) {
    std::string arg = argv[i];
    if (arg == "-template") {
      if (i + 1 < argc) {
        templateName = argv[i + 1];
        i++;
      }
    } else
    if (arg == "-controlMode") {
      if (i + 1 < argc) {
        controlMode = Fabricator::parseControlMode(argv[i + 1]);
        i++;
      }
    } else if (arg == "-craftAheadSeconds") {
      if (i + 1 < argc) {
        craftAheadSeconds = std::stoi(argv[i + 1]);
        i++;
      }
    } else if (arg == "-dubAheadSeconds") {
      if (i + 1 < argc) {
        dubAheadSeconds = std::stoi(argv[i + 1]);
        i++;
      }
    } else if (arg == "-persistenceWindowSeconds") {
      if (i + 1 < argc) {
        persistenceWindowSeconds = std::stoi(argv[i + 1]);
        i++;
      }
    } else if (arg == "-h" || arg == "--help") {
      showUsage(argv[0]);
      return 0;
    }
  }

  try {
    XJPlayer player(pathToProjectFile,
                    controlMode, templateName,
                    craftAheadSeconds,
                    dubAheadSeconds,
                    persistenceWindowSeconds);
    player.Start();

  } catch (const std::exception &e) {
    std::cerr << "Error: " << e.what() << std::endl;
    return -1;
  }
  return 0;
}
