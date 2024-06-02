# Fetch content module is available in CMake 3.11+
include(FetchContent)

# Tests need to be added as executables first
add_executable(xjmusic_test
        _helper/AssertionHelpers.cpp
        _helper/ContentTestHelper.cpp
        content/ContentStoreTest.cpp
        content/InstrumentAudioTest.cpp
        content/InstrumentConfigTest.cpp
        content/InstrumentMemeTest.cpp
        content/InstrumentTest.cpp
        content/LibraryTest.cpp
        content/MemeTaxonomyTest.cpp
        content/ProgramConfigTest.cpp
        content/ProgramMemeTest.cpp
        content/ProgramSequenceBindingMemeTest.cpp
        content/ProgramSequenceBindingTest.cpp
        content/ProgramSequenceChordTest.cpp
        content/ProgramSequenceChordVoicingTest.cpp
        content/ProgramSequencePatternEventTest.cpp
        content/ProgramSequencePatternTest.cpp
        content/ProgramSequenceTest.cpp
        content/ProgramTest.cpp
        content/ProgramVoiceTest.cpp
        content/ProgramVoiceTrackTest.cpp
        content/ProjectTest.cpp
        content/TemplateBindingTest.cpp
        content/TemplateConfigTest.cpp
        content/TemplateTest.cpp
        music/AccidentalTest.cpp
        music/BPMTest.cpp
        music/BarTest.cpp
        music/ChordTest.cpp
        music/NoteRangeTest.cpp
        music/NoteTest.cpp
        music/OctaveTest.cpp
        music/PitchClassTest.cpp
        music/RootTest.cpp
        music/SlashRootTest.cpp
        music/StepTest.cpp
        music/StickyBunTest.cpp
        music/TuningTest.cpp
        util/ConfigParserTest.cpp
        util/StringUtilsTest.cpp
        util/ValueUtilsTest.cpp
)

# I'm using C++17 in the test
target_compile_features(xjmusic_test PRIVATE cxx_std_17)

# Turn off the optimizer for faster build and easier debugging
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -O0")

# Link the libraries from the main project
target_link_libraries(xjmusic_test PRIVATE xjmusic)

# Google Test (googletest)
FetchContent_Declare(googletest URL https://github.com/google/googletest/archive/5376968f6948923e2411081fd9372e71a59d8e77.zip)
FetchContent_MakeAvailable(googletest)
target_link_libraries(xjmusic_test PRIVATE gtest_main)

# yaml-cpp (jbeder/yaml-cpp)
FetchContent_Declare(yaml-cpp URL https://github.com/jbeder/yaml-cpp/archive/refs/tags/0.8.0.tar.gz)
FetchContent_GetProperties(yaml-cpp)
if(NOT yaml-cpp_POPULATED)
    message(STATUS "Fetching yaml-cpp...")
    FetchContent_Populate(yaml-cpp)
    add_subdirectory(${yaml-cpp_SOURCE_DIR} ${yaml-cpp_BINARY_DIR})
endif()
target_link_libraries(xjmusic_test PUBLIC yaml-cpp::yaml-cpp)

# For Windows: Prevent overriding the parent project's compiler/linker settings
set(gtest_force_shared_crt ON CACHE BOOL "" FORCE)

# Test command
add_test(NAME xjmusic_test COMMAND xjmusic_test)

# Copy _data directory to the test executable directory
file(COPY ${CMAKE_SOURCE_DIR}/test/_data DESTINATION ${CMAKE_CURRENT_BINARY_DIR})

# Enable testing
enable_testing()