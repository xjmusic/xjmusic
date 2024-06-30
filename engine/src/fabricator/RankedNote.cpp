// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include "xjmusic/fabricator/RankedNote.h"

using namespace XJ;


RankedNote::RankedNote(
    const Note note,
    const int delta
) {
  this->note = note;
  this->delta = delta;
}


Note RankedNote::getTones() const {
  return note;
}


int RankedNote::getDelta() const {
  return delta;
}
