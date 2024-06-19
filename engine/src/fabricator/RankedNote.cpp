// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include "xjmusic/fabricator/RankedNote.h"

using namespace XJ;


RankedNote::RankedNote(
    Note note,
    int delta
) {
  this->note = note;
  this->delta = delta;
}


Note RankedNote::getTones() {
  return note;
}


int RankedNote::getDelta() {
  return delta;
}
