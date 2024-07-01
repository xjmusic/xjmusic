// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef XJMUSIC_WORK_H
#define XJMUSIC_WORK_H

namespace XJ {

  class Work {
  public:
    virtual ~Work() = default;

  private:
    /**
     Stop work
     */
    virtual void finish();

    /**
     Check whether the craft work is finished

     @return true if finished (not running)
     */
    virtual bool isFinished();
  };

}// namespace XJ

#endif// XJMUSIC_WORK_H