# FetchContent added in CMake 3.11, downloads during the configure step
include(FetchContent)

# show up in IDEs unless they are listed in add_library.
add_library(xjnexus
        content/ContentStore.cpp
        content/Entity.cpp
        content/Instrument.cpp
        content/InstrumentAudio.cpp
        content/InstrumentConfig.cpp
        content/InstrumentMeme.cpp
        content/Library.cpp
        content/MemeTaxonomy.cpp
        content/Program.cpp
        content/ProgramConfig.cpp
        content/ProgramMeme.cpp
        content/ProgramSequence.cpp
        content/ProgramSequenceBinding.cpp
        content/ProgramSequenceBindingMeme.cpp
        content/ProgramSequenceChord.cpp
        content/ProgramSequenceChordVoicing.cpp
        content/ProgramSequencePattern.cpp
        content/ProgramSequencePatternEvent.cpp
        content/ProgramVoice.cpp
        content/ProgramVoiceTrack.cpp
        content/Project.cpp
        content/Template.cpp
        content/TemplateBinding.cpp
        content/TemplateConfig.cpp
        music/Accidental.cpp
        music/BPM.cpp
        music/Bar.cpp
        music/Chord.cpp
        music/Note.cpp
        music/NoteRange.cpp
        music/Octave.cpp
        music/PitchClass.cpp
        music/Root.cpp
        music/SlashRoot.cpp
        music/Step.cpp
        music/StickyBun.cpp
        music/Tuning.cpp
        util/ConfigParser.cpp
        util/StringUtils.cpp
        util/ValueUtils.cpp
        xjnexus.cpp
)

# We need this directory, and users of our library will need it too
target_include_directories(xjnexus PUBLIC ../include)

# All users of this library will need at least C++17
target_compile_features(xjnexus PUBLIC cxx_std_17)

# Fast C++ logging library (gabime/spdlog)
FetchContent_Declare(spdlog URL https://github.com/gabime/spdlog/archive/v1.13.0.tar.gz)
FetchContent_MakeAvailable(spdlog)
target_link_libraries(xjnexus PRIVATE spdlog::spdlog)

# IDEs should put the headers in a nice place
source_group(
        TREE "${PROJECT_SOURCE_DIR}/include"
        PREFIX "Header Files"
        FILES ${HEADER_LIST})