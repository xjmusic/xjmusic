# Create the executable
add_executable(xj_player XJPlayer.cpp ../include/xjmusic/Engine.h)
target_compile_features(xj_player PRIVATE cxx_std_17)
target_link_libraries(xj_player PRIVATE xjmusic)

# Add SDL2
if (WIN32)
    add_vendor_external_project(xj_player vendor_sdl2 "SDL-release-2.30.5.tar.gz" "e3b9c6faf8b28567e069588c8f8fecef" "lib/SDL2d.lib;lib/SDL2maind.lib" "bin/SDL2d.dll")
else ()
    add_vendor_external_project(xj_player vendor_sdl2 "SDL-release-2.30.5.tar.gz" "e3b9c6faf8b28567e069588c8f8fecef" "lib/libSDL2.a;lib/libSDL2main.a" "lib/libSDL2.so")
endif ()