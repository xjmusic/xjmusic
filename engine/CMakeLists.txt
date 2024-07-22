cmake_minimum_required(VERSION 3.24)

# Project name and a few useful settings. Other commands can pick up the results
project(
        xjmusic
        VERSION 1.8.42
        DESCRIPTION "The XJ music engine"
        LANGUAGES CXX)

# C++ 17
set(CMAKE_CXX_STANDARD 17)

# Add a vendored dependency from the given archive
include(ExternalProject)
function(add_vendored_dependency TARGET_NAME ARCHIVE_NAME UNPACKED_DIR_NAME HASH_MD5)
    ExternalProject_Add(${TARGET_NAME}
            URL ${CMAKE_SOURCE_DIR}/vendor/${ARCHIVE_NAME}
            PREFIX ${CMAKE_BINARY_DIR}/${UNPACKED_DIR_NAME}
            URL_HASH MD5=${HASH_MD5}
            INSTALL_COMMAND ""
    )

endfunction()

add_vendored_dependency(vendored_googletest googletest-1.15.0.tar.gz googletest-1.15.0 1fb60c818501bcecee87349cf2e7377f)
add_vendored_dependency(vendored_json nlohmann-json-3.11.3.zip json-3.11.3 23712ebf3a4b4ccb39f2375521716ab3)
add_vendored_dependency(vendored_sdl2 SDL-release-2.30.5.zip SDL-release-2.30.5 c0bbff02fc1919bd42defc2a486c4056)
add_vendored_dependency(vendored_yaml yaml-cpp-0.8.0.tar.gz yaml-cpp-0.8.0 1d2c7975edba60e995abe3c4af6480e5)

# Only do these if this is the main project, and not if it is included through add_subdirectory
if (CMAKE_PROJECT_NAME STREQUAL PROJECT_NAME)

    # Optionally set things like CMAKE_CXX_STANDARD, CMAKE_POSITION_INDEPENDENT_CODE here

    # Let's ensure -std=c++xx instead of -std=g++xx
    set(CMAKE_CXX_EXTENSIONS OFF)

    # Let's nicely support folders in IDEs
    set_property(GLOBAL PROPERTY USE_FOLDERS ON)

    # Testing only available if this is the main app
    # Note this needs to be done in the main CMakeLists
    # since it calls enable_testing, which must be in the
    # main CMakeLists.
    include(CTest)

    # Docs only available if this is the main app
    find_package(Doxygen)
    if (Doxygen_FOUND)
        add_subdirectory(doc)
    else ()
        message(STATUS "Doxygen not found, not building docs")
    endif ()
endif ()

# The compiled library code is here
add_subdirectory(src)

# The executable code is here
add_subdirectory(example)

# Testing only available if this is the main app
# Emergency override MODERN_CMAKE_BUILD_TESTING provided as well
if ((CMAKE_PROJECT_NAME STREQUAL PROJECT_NAME OR MODERN_CMAKE_BUILD_TESTING)
        AND BUILD_TESTING)
    add_subdirectory(test)
endif ()