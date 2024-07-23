# Create the executable
add_executable(xj_player XJPlayer.cpp ../include/xjmusic/Engine.h)
target_compile_features(xj_player PRIVATE cxx_std_17)
target_link_libraries(xj_player PRIVATE xjmusic)

ExternalProject_Add(vendor_sdl
        PREFIX "${CMAKE_BINARY_DIR}/vendor"
        URL "${CMAKE_SOURCE_DIR}/vendor/SDL-release-2.30.5.tar.gz"
        URL_HASH MD5=c0bbff02fc1919bd42defc2a486c4056
        CMAKE_ARGS -DCMAKE_INSTALL_PREFIX=${CMAKE_BINARY_DIR}/vendor/installed
)
set_target_properties(vendor_sdl PROPERTIES EXCLUDE_FROM_ALL TRUE)