# Fetch content module is available in CMake 3.11+
include(FetchContent)

# Tests need to be added as executables first
add_executable(xjmusic_test
        _helper/ContentFixtures.cpp
        _helper/LoremIpsum.cpp
        _helper/SegmentFixtures.cpp
        _helper/TestHelpers.cpp
        _helper/YamlTest.cpp
        audio/AudioMathUtilsTest.cpp
        content/ContentEntityStoreTest.cpp
        content/InstrumentAudioTest.cpp
        content/InstrumentConfigTest.cpp
        content/InstrumentMemeTest.cpp
        content/InstrumentTest.cpp
        content/LibraryTest.cpp
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
        craft/ArrangementTest.cpp
        craft/CraftTest.cpp
        craft/background/CraftBackgroundContinueTest.cpp
        craft/background/CraftBackgroundInitialTest.cpp
        craft/background/CraftBackgroundNextMacroTest.cpp
        craft/background/CraftBackgroundNextMainTest.cpp
        craft/background/CraftBackgroundProgramVoiceContinueTest.cpp
        craft/background/CraftBackgroundProgramVoiceInitialTest.cpp
        craft/background/CraftBackgroundProgramVoiceNextMacroTest.cpp
        craft/background/CraftBackgroundProgramVoiceNextMainTest.cpp
        craft/background/CraftBackground_LayeredVoicesTest.cpp
        craft/beat/CraftBeatContinueTest.cpp
        craft/beat/CraftBeatInitialTest.cpp
        craft/beat/CraftBeatNextMacroTest.cpp
        craft/beat/CraftBeatNextMainTest.cpp
        craft/beat/CraftBeatProgramVoiceContinueTest.cpp
        craft/beat/CraftBeatProgramVoiceInitialTest.cpp
        craft/beat/CraftBeatProgramVoiceNextMacroTest.cpp
        craft/beat/CraftBeatProgramVoiceNextMainTest.cpp
        craft/beat/CraftBeat_LayeredVoicesTest.cpp
        craft/detail/CraftDetailContinueTest.cpp
        craft/detail/CraftDetailInitialTest.cpp
        craft/detail/CraftDetailNextMacroTest.cpp
        craft/detail/CraftDetailNextMainTest.cpp
        craft/detail/CraftDetailProgramVoiceContinueTest.cpp
        craft/detail/CraftDetailProgramVoiceInitialTest.cpp
        craft/detail/CraftDetailProgramVoiceNextMacroTest.cpp
        craft/detail/CraftDetailProgramVoiceNextMainTest.cpp
        craft/detail_hook/CraftHookContinueTest.cpp
        craft/detail_hook/CraftHookInitialTest.cpp
        craft/detail_hook/CraftHookNextMacroTest.cpp
        craft/detail_hook/CraftHookNextMainTest.cpp
        craft/detail_hook/CraftHookProgramVoiceContinueTest.cpp
        craft/detail_hook/CraftHookProgramVoiceInitialTest.cpp
        craft/detail_hook/CraftHookProgramVoiceNextMacroTest.cpp
        craft/detail_hook/CraftHookProgramVoiceNextMainTest.cpp
        craft/detail_perc_loop/CraftPercLoopContinueTest.cpp
        craft/detail_perc_loop/CraftPercLoopInitialTest.cpp
        craft/detail_perc_loop/CraftPercLoopNextMacroTest.cpp
        craft/detail_perc_loop/CraftPercLoopNextMainTest.cpp
        craft/detail_perc_loop/CraftPercLoopProgramVoiceContinueTest.cpp
        craft/detail_perc_loop/CraftPercLoopProgramVoiceInitialTest.cpp
        craft/detail_perc_loop/CraftPercLoopProgramVoiceNextMacroTest.cpp
        craft/detail_perc_loop/CraftPercLoopProgramVoiceNextMainTest.cpp
        craft/detail_perc_loop/CraftPercLoop_LayeredVoicesTest.cpp
        craft/macro_main/CraftFoundationContinueTest.cpp
        craft/macro_main/CraftFoundationInitialTest.cpp
        craft/macro_main/CraftFoundationNextMacroTest.cpp
        craft/macro_main/CraftFoundationNextMainTest.cpp
        craft/macro_main/CraftSegmentOutputEncoderTest.cpp
        craft/macro_main/CraftSegmentPatternMemeTest.cpp
        craft/macro_main/MacroFromOverlappingMemeSequencesTest.cpp
        craft/transition/CraftTransitionContinueTest.cpp
        craft/transition/CraftTransitionInitialTest.cpp
        craft/transition/CraftTransitionNextMacroTest.cpp
        craft/transition/CraftTransitionNextMainTest.cpp
        craft/transition/CraftTransitionProgramVoiceContinueTest.cpp
        craft/transition/CraftTransitionProgramVoiceInitialTest.cpp
        craft/transition/CraftTransitionProgramVoiceNextMacroTest.cpp
        craft/transition/CraftTransitionProgramVoiceNextMainTest.cpp
        craft/transition/CraftTransition_LayeredVoicesTest.cpp
        fabricator/FabricatorTest.cpp
        fabricator/MarbleBagTest.cpp
        fabricator/NotePickerTest.cpp
        fabricator/SegmentRetrospectiveTest.cpp
        fabricator/SegmentUtilsTest.cpp
        meme/MemeIsometryTest.cpp
        meme/MemeStackTest.cpp
        meme/MemeTaxonomyTest.cpp
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
        segment/SegmentEntityStoreTest.cpp
        segment/SegmentMemeTest.cpp
        util/ConfigParserTest.cpp
        util/CsvUtilsTest.cpp
        util/StringUtilsTest.cpp
        util/ValueUtilsTest.cpp
        work/ComplexLibraryTest.cpp
)

# I'm using C++17 in the test
target_compile_features(xjmusic_test PRIVATE cxx_std_17)

# Turn off the optimizer for faster build and easier debugging
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -O0")
set(CMAKE_CXX_FLAGS_DEBUG "${CMAKE_CXX_FLAGS_DEBUG} -O0")

# Link the libraries from the main project
target_link_libraries(xjmusic_test PRIVATE xjmusic)

# Google Test (googletest)
FetchContent_Declare(googletest URL https://github.com/google/googletest/archive/refs/tags/v1.14.0.zip)
FetchContent_MakeAvailable(googletest)
target_link_libraries(xjmusic_test PRIVATE gtest_main)
target_link_libraries(xjmusic_test PRIVATE gmock_main)

# yaml-cpp (jbeder/yaml-cpp)
FetchContent_Declare(yaml-cpp URL https://github.com/jbeder/yaml-cpp/archive/refs/tags/0.8.0.tar.gz)
FetchContent_GetProperties(yaml-cpp)
if(NOT yaml-cpp_POPULATED)
    message(STATUS "Fetching yaml-cpp...")
    FetchContent_Populate(yaml-cpp)
    add_subdirectory(${yaml-cpp_SOURCE_DIR} ${yaml-cpp_BINARY_DIR})
endif()
target_link_libraries(xjmusic_test PUBLIC yaml-cpp::yaml-cpp)

# Fast C++ logging library (gabime/spdlog)
FetchContent_Declare(spdlog URL https://github.com/gabime/spdlog/archive/v1.13.0.tar.gz)
FetchContent_MakeAvailable(spdlog)
target_link_libraries(xjmusic_test PRIVATE spdlog::spdlog)

# For Windows: Prevent overriding the parent project's compiler/linker settings
set(gtest_force_shared_crt ON CACHE BOOL "" FORCE)

# Test command
add_test(NAME xjmusic_test COMMAND xjmusic_test)

# Copy _data directory to the test executable directory
file(COPY ${CMAKE_SOURCE_DIR}/test/_data DESTINATION ${CMAKE_CURRENT_BINARY_DIR})

# Enable testing
enable_testing()