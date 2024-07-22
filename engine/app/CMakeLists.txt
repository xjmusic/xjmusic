add_executable(xj_player XJPlayer.cpp
        ../include/xjmusic/Engine.h)
target_compile_features(xj_player PRIVATE cxx_std_17)

target_link_libraries(xj_player PRIVATE xjmusic)

