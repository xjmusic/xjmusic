cmake_minimum_required(VERSION 3.24)

# Project name and a few useful settings. Other commands can pick up the results
project(
        xjmusic
        VERSION 1.8.42
        DESCRIPTION "The XJ music engine"
        LANGUAGES CXX)

# C++ 17
set(CMAKE_CXX_STANDARD 17)

## Add a vendored dependency from the given archive
#function(add_vendored_dependency TARGET_NAME ARCHIVE_NAME UNPACKED_DIR_NAME)
#    file(MAKE_DIRECTORY ${CMAKE_BINARY_DIR}/${UNPACKED_DIR_NAME}/build)
#    add_custom_target(${TARGET_NAME} ALL)
#    # Unpack the archive
#    add_custom_command(TARGET ${TARGET_NAME} PRE_BUILD
#            DEPENDS ${CMAKE_SOURCE_DIR}/vendor/${ARCHIVE_NAME}
#            #
#            COMMAND echo "\n\nUnpacking vendor/${ARCHIVE_NAME}"
#            WORKING_DIRECTORY ${CMAKE_BINARY_DIR}
#            COMMAND ${CMAKE_COMMAND} -E tar xzf ${CMAKE_SOURCE_DIR}/vendor/${ARCHIVE_NAME}
#            #
#            COMMAND echo "\n\nConfiguring ${UNPACKED_DIR_NAME}"
#            WORKING_DIRECTORY ${CMAKE_BINARY_DIR}/${UNPACKED_DIR_NAME}/build
#            COMMAND ${CMAKE_COMMAND} .. -DCMAKE_BUILD_TYPE=Release
#            #
#            COMMAND echo "\n\nBuilding ${UNPACKED_DIR_NAME}"
#            WORKING_DIRECTORY ${CMAKE_BINARY_DIR}/${UNPACKED_DIR_NAME}/build
#            COMMAND ${CMAKE_COMMAND} --build .
#            VERBATIM)
#endfunction()

# Ensure the vendored_json target is correctly defined in your CMakeLists.txt
function(add_vendored_dependency TARGET_NAME ARCHIVE_NAME UNPACKED_DIR_NAME)
    file(MAKE_DIRECTORY ${CMAKE_BINARY_DIR}/${UNPACKED_DIR_NAME}/build)
    add_custom_target(${TARGET_NAME} ALL)
    add_custom_command(TARGET ${TARGET_NAME} PRE_BUILD
            DEPENDS ${CMAKE_SOURCE_DIR}/vendor/${ARCHIVE_NAME}
            WORKING_DIRECTORY ${CMAKE_BINARY_DIR}
            COMMAND ${CMAKE_COMMAND} -E tar xzf ${CMAKE_SOURCE_DIR}/vendor/${ARCHIVE_NAME}
            COMMAND ${CMAKE_COMMAND} -E chdir ${CMAKE_BINARY_DIR}/${UNPACKED_DIR_NAME} cmake -S . -B build -DCMAKE_BUILD_TYPE=Release
            COMMAND ${CMAKE_COMMAND} --build ${CMAKE_BINARY_DIR}/${UNPACKED_DIR_NAME}/build
            VERBATIM)
endfunction()

add_vendored_dependency(vendored_sdl2 SDL-release-2.30.5.zip SDL-release-2.30.5)
add_vendored_dependency(vendored_json nlohmann-json-3.11.3.zip json-3.11.3)
add_vendored_dependency(vendored_googletest googletest-1.14.0.zip googletest-1.14.0)
add_vendored_dependency(vendored_spdlog spdlog-1.13.0.tar.gz spdlog-1.13.0)
add_vendored_dependency(vendored_yaml yaml-cpp-0.8.0.tar.gz yaml-cpp-0.8.0)

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

# FetchContent added in CMake 3.11, downloads during the configure step
include(FetchContent)

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