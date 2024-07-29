// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <iostream>

#include "XJPlayer.h"
#include "xjmusic/util/CsvUtils.h"

/**
 * Show the usage of the application.
 * @param name  The name of the application.
 */
void showUsage(const std::string& name)
{
	std::cout << "Usage: " << name << " <pathToProjectFile> [-template <name>] [-mode <control>] [-craft <seconds ahead>] [-dub <seconds ahead>] [-persistence <window in seconds>]" << std::endl;
	std::cout << "  <pathToProjectFile>     (REQUIRED) The path to the XJ music workstation .xj project file." << std::endl;
	std::cout << "  -template <name>        The name of the template to start with." << std::endl;
	std::cout << "  -mode <control>         The control mode to use. Options are: Auto, Taxonomy, or Macro." << std::endl;
	std::cout << "  -craft <seconds ahead>  How many seconds ahead to craft." << std::endl;
	std::cout << "  -dub <seconds ahead>    How many seconds ahead to dub." << std::endl;
	std::cout << "  -deadline <seconds>     How many seconds ahead is the scheduling deadline." << std::endl;
	std::cout << "  -persistence <seconds>  How long to keep segments in memory." << std::endl;
	std::cout << "  -h, --help              Show this help message." << std::endl;
}

/**
 * Main entry point of the application.
 * @param argc  The number of arguments passed to the application.
 * @param argv  The arguments passed to the application.
 * @return    The exit code of the application.
 */
int main(const int argc, char* argv[])
{
	// Check if at least one argument was passed
	if (argc <= 1)
	{
		std::cout << "Must pass the path to an XJ music workstation .xj project as the first argument!" << std::endl;
		showUsage(argv[0]);
		return -1;
	}
	std::string pathToProjectFile = argv[1];
	std::cout << "Will open project: " << pathToProjectFile << std::endl;

	// User can specify the control mode, craft ahead seconds, dub ahead seconds, and persistence window seconds
	Fabricator::ControlMode	   controlMode = Fabricator::ControlMode::Auto;
	std::optional<int>		   craftAheadSeconds;
	std::optional<int>		   dubAheadSeconds;
	std::optional<int>		   deadlineSeconds;
	std::optional<int>		   persistenceWindowSeconds;
	std::optional<std::string> templateName;

	// Parse all remaining arguments to set the control mode, craft ahead seconds, dub ahead seconds, and persistence window seconds
	for (int i = 2; i < argc; i++)
	{
		std::string arg = argv[i];
		if (arg == "-template")
		{
			if (i + 1 < argc)
			{
				templateName = argv[i + 1];
				i++;
			}
		}
		else if (arg == "-mode")
		{
			if (i + 1 < argc)
			{
				controlMode = Fabricator::parseControlMode(argv[i + 1]);
				i++;
			}
		}
		else if (arg == "-craft")
		{
			if (i + 1 < argc)
			{
				craftAheadSeconds = std::stoi(argv[i + 1]);
				i++;
			}
		}
		else if (arg == "-dub")
		{
			if (i + 1 < argc)
			{
				dubAheadSeconds = std::stoi(argv[i + 1]);
				i++;
			}
		}
		else if (arg == "-deadline")
		{
			if (i + 1 < argc)
			{
				deadlineSeconds = std::stoi(argv[i + 1]);
				i++;
			}
		}
		else if (arg == "-persistence")
		{
			if (i + 1 < argc)
			{
				persistenceWindowSeconds = std::stoi(argv[i + 1]);
				i++;
			}
		}
		else if (arg == "-h" || arg == "--help")
		{
			showUsage(argv[0]);
			return 0;
		}
	}

	try
	{
		XJPlayer player(pathToProjectFile,
			controlMode,
			templateName,
			craftAheadSeconds,
			dubAheadSeconds,
			deadlineSeconds,
			persistenceWindowSeconds);
		player.Start();
	}
	catch (const std::exception& e)
	{
		std::cerr << "Error: " << e.what() << std::endl;
		return -1;
	}
	return 0;
}
