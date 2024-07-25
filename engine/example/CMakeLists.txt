# Fetch content module is available in CMake 3.11+
include(FetchContent)

# Create the executable
add_executable(xj_player main.cpp ../include/xjmusic/Engine.h
        XJPlayer.cpp
        EngineUiBase.cpp
        EngineUiBase.h)
target_compile_features(xj_player PRIVATE cxx_std_17)
target_link_libraries(xj_player PRIVATE xjmusic)

# Add SDL2
if (WIN32)
    add_vendor_external_project(xj_player vendor_sdl2 "SDL-release-2.30.5.tar.gz" "e3b9c6faf8b28567e069588c8f8fecef" "lib/SDL2d.lib;lib/SDL2maind.lib" "bin/SDL2d.dll")
elseif (APPLE)
    add_vendor_external_project(xj_player vendor_sdl2 "SDL-release-2.30.5.tar.gz" "e3b9c6faf8b28567e069588c8f8fecef" "lib/libSDL2.a;lib/libSDL2main.a" "lib/libSDL2.dylib")
    target_link_libraries(xj_player PRIVATE "-framework AudioToolbox")
    target_link_libraries(xj_player PRIVATE "-framework Cocoa")
    target_link_libraries(xj_player PRIVATE "-framework CoreAudio")
    target_link_libraries(xj_player PRIVATE "-framework CoreFoundation")
    target_link_libraries(xj_player PRIVATE "-framework CoreGraphics")
    target_link_libraries(xj_player PRIVATE "-framework CoreHaptics")
    target_link_libraries(xj_player PRIVATE "-framework CoreVideo")
    target_link_libraries(xj_player PRIVATE "-framework GameController")
    target_link_libraries(xj_player PRIVATE "-framework IOKit")
    target_link_libraries(xj_player PRIVATE "-framework Metal")
    target_link_libraries(xj_player PRIVATE "-framework Carbon")
    target_link_libraries(xj_player PRIVATE "-framework ForceFeedback")
else ()
    add_vendor_external_project(xj_player vendor_sdl2 "SDL-release-2.30.5.tar.gz" "e3b9c6faf8b28567e069588c8f8fecef" "lib/libSDL2.a;lib/libSDL2main.a" "lib/libSDL2.so")
endif ()

# FTXUI
FetchContent_Declare(ftxui URL "${CMAKE_SOURCE_DIR}/vendor/FTXUI-5.0.0.tar.gz")
FetchContent_MakeAvailable(ftxui)
target_link_libraries(xj_player
        PRIVATE ftxui::screen
        PRIVATE ftxui::dom
        PRIVATE ftxui::component
)

