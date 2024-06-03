# FetchContent added in CMake 3.11, downloads during the configure step
include(FetchContent)

# show up in IDEs unless they are listed in add_library.
add_library(xjmusic
        entities/content/ContentStore.cpp
        entities/content/Entity.cpp
        entities/content/Instrument.cpp
        entities/content/InstrumentAudio.cpp
        entities/content/InstrumentConfig.cpp
        entities/content/InstrumentMeme.cpp
        entities/content/Library.cpp
        entities/content/MemeTaxonomy.cpp
        entities/content/Program.cpp
        entities/content/ProgramConfig.cpp
        entities/content/ProgramMeme.cpp
        entities/content/ProgramSequence.cpp
        entities/content/ProgramSequenceBinding.cpp
        entities/content/ProgramSequenceBindingMeme.cpp
        entities/content/ProgramSequenceChord.cpp
        entities/content/ProgramSequenceChordVoicing.cpp
        entities/content/ProgramSequencePattern.cpp
        entities/content/ProgramSequencePatternEvent.cpp
        entities/content/ProgramVoice.cpp
        entities/content/ProgramVoiceTrack.cpp
        entities/content/Project.cpp
        entities/content/Template.cpp
        entities/content/TemplateBinding.cpp
        entities/content/TemplateConfig.cpp
        entities/music/Accidental.cpp
        entities/music/BPM.cpp
        entities/music/Bar.cpp
        entities/music/Chord.cpp
        entities/music/Note.cpp
        entities/music/NoteRange.cpp
        entities/music/Octave.cpp
        entities/music/PitchClass.cpp
        entities/music/Root.cpp
        entities/music/SlashRoot.cpp
        entities/music/Step.cpp
        entities/music/StickyBun.cpp
        entities/music/Tuning.cpp
        entities/segment/Chain.cpp
        entities/segment/Segment.cpp
        entities/segment/SegmentChoice.cpp
        entities/segment/SegmentChoiceArrangement.cpp
        entities/segment/SegmentChoiceArrangementPick.cpp
        entities/segment/SegmentChord.cpp
        entities/segment/SegmentChordVoicing.cpp
        entities/segment/SegmentMeme.cpp
        entities/segment/SegmentMessage.cpp
        entities/segment/SegmentMeta.cpp
        util/ConfigParser.cpp
        util/StringUtils.cpp
        util/ValueUtils.cpp
        xjmusic.cpp
)

# We need this directory, and users of our library will need it too
target_include_directories(xjmusic PUBLIC ../include)

# All users of this library will need at least C++17
target_compile_features(xjmusic PUBLIC cxx_std_17)

# Fast C++ logging library (gabime/spdlog)
FetchContent_Declare(spdlog URL https://github.com/gabime/spdlog/archive/v1.13.0.tar.gz)
FetchContent_MakeAvailable(spdlog)
target_link_libraries(xjmusic PRIVATE spdlog::spdlog)

# IDEs should put the headers in a nice place
source_group(
        TREE "${PROJECT_SOURCE_DIR}/include"
        PREFIX "Header Files"
        FILES ${HEADER_LIST})
