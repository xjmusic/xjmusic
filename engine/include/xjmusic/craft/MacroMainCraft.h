// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef XJMUSIC_CRAFT_MACRO_MAIN_CRAFT_H
#define XJMUSIC_CRAFT_MACRO_MAIN_CRAFT_H

#include "xjmusic/craft/Craft.h"
#include "xjmusic/fabricator/Fabricator.h"

namespace XJ {

  /**
   [#138] Foundation craft for Initial Segment of a Chain
   [#214] If a Chain has Sequences associated with it directly, prefer those choices to any in the Library
   */
  class MacroMainCraft : protected Craft {
    std::optional<const Program *> overrideMacroProgram;
    std::set<std::string> overrideMemes;

  public:
    MacroMainCraft(
        Fabricator *fabricator,
        const std::optional<const Program *> &overrideMacroProgram,
        const std::set<std::string> &overrideMemes);

    /**
     perform macro craft for the current segment
     */
    void doWork() const;

   /**
    will rank all possibilities, and choose the next macro program

    @return macro-type program
    */
   const Program *chooseMacroProgram() const;

  private:
    /**
     Do the macro-choice work.

     @param segment of which to compute main choice
     @return the macro sequence
     */
    const ProgramSequence *doMacroChoiceWork(const Segment *segment) const;

    /**
     Do the main-choice work.

     @param segment of which to compute main choice
     @return the main sequence
     */
    const ProgramSequence *doMainChoiceWork(const Segment *segment) const;

    /**
     Compute the key of the current segment key, the key of the current main program sequence

     @param mainSequence of which to compute key
     @return key
     */
    std::string computeSegmentKey(const ProgramSequence *mainSequence) const;

    /**
     Compute the intensity of the current segment
     future: Segment Intensity = average of macro and main-sequence patterns
     <p>
     Segment is assigned a intensity during macro-main craft. It's going to be used to determine a target # of perc loops
     Percussion Loops Alpha https://github.com/xjmusic/xjmusic/issues/261

     @param macroSequence of which to compute segment tempo
     @param mainSequence  of which to compute segment tempo
     @return intensity
     */
    float computeSegmentIntensity(
        int delta,
        std::optional<const ProgramSequence *> macroSequence,
        std::optional<const ProgramSequence *> mainSequence) const;

    /**
     Compute the average intensity of the two given sequences

     @param macroSequence of which to compute segment tempo
     @param mainSequence  of which to compute segment tempo
     @return intensity
     */
    static float computeIntensity(
        std::optional<const ProgramSequence *> macroSequence,
        std::optional<const ProgramSequence *> mainSequence);

    /**
     compute the macroSequenceBindingOffset

     @return macroSequenceBindingOffset
     */
    int computeMacroSequenceBindingOffset() const;

    /**
     compute the mainSequenceBindingOffset

     @return mainSequenceBindingOffset
     */
    int computeMainProgramSequenceBindingOffset() const;

    /**
     Choose program completely at random

     @param programs all from which to choose
     @param avoid    to avoid
     @return program
     */
    const Program *chooseRandomProgram(const std::set<const Program *> &programs, std::set<UUID> avoid) const;

    /**
     Choose main program
     <p>
     ONLY CHOOSES ONCE, then returns that choice every time

     @return main-type Program
     */
    const Program *chooseMainProgram() const;

    /**
     Get Segment length, in nanoseconds

     @param mainProgram  from which to source tempo
     @param mainSequence the end of which marks the end of the segment
     @return segment length, in nanoseconds
     */
    long segmentLengthMicros(const Program *mainProgram, const ProgramSequence *mainSequence) const;
  };

}// namespace XJ

#endif//XJMUSIC_CRAFT_MACRO_MAIN_CRAFT_H