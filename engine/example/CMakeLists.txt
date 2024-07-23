# Define the path to the SDL2 archive and the desired output directory
set(SDL2_ARCHIVE ${CMAKE_SOURCE_DIR}/vendor/SDL-release-2.30.5.zip)
set(SDL2_OUTPUT_DIR ${CMAKE_BINARY_DIR}/SDL-release-2.30.5)

add_custom_target(unpack_sdl2 ALL)
add_custom_command(TARGET unpack_sdl2 PRE_BUILD
        COMMAND ${CMAKE_COMMAND} -E remove_directory ${SDL2_OUTPUT_DIR}
        COMMAND ${CMAKE_COMMAND} -E tar xzf ${SDL2_ARCHIVE}
        WORKING_DIRECTORY ${CMAKE_BINARY_DIR}
        DEPENDS ${SDL2_ARCHIVE}
        COMMENT "Unpacking ${SDL2_ARCHIVE}"
        VERBATIM)

# Now, you can include the SDL2 directory in your project
include_directories(${SDL2_OUTPUT_DIR})

# Create the executable
add_executable(xj_player XJPlayer.cpp ../include/xjmusic/Engine.h)
target_compile_features(xj_player PRIVATE cxx_std_17)
target_link_libraries(xj_player PRIVATE xjmusic)
add_dependencies(xj_player unpack_sdl2)

# Attach the SDL2 library
find_file(SDL2_INCLUDE_DIR NAME SDL.h HINTS SDL2)
find_library(SDL2_LIBRARY NAME SDL2)
message(STATUS "SDL2_INCLUDE_DIR: ${SDL2_INCLUDE_DIR}")
# target_include_directories(xj_player ${SDL2_INCLUDE_DIR})
target_link_libraries(xj_player PRIVATE ${SDL2_LIBRARY})