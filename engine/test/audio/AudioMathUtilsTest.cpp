// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <gtest/gtest.h>

#include "xjmusic/audio/AudioMathUtils.h"

class AudioMathUtilsTest : public ::testing::Test {
protected:
    XJ::InstrumentAudio audio;
    int intensityLayers = 2;
    float intensityThreshold = 0.5f;
};

TEST_F(AudioMathUtilsTest, computeIntensityAmplitude) {
    audio.intensity = 0.5f;

    EXPECT_FLOAT_EQ(0.0f, XJ::AudioMathUtils::computeIntensityAmplitude(&audio, intensityLayers, intensityThreshold, true, 0.000f));
    EXPECT_FLOAT_EQ(0.5f, XJ::AudioMathUtils::computeIntensityAmplitude(&audio, intensityLayers, intensityThreshold, true, 0.125f));
    EXPECT_FLOAT_EQ(1.0f, XJ::AudioMathUtils::computeIntensityAmplitude(&audio, intensityLayers, intensityThreshold, true, 0.250f));
    EXPECT_FLOAT_EQ(1.0f, XJ::AudioMathUtils::computeIntensityAmplitude(&audio, intensityLayers, intensityThreshold, true, 0.500f));
    EXPECT_FLOAT_EQ(1.0f, XJ::AudioMathUtils::computeIntensityAmplitude(&audio, intensityLayers, intensityThreshold, true, 0.750f));
    EXPECT_FLOAT_EQ(0.5f, XJ::AudioMathUtils::computeIntensityAmplitude(&audio, intensityLayers, intensityThreshold, true, 0.875f));
    EXPECT_FLOAT_EQ(0.0f, XJ::AudioMathUtils::computeIntensityAmplitude(&audio, intensityLayers, intensityThreshold, true, 1.000f));
    EXPECT_FLOAT_EQ(0.0f, XJ::AudioMathUtils::computeIntensityAmplitude(&audio, intensityLayers, intensityThreshold, false, 0.000f));
    EXPECT_FLOAT_EQ(0.5f, XJ::AudioMathUtils::computeIntensityAmplitude(&audio, intensityLayers, intensityThreshold, false, 0.125f));
    EXPECT_FLOAT_EQ(1.0f, XJ::AudioMathUtils::computeIntensityAmplitude(&audio, intensityLayers, intensityThreshold, false, 0.250f));
    EXPECT_FLOAT_EQ(1.0f, XJ::AudioMathUtils::computeIntensityAmplitude(&audio, intensityLayers, intensityThreshold, false, 0.500f));
    EXPECT_FLOAT_EQ(1.0f, XJ::AudioMathUtils::computeIntensityAmplitude(&audio, intensityLayers, intensityThreshold, false, 0.750f));
    EXPECT_FLOAT_EQ(1.0f, XJ::AudioMathUtils::computeIntensityAmplitude(&audio, intensityLayers, intensityThreshold, false, 0.875f));
    EXPECT_FLOAT_EQ(1.0f, XJ::AudioMathUtils::computeIntensityAmplitude(&audio, intensityLayers, intensityThreshold, false, 1.000f));
}

TEST_F(AudioMathUtilsTest, computeIntensityAmplitude_singleIntensityLayerIgnoresEverything) {
    EXPECT_FLOAT_EQ(1.0f, XJ::AudioMathUtils::computeIntensityAmplitude(nullptr, 1, 0, false, 0));
}