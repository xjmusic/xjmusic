// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <iostream>

#include "xjmusic/Engine.h"
#include "xjmusic/util/CsvUtils.h"

int main(int argc, char *argv[]) {
  // Check if at least one argument was passed
  if (argc <= 1) {
    std::cout << "Must pass the path to an XJ music workstation .xj project as the first argument!" << std::endl;
    return -1;
  }
  std::string pathToProjectFile = argv[1];
  std::cout << "Will open project: " << pathToProjectFile << std::endl;

  try {
    std::unique_ptr<Engine> engine = std::make_unique<Engine>(
        pathToProjectFile,
        Fabricator::ControlMode::Auto,
        std::nullopt,
        std::nullopt,
        std::nullopt
    );
    std::cout << "Loaded project OK" << std::endl << std::endl;
    std::cout << "[Meme Taxonomy]" << std::endl;
    for (MemeCategory category: engine->getMemeTaxonomy()->getCategories()) {
      std::cout << "  " << category.getName() << "[" << CsvUtils::join(category.getMemes()) << "]" << std::endl;
    }
    engine->start(std::nullopt);

  } catch (const std::exception &e) {
    std::cerr << "Error: " << e.what() << std::endl;
    return -1;
  }
  return 0;
}