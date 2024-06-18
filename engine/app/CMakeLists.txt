add_executable(app app.cpp
        ../include/xjmusic/music/Accidental.h)
target_compile_features(app PRIVATE cxx_std_17)

target_link_libraries(app PRIVATE xjmusic)

