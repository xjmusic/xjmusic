+++
tags = ["Art","Synthesis","Algorithms","Open Source","Music"]
date = "1996-01-01"
description = "A platform for audio synthesis and algorithmic composition, used by musicians, artists, and researchers working with sound. It is free and open source software available for Windows, macOS, and Linux."
draft = false
aliases = []
title = "SuperCollider"
authors = ["Open Source", "GNU General Public License"]
external_url = "http://supercollider.github.io/"
publication = "James McCartney"
+++

SuperCollider features three major components:

  * scsynth, a real-time audio server, forms the core of the platform. It features 400+ unit generators ("UGens") for analysis, synthesis, and processing. Its granularity allows the fluid combination of many known and unknown audio techniques, moving between additive and subtractive synthesis, FM, granular synthesis, FFT, and physical modelling. You can write your own UGens in C++, and users have already contributed several hundred more to the sc3-plugins repository.
  * sclang, an interpreted programming language. It is focused on sound, but not limited to any specific domain. sclang controls scsynth via Open Sound Control. You can use it for algorithmic composition and sequencing, finding new sound synthesis methods, connecting your app to external hardware including MIDI controllers, network music, writing GUIs and visual displays, or for your daily programming experiments. It has a stock of user-contributed extensions called Quarks.
  * scide is an editor for sclang with an integrated help system.
