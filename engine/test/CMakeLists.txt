# Fetch content module is available in CMake 3.11+
include(FetchContent)

# Tests need to be added as executables first
add_executable(xjmusic_test
        _helper/AssertionHelpers.cpp
        _helper/ContentTestHelper.cpp
        _helper/LoremIpsum.cpp
        entities/content/ContentStoreTest.cpp
        entities/content/InstrumentAudioTest.cpp
        entities/content/InstrumentConfigTest.cpp
        entities/content/InstrumentMemeTest.cpp
        entities/content/InstrumentTest.cpp
        entities/content/LibraryTest.cpp
        entities/content/MemeTaxonomyTest.cpp
        entities/content/ProgramConfigTest.cpp
        entities/content/ProgramMemeTest.cpp
        entities/content/ProgramSequenceBindingMemeTest.cpp
        entities/content/ProgramSequenceBindingTest.cpp
        entities/content/ProgramSequenceChordTest.cpp
        entities/content/ProgramSequenceChordVoicingTest.cpp
        entities/content/ProgramSequencePatternEventTest.cpp
        entities/content/ProgramSequencePatternTest.cpp
        entities/content/ProgramSequenceTest.cpp
        entities/content/ProgramTest.cpp
        entities/content/ProgramVoiceTest.cpp
        entities/content/ProgramVoiceTrackTest.cpp
        entities/content/ProjectTest.cpp
        entities/content/TemplateBindingTest.cpp
        entities/content/TemplateConfigTest.cpp
        entities/content/TemplateTest.cpp
        entities/music/AccidentalTest.cpp
        entities/music/BPMTest.cpp
        entities/music/BarTest.cpp
        entities/music/ChordTest.cpp
        entities/music/NoteRangeTest.cpp
        entities/music/NoteTest.cpp
        entities/music/OctaveTest.cpp
        entities/music/PitchClassTest.cpp
        entities/music/RootTest.cpp
        entities/music/SlashRootTest.cpp
        entities/music/StepTest.cpp
        entities/music/StickyBunTest.cpp
        entities/music/TuningTest.cpp
        fabricator/SegmentUtilsTest.cpp
        util/ConfigParserTest.cpp
        util/StringUtilsTest.cpp
        util/ValueUtilsTest.cpp
        _helper/FabricationContentOneFixtures.cpp
        _helper/FabricationContentTwoFixtures.cpp
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