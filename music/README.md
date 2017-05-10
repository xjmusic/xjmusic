## Music theory models in Java

Then, to calculate the note pitch classes for a specified **Chord**:

    $ music-theory chord "Cm nondominant -5 679"
    
    root: C
    tones:
      3: D#
      6: A
      7: A#
      9: D

To list the names of all the known chord-building rules:

    $ music-theory chords
    
    - Basic
    - Nondominant
    - Major Triad
    - Minor Triad
    - Augmented Triad
    - Diminished Triad
    - Suspended Triad
    - Omit Fifth
    - Flat Fifth
    - Add Sixth
    - Augmented Sixth
    - Omit Sixth
    - Add Seventh
    - Dominant Seventh
    - Major Seventh
    - Minor Seventh
    - Diminished Seventh
    - Half Diminished Seventh
    - Diminished Major Seventh
    - Augmented Major Seventh
    - Augmented Minor Seventh
    - Harmonic Seventh
    - Omit Seventh
    - Add Ninth
    - Dominant Ninth
    - Major Ninth
    - Minor Ninth
    - Sharp Ninth
    - Omit Ninth
    - Add Eleventh
    - Dominant Eleventh
    - Major Eleventh
    - Minor Eleventh
    - Omit Eleventh
    - Add Thirteenth
    - Dominant Thirteenth
    - Major Thirteenth
    - Minor Thirteenth

To calculate the note pitch classes for a specified **Scale**:

    $ music-theory scale "C aug"
    
    root: C
    tones:
      1: C
      2: D#
      3: E
      4: G
      5: G#
      6: B

To list the names of all the known scale-building rules:

    $ music-theory scales
    
    - Default (Major)
    - Minor
    - Major
    - Natural Minor
    - Diminished
    - Augmented
    - Melodic Minor Ascend
    - Melodic Minor Descend
    - Harmonic Minor
    - Ionian
    - Dorian
    - Phrygian
    - Lydian
    - Mixolydian
    - Aeolian
    - Locrian

To determine a key:

    $ music-theory key Db
    
    root: Db
    mode: Major
    relative:
      root: Bb
      mode: Minor

## Note

A Note is used to represent the relative duration and pitch of a sound.

## Key

The key of a piece is a group of pitches, or scale upon which a music composition is created in classical, Western art, and Western pop music.

## Chord

In music theory, a chord is any harmonic set of three or more notes that is heard as if sounding simultaneously.

## Scale

In music theory, a scale is any set of musical notes ordered by fundamental frequency or pitch.
