add_executable(xj_player XJPlayer.cpp
        ../include/xjmusic/Engine.h)
target_compile_features(xj_player PRIVATE cxx_std_17)

target_link_libraries(xj_player PRIVATE xjmusic)

# JSON for Modern C++
add_dependencies(xj_player vendored_json)
target_include_directories(xj_player PRIVATE ${CMAKE_BINARY_DIR}/json-3.11.3/include)

# SDL2
add_dependencies(xj_player vendored_sdl2)
target_include_directories(xj_player PRIVATE ${CMAKE_BINARY_DIR}/SDL-release-2.30.5/include)
find_library(SDL2_LIBRARIES SDL2 HINTS ${CMAKE_BINARY_DIR}/SDL-release-2.30.5/build)
target_link_libraries(xj_player PRIVATE ${SDL2_LIBRARIES})
