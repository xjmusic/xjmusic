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
function(add_vendored_dependency TARGET_NAME ARCHIVE_NAME UNPACKED_DIR_NAME HASH_MD5 BUILD_BYPRODUCT_FILES)
    set(BUILD_BYPRODUCTS "")
    foreach (BUILD_BYPRODUCT_FILE ${BUILD_BYPRODUCT_FILES})
        list(APPEND BUILD_BYPRODUCTS "${CMAKE_BINARY_DIR}/${UNPACKED_DIR_NAME}/src/${TARGET_NAME}-build/${BUILD_BYPRODUCT_FILE}")
    endforeach ()
    message(STATUS "Adding vendored dependency ${TARGET_NAME} from ${ARCHIVE_NAME} with ${BUILD_BYPRODUCTS}")
    ExternalProject_Add(${TARGET_NAME}
            URL ${CMAKE_SOURCE_DIR}/vendor/${ARCHIVE_NAME}
            PREFIX ${CMAKE_BINARY_DIR}/${UNPACKED_DIR_NAME}
            URL_HASH MD5=${HASH_MD5}
            BUILD_BYPRODUCTS ${BUILD_BYPRODUCTS}
            INSTALL_COMMAND ""
    )
    # For Windows: Build with MultiThreadedDebug runtime library
    if (WIN32)
        set_property(TARGET ${TARGET_NAME} PROPERTY MSVC_RUNTIME_LIBRARY "MultiThreadedDebug")
    endif ()
endfunction()

# Windows libaries have different names than Linux/Mac
if (WIN32)
    add_vendored_dependency(vendored_googletest googletest-1.15.0.tar.gz googletest-1.15.0 1fb60c818501bcecee87349cf2e7377f "lib/gtest.lib;lib/gmock.lib;lib/gtest_main.lib;lib/gmock_main.lib")
    add_vendored_dependency(vendored_yaml yaml-cpp-0.8.0.tar.gz yaml-cpp-0.8.0 1d2c7975edba60e995abe3c4af6480e5 "yaml-cppd.lib")
    add_vendored_dependency(vendored_sdl2 SDL-release-2.30.5.zip SDL-release-2.30.5 c0bbff02fc1919bd42defc2a486c4056 "SDL2d.lib")
else ()
    add_vendored_dependency(vendored_googletest googletest-1.15.0.tar.gz googletest-1.15.0 1fb60c818501bcecee87349cf2e7377f "lib/libgtest.a;lib/libgmock.a;lib/libgtest_main.a;lib/libgmock_main.a")
    add_vendored_dependency(vendored_yaml yaml-cpp-0.8.0.tar.gz yaml-cpp-0.8.0 1d2c7975edba60e995abe3c4af6480e5 "libyaml-cpp.a")
    add_vendored_dependency(vendored_sdl2 SDL-release-2.30.5.zip SDL-release-2.30.5 c0bbff02fc1919bd42defc2a486c4056 "libSDL2.a")
endif ()
add_vendored_dependency(vendored_json nlohmann-json-3.11.3.zip json-3.11.3 23712ebf3a4b4ccb39f2375521716ab3 "")


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