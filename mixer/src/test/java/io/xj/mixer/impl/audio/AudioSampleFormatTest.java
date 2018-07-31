// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.mixer.impl.audio;

import io.xj.mixer.impl.exception.FormatException;

import org.junit.Test;

import javax.sound.sampled.AudioFormat;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class AudioSampleFormatTest {

  @Test
  public void typeOf_U8() throws Exception {
    assertEquals(AudioSampleFormat.U8, AudioSampleFormat.typeOfInput(
      new AudioFormat(AudioFormat.Encoding.PCM_UNSIGNED,
        4000, 8, 1, 1, 4000, false)));
  }

  @Test(expected = FormatException.class)
  public void typeOf_U8_unsupportedForOutput() throws Exception {
    assertEquals(AudioSampleFormat.U8, AudioSampleFormat.typeOfOutput(
      new AudioFormat(AudioFormat.Encoding.PCM_UNSIGNED,
        4000, 8, 1, 1, 4000, false)));
  }

  @Test
  public void typeOf_S8() throws Exception {
    assertEquals(AudioSampleFormat.S8, AudioSampleFormat.typeOfOutput(
      new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
        4000, 8, 1, 1, 4000, false)));
  }

  @Test
  public void typeOf_U16LSB() throws Exception {
    assertEquals(AudioSampleFormat.U16LSB, AudioSampleFormat.typeOfInput(
      new AudioFormat(AudioFormat.Encoding.PCM_UNSIGNED,
        22000, 16, 2, 4, 22000, false)));
  }

  @Test(expected = FormatException.class)
  public void typeOf_U16LSB_unsupportedForOutput() throws Exception {
    assertEquals(AudioSampleFormat.U16LSB, AudioSampleFormat.typeOfOutput(
      new AudioFormat(AudioFormat.Encoding.PCM_UNSIGNED,
        22000, 16, 2, 4, 22000, false)));
  }

  @Test
  public void typeOf_S16LSB() throws Exception {
    assertEquals(AudioSampleFormat.S16LSB, AudioSampleFormat.typeOfOutput(
      new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
        22000, 16, 2, 4, 22000, false)));
  }

  @Test
  public void typeOf_S32LSB() throws Exception {
    assertEquals(AudioSampleFormat.S32LSB, AudioSampleFormat.typeOfOutput(
      new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
        44100, 32, 2, 8, 44100, false)));
  }

  @Test
  public void typeOf_F32LSB() throws Exception {
    assertEquals(AudioSampleFormat.F32LSB, AudioSampleFormat.typeOfOutput(
      new AudioFormat(AudioFormat.Encoding.PCM_FLOAT,
        48000, 32, 2, 8, 48000, false)));
  }

  @Test
  public void typeOf_F64LSB() throws Exception {
    assertEquals(AudioSampleFormat.F64LSB, AudioSampleFormat.typeOfOutput(
      new AudioFormat(AudioFormat.Encoding.PCM_FLOAT,
        48000, 64, 2, 16, 48000, false)));
  }

  @Test
  public void typeOf_U16MSB() throws Exception {
    assertEquals(AudioSampleFormat.U16MSB, AudioSampleFormat.typeOfInput(
      new AudioFormat(AudioFormat.Encoding.PCM_UNSIGNED,
        22000, 16, 2, 4, 22000, true)));
  }

  @Test(expected = FormatException.class)
  public void typeOf_U16MSB_unsupportedForOutput() throws Exception {
    assertEquals(AudioSampleFormat.U16MSB, AudioSampleFormat.typeOfOutput(
      new AudioFormat(AudioFormat.Encoding.PCM_UNSIGNED,
        22000, 16, 2, 4, 22000, true)));
  }

  @Test
  public void typeOf_S16MSB() throws Exception {
    assertEquals(AudioSampleFormat.S16MSB, AudioSampleFormat.typeOfOutput(
      new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
        22000, 16, 2, 4, 22000, true)));
  }

  @Test
  public void typeOf_S32MSB() throws Exception {
    assertEquals(AudioSampleFormat.S32MSB, AudioSampleFormat.typeOfOutput(
      new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
        44100, 32, 2, 8, 44100, true)));
  }

  @Test
  public void typeOf_F32MSB() throws Exception {
    assertEquals(AudioSampleFormat.F32MSB, AudioSampleFormat.typeOfOutput(
      new AudioFormat(AudioFormat.Encoding.PCM_FLOAT,
        48000, 32, 2, 8, 48000, true)));
  }

  @Test
  public void typeOf_F64MSB() throws Exception {
    assertEquals(AudioSampleFormat.F64MSB, AudioSampleFormat.typeOfOutput(
      new AudioFormat(AudioFormat.Encoding.PCM_FLOAT,
        48000, 64, 2, 16, 48000, true)));
  }


  @Test
  public void ofBytes_U8() {
    assertEquals(0.015625, AudioSampleFormat.fromBytes(new byte[]{-126}, AudioSampleFormat.U8), 0);
    assertEquals(0.0234375, AudioSampleFormat.fromBytes(new byte[]{-125}, AudioSampleFormat.U8), 0);
    assertEquals(0.34375, AudioSampleFormat.fromBytes(new byte[]{-84}, AudioSampleFormat.U8), 0);
    assertEquals(0.6171875, AudioSampleFormat.fromBytes(new byte[]{-49}, AudioSampleFormat.U8), 0);
  }

  @Test
  public void toBytes_S8() {
    assertArrayEquals(new byte[]{1}, AudioSampleFormat.toBytes(0.014007995849482712, AudioSampleFormat.S8));
    assertArrayEquals(new byte[]{1}, AudioSampleFormat.toBytes(0.01425214392529069, AudioSampleFormat.S8)); // note collision with preceding test value, due to reduction of resolution from 64-bit float to 8-bit int
    assertArrayEquals(new byte[]{44}, AudioSampleFormat.toBytes(0.34404736472670675, AudioSampleFormat.S8));
    assertArrayEquals(new byte[]{78}, AudioSampleFormat.toBytes(0.614740440076906645, AudioSampleFormat.S8));
  }

  @Test
  public void ofBytes_S8() {
    assertEquals(-0.984375, AudioSampleFormat.fromBytes(new byte[]{-126}, AudioSampleFormat.S8), 0);
    assertEquals(-0.9765625, AudioSampleFormat.fromBytes(new byte[]{-125}, AudioSampleFormat.S8), 0);
    assertEquals(0.09375, AudioSampleFormat.fromBytes(new byte[]{12}, AudioSampleFormat.S8), 0);
    assertEquals(0.4609375, AudioSampleFormat.fromBytes(new byte[]{59}, AudioSampleFormat.S8), 0);
  }

  @Test
  public void ofBytes_U16LSB() {
    assertEquals(0.014007568359375, AudioSampleFormat.fromBytes(new byte[]{-53, -127}, AudioSampleFormat.U16LSB), 0);
    assertEquals(0.014251708984375, AudioSampleFormat.fromBytes(new byte[]{-45, -127}, AudioSampleFormat.U16LSB), 0);
    assertEquals(-0.985595703125, AudioSampleFormat.fromBytes(new byte[]{-40, 1}, AudioSampleFormat.U16LSB), 0);
    assertEquals(-0.985198974609375, AudioSampleFormat.fromBytes(new byte[]{-27, 1}, AudioSampleFormat.U16LSB), 0);
  }

  @Test
  public void toBytes_S16LSB() {
    assertArrayEquals(new byte[]{-53, 1}, AudioSampleFormat.toBytes(0.014007995849482712, AudioSampleFormat.S16LSB));
    assertArrayEquals(new byte[]{-45, 1}, AudioSampleFormat.toBytes(0.01425214392529069, AudioSampleFormat.S16LSB));
    assertArrayEquals(new byte[]{-40, 1}, AudioSampleFormat.toBytes(0.014404736472670675, AudioSampleFormat.S16LSB));
    assertArrayEquals(new byte[]{-29, 1}, AudioSampleFormat.toBytes(0.014740440076906645, AudioSampleFormat.S16LSB));
    assertArrayEquals(new byte[]{0, -128}, AudioSampleFormat.toBytes(-1, AudioSampleFormat.S16LSB));
    assertArrayEquals(new byte[]{-1, 127}, AudioSampleFormat.toBytes(0.999969482421875, AudioSampleFormat.S16LSB));
  }

  @Test
  public void ofBytes_S16LSB() {
    assertEquals(0.014007568359375, AudioSampleFormat.fromBytes(new byte[]{-53, 1}, AudioSampleFormat.S16LSB), 0);
    assertEquals(0.014251708984375, AudioSampleFormat.fromBytes(new byte[]{-45, 1}, AudioSampleFormat.S16LSB), 0);
    assertEquals(0.014404296875, AudioSampleFormat.fromBytes(new byte[]{-40, 1}, AudioSampleFormat.S16LSB), 0);
    assertEquals(0.014739990234375, AudioSampleFormat.fromBytes(new byte[]{-29, 1}, AudioSampleFormat.S16LSB), 0);
    assertEquals(-1.0, AudioSampleFormat.fromBytes(new byte[]{0, -128}, AudioSampleFormat.S16LSB), 0);
    assertEquals(0.999969482421875, AudioSampleFormat.fromBytes(new byte[]{-1, 127}, AudioSampleFormat.S16LSB), 0);
  }

  @Test
  public void toBytes_S32LSB() {
    assertArrayEquals(new byte[]{106, -4, 52, -2}, AudioSampleFormat.toBytes(0.014007995849482712, AudioSampleFormat.S32LSB));
    assertArrayEquals(new byte[]{90, -4, 44, -2}, AudioSampleFormat.toBytes(0.01425214392529069, AudioSampleFormat.S32LSB));
    assertArrayEquals(new byte[]{29, -55, -12, -54}, AudioSampleFormat.toBytes(0.414404736472670675, AudioSampleFormat.S32LSB));
    assertArrayEquals(new byte[]{95, -99, 124, 91}, AudioSampleFormat.toBytes(-0.714740440076906645, AudioSampleFormat.S32LSB));
    assertArrayEquals(new byte[]{0, 0, 0, -128}, AudioSampleFormat.toBytes(1, AudioSampleFormat.S32LSB));
    assertArrayEquals(new byte[]{-1, -1, -1, 127}, AudioSampleFormat.toBytes(-1, AudioSampleFormat.S32LSB));
  }

  @Test
  public void ofBytes_S32LSB() {
    assertEquals(0.014007995836436749, AudioSampleFormat.fromBytes(new byte[]{106, -4, 52, -2}, AudioSampleFormat.S32LSB), 0);
    assertEquals(0.014252143912017345, AudioSampleFormat.fromBytes(new byte[]{90, -4, 44, -2}, AudioSampleFormat.S32LSB), 0);
    assertEquals(0.014404736459255219, AudioSampleFormat.fromBytes(new byte[]{80, -4, 39, -2}, AudioSampleFormat.S32LSB), 0);
    assertEquals(0.01474044006317854, AudioSampleFormat.fromBytes(new byte[]{58, -4, 28, -2}, AudioSampleFormat.S32LSB), 0);
    assertEquals(1.0, AudioSampleFormat.fromBytes(new byte[]{0, 0, 0, -128}, AudioSampleFormat.S32LSB), 0);
    assertEquals(-0.9999999995343387, AudioSampleFormat.fromBytes(new byte[]{-1, -1, -1, 127}, AudioSampleFormat.S32LSB), 0);
  }

  @Test
  public void toBytes_F32LSB() {
    assertArrayEquals(new byte[]{-53, -36, 75, -79}, AudioSampleFormat.toBytes(-2.9665893574863208E-9, AudioSampleFormat.F32LSB));
    assertArrayEquals(new byte[]{115, 14, 23, -79}, AudioSampleFormat.toBytes(-2.1981605675431764E-9, AudioSampleFormat.F32LSB));
    assertArrayEquals(new byte[]{-75, 22, 15, -78}, AudioSampleFormat.toBytes(-8.328858649346046E-9, AudioSampleFormat.F32LSB));
    assertArrayEquals(new byte[]{-74, -25, -48, -79}, AudioSampleFormat.toBytes(-6.079939134478311E-9, AudioSampleFormat.F32LSB));
    assertArrayEquals(new byte[]{0, 0, -128, -65}, AudioSampleFormat.toBytes(-1, AudioSampleFormat.F32LSB));
    assertArrayEquals(new byte[]{0, 0, 0, -65}, AudioSampleFormat.toBytes(-0.5, AudioSampleFormat.F32LSB));
    assertArrayEquals(new byte[]{0, 0, 0, 0}, AudioSampleFormat.toBytes(0, AudioSampleFormat.F32LSB));
    assertArrayEquals(new byte[]{0, 0, 0, 63}, AudioSampleFormat.toBytes(0.5, AudioSampleFormat.F32LSB));
    assertArrayEquals(new byte[]{0, 0, -128, 63}, AudioSampleFormat.toBytes(1, AudioSampleFormat.F32LSB));
  }

  @Test
  public void ofBytes_F32LSB() {
    assertEquals(-9.720623347675428E-5, AudioSampleFormat.fromBytes(new byte[]{51, -37, -53, -72}, AudioSampleFormat.F32LSB), 0);
    assertEquals(-7.202712731668726E-5, AudioSampleFormat.fromBytes(new byte[]{69, 13, -105, -72}, AudioSampleFormat.F32LSB), 0);
    assertEquals(-2.7291171136312187E-4, AudioSampleFormat.fromBytes(new byte[]{-105, 21, -113, -71}, AudioSampleFormat.F32LSB), 0);
    assertEquals(-1.992213656194508E-4, AudioSampleFormat.fromBytes(new byte[]{20, -26, 80, -71}, AudioSampleFormat.F32LSB), 0);
    assertEquals(-1, AudioSampleFormat.fromBytes(new byte[]{0, 0, -128, -65}, AudioSampleFormat.F32LSB), 0);
    assertEquals(-0.5, AudioSampleFormat.fromBytes(new byte[]{0, 0, 0, -65}, AudioSampleFormat.F32LSB), 0);
    assertEquals(0, AudioSampleFormat.fromBytes(new byte[]{0, 0, 0, 0}, AudioSampleFormat.F32LSB), 0);
    assertEquals(0.5, AudioSampleFormat.fromBytes(new byte[]{0, 0, 0, 63}, AudioSampleFormat.F32LSB), 0);
    assertEquals(1, AudioSampleFormat.fromBytes(new byte[]{0, 0, -128, 63}, AudioSampleFormat.F32LSB), 0);
  }

  @Test
  public void toBytes_F64LSB() {
    assertArrayEquals(new byte[]{101, -82, 50, 87, -103, 123, 41, -66}, AudioSampleFormat.toBytes(-2.9665893574863208E-9, AudioSampleFormat.F64LSB));
    assertArrayEquals(new byte[]{58, -57, -100, 99, -50, -31, 34, -66}, AudioSampleFormat.toBytes(-2.1981605675431764E-9, AudioSampleFormat.F64LSB));
    assertArrayEquals(new byte[]{91, 75, -83, -91, -42, -30, 65, -66}, AudioSampleFormat.toBytes(-8.328858649346046E-9, AudioSampleFormat.F64LSB));
    assertArrayEquals(new byte[]{-37, 115, -19, -71, -10, 28, 58, -66}, AudioSampleFormat.toBytes(-6.079939134478311E-9, AudioSampleFormat.F64LSB));
    assertArrayEquals(new byte[]{0, 0, 0, 0, 0, 0, -16, -65}, AudioSampleFormat.toBytes(-1, AudioSampleFormat.F64LSB));
    assertArrayEquals(new byte[]{0, 0, 0, 0, 0, 0, -32, -65}, AudioSampleFormat.toBytes(-0.5, AudioSampleFormat.F64LSB));
    assertArrayEquals(new byte[]{0, 0, 0, 0, 0, 0, 0, 0}, AudioSampleFormat.toBytes(0, AudioSampleFormat.F64LSB));
    assertArrayEquals(new byte[]{0, 0, 0, 0, 0, 0, -32, 63}, AudioSampleFormat.toBytes(0.5, AudioSampleFormat.F64LSB));
    assertArrayEquals(new byte[]{0, 0, 0, 0, 0, 0, -16, 63}, AudioSampleFormat.toBytes(1, AudioSampleFormat.F64LSB));
  }

  @Test
  public void ofBytes_F64LSB() {
    assertEquals(-2.9665893574863208E-9, AudioSampleFormat.fromBytes(new byte[]{101, -82, 50, 87, -103, 123, 41, -66}, AudioSampleFormat.F64LSB), 0);
    assertEquals(-2.1981605675431764E-9, AudioSampleFormat.fromBytes(new byte[]{58, -57, -100, 99, -50, -31, 34, -66}, AudioSampleFormat.F64LSB), 0);
    assertEquals(-8.328858649346046E-9, AudioSampleFormat.fromBytes(new byte[]{91, 75, -83, -91, -42, -30, 65, -66}, AudioSampleFormat.F64LSB), 0);
    assertEquals(-6.079939134478311E-9, AudioSampleFormat.fromBytes(new byte[]{-37, 115, -19, -71, -10, 28, 58, -66}, AudioSampleFormat.F64LSB), 0);
    assertEquals(-1, AudioSampleFormat.fromBytes(new byte[]{0, 0, 0, 0, 0, 0, -16, -65}, AudioSampleFormat.F64LSB), 0);
    assertEquals(-0.5, AudioSampleFormat.fromBytes(new byte[]{0, 0, 0, 0, 0, 0, -32, -65}, AudioSampleFormat.F64LSB), 0);
    assertEquals(0, AudioSampleFormat.fromBytes(new byte[]{0, 0, 0, 0, 0, 0, 0, 0}, AudioSampleFormat.F64LSB), 0);
    assertEquals(0.5, AudioSampleFormat.fromBytes(new byte[]{0, 0, 0, 0, 0, 0, -32, 63}, AudioSampleFormat.F64LSB), 0);
    assertEquals(1, AudioSampleFormat.fromBytes(new byte[]{0, 0, 0, 0, 0, 0, -16, 63}, AudioSampleFormat.F64LSB), 0);
  }

  @Test
  public void ofBytes_U16MSB() {
    assertEquals(0.014007568359375, AudioSampleFormat.fromBytes(new byte[]{-127, -53}, AudioSampleFormat.U16MSB), 0);
    assertEquals(0.014251708984375, AudioSampleFormat.fromBytes(new byte[]{-127, -45}, AudioSampleFormat.U16MSB), 0);
    assertEquals(-0.985595703125, AudioSampleFormat.fromBytes(new byte[]{1, -40}, AudioSampleFormat.U16MSB), 0);
    assertEquals(-0.985198974609375, AudioSampleFormat.fromBytes(new byte[]{1, -27}, AudioSampleFormat.U16MSB), 0);
  }

  @Test
  public void toBytes_S16MSB() {
    assertArrayEquals(new byte[]{1, -53}, AudioSampleFormat.toBytes(0.014007995849482712, AudioSampleFormat.S16MSB));
    assertArrayEquals(new byte[]{1, -45}, AudioSampleFormat.toBytes(0.01425214392529069, AudioSampleFormat.S16MSB));
    assertArrayEquals(new byte[]{1, -40}, AudioSampleFormat.toBytes(0.014404736472670675, AudioSampleFormat.S16MSB));
    assertArrayEquals(new byte[]{1, -29}, AudioSampleFormat.toBytes(0.014740440076906645, AudioSampleFormat.S16MSB));
    assertArrayEquals(new byte[]{-128, 0}, AudioSampleFormat.toBytes(-1, AudioSampleFormat.S16MSB));
    assertArrayEquals(new byte[]{127, -1}, AudioSampleFormat.toBytes(0.999969482421875, AudioSampleFormat.S16MSB));
  }

  @Test
  public void ofBytes_S16MSB() {
    assertEquals(0.014007568359375, AudioSampleFormat.fromBytes(new byte[]{1, -53}, AudioSampleFormat.S16MSB), 0);
    assertEquals(0.014251708984375, AudioSampleFormat.fromBytes(new byte[]{1, -45}, AudioSampleFormat.S16MSB), 0);
    assertEquals(0.014404296875, AudioSampleFormat.fromBytes(new byte[]{1, -40}, AudioSampleFormat.S16MSB), 0);
    assertEquals(0.014739990234375, AudioSampleFormat.fromBytes(new byte[]{1, -29}, AudioSampleFormat.S16MSB), 0);
    assertEquals(-1.0, AudioSampleFormat.fromBytes(new byte[]{-128, 0}, AudioSampleFormat.S16MSB), 0);
    assertEquals(0.999969482421875, AudioSampleFormat.fromBytes(new byte[]{127, -1}, AudioSampleFormat.S16MSB), 0);
  }

  @Test
  public void toBytes_S32MSB() {
    assertArrayEquals(new byte[]{-2, 52, -4, 106}, AudioSampleFormat.toBytes(0.014007995849482712, AudioSampleFormat.S32MSB));
    assertArrayEquals(new byte[]{-2, 44, -4, 90}, AudioSampleFormat.toBytes(0.01425214392529069, AudioSampleFormat.S32MSB));
    assertArrayEquals(new byte[]{-54, -12, -55, 29}, AudioSampleFormat.toBytes(0.414404736472670675, AudioSampleFormat.S32MSB));
    assertArrayEquals(new byte[]{91, 124, -99, 95}, AudioSampleFormat.toBytes(-0.714740440076906645, AudioSampleFormat.S32MSB));
    assertArrayEquals(new byte[]{-128, 0, 0, 0}, AudioSampleFormat.toBytes(1, AudioSampleFormat.S32MSB));
    assertArrayEquals(new byte[]{127, -1, -1, -1}, AudioSampleFormat.toBytes(-1, AudioSampleFormat.S32MSB));
  }

  @Test
  public void ofBytes_S32MSB() {
    assertEquals(0.014007995836436749, AudioSampleFormat.fromBytes(new byte[]{-2, 52, -4, 106}, AudioSampleFormat.S32MSB), 0);
    assertEquals(0.014252143912017345, AudioSampleFormat.fromBytes(new byte[]{-2, 44, -4, 90}, AudioSampleFormat.S32MSB), 0);
    assertEquals(0.014404736459255219, AudioSampleFormat.fromBytes(new byte[]{-2, 39, -4, 80}, AudioSampleFormat.S32MSB), 0);
    assertEquals(0.01474044006317854, AudioSampleFormat.fromBytes(new byte[]{-2, 28, -4, 58}, AudioSampleFormat.S32MSB), 0);
    assertEquals(1.0, AudioSampleFormat.fromBytes(new byte[]{-128, 0, 0, 0}, AudioSampleFormat.S32MSB), 0);
    assertEquals(-0.9999999995343387, AudioSampleFormat.fromBytes(new byte[]{127, -1, -1, -1}, AudioSampleFormat.S32MSB), 0);
  }

  @Test
  public void toBytes_F32MSB() {
    assertArrayEquals(new byte[]{-79, 75, -36, -53}, AudioSampleFormat.toBytes(-2.9665893574863208E-9, AudioSampleFormat.F32MSB));
    assertArrayEquals(new byte[]{-79, 23, 14, 115}, AudioSampleFormat.toBytes(-2.1981605675431764E-9, AudioSampleFormat.F32MSB));
    assertArrayEquals(new byte[]{-78, 15, 22, -75}, AudioSampleFormat.toBytes(-8.328858649346046E-9, AudioSampleFormat.F32MSB));
    assertArrayEquals(new byte[]{-79, -48, -25, -74}, AudioSampleFormat.toBytes(-6.079939134478311E-9, AudioSampleFormat.F32MSB));
    assertArrayEquals(new byte[]{-65, -128, 0, 0}, AudioSampleFormat.toBytes(-1, AudioSampleFormat.F32MSB));
    assertArrayEquals(new byte[]{-65, 0, 0, 0}, AudioSampleFormat.toBytes(-0.5, AudioSampleFormat.F32MSB));
    assertArrayEquals(new byte[]{0, 0, 0, 0}, AudioSampleFormat.toBytes(0, AudioSampleFormat.F32MSB));
    assertArrayEquals(new byte[]{63, 0, 0, 0}, AudioSampleFormat.toBytes(0.5, AudioSampleFormat.F32MSB));
    assertArrayEquals(new byte[]{63, -128, 0, 0}, AudioSampleFormat.toBytes(1, AudioSampleFormat.F32MSB));
  }

  @Test
  public void ofBytes_F32MSB() {
    assertEquals(-9.720623347675428E-5, AudioSampleFormat.fromBytes(new byte[]{-72, -53, -37, 51}, AudioSampleFormat.F32MSB), 0);
    assertEquals(-7.202712731668726E-5, AudioSampleFormat.fromBytes(new byte[]{-72, -105, 13, 69}, AudioSampleFormat.F32MSB), 0);
    assertEquals(-2.7291171136312187E-4, AudioSampleFormat.fromBytes(new byte[]{-71, -113, 21, -105}, AudioSampleFormat.F32MSB), 0);
    assertEquals(-1.992213656194508E-4, AudioSampleFormat.fromBytes(new byte[]{-71, 80, -26, 20}, AudioSampleFormat.F32MSB), 0);
    assertEquals(-1, AudioSampleFormat.fromBytes(new byte[]{-65, -128, 0, 0}, AudioSampleFormat.F32MSB), 0);
    assertEquals(-0.5, AudioSampleFormat.fromBytes(new byte[]{-65, 0, 0, 0}, AudioSampleFormat.F32MSB), 0);
    assertEquals(0, AudioSampleFormat.fromBytes(new byte[]{0, 0, 0, 0}, AudioSampleFormat.F32MSB), 0);
    assertEquals(0.5, AudioSampleFormat.fromBytes(new byte[]{63, 0, 0, 0}, AudioSampleFormat.F32MSB), 0);
    assertEquals(1, AudioSampleFormat.fromBytes(new byte[]{63, -128, 0, 0}, AudioSampleFormat.F32MSB), 0);
  }

  @Test
  public void toBytes_F64MSB() {
    assertArrayEquals(new byte[]{-66, 41, 123, -103, 87, 50, -82, 101}, AudioSampleFormat.toBytes(-2.9665893574863208E-9, AudioSampleFormat.F64MSB));
    assertArrayEquals(new byte[]{-66, 34, -31, -50, 99, -100, -57, 58}, AudioSampleFormat.toBytes(-2.1981605675431764E-9, AudioSampleFormat.F64MSB));
    assertArrayEquals(new byte[]{-66, 65, -30, -42, -91, -83, 75, 91}, AudioSampleFormat.toBytes(-8.328858649346046E-9, AudioSampleFormat.F64MSB));
    assertArrayEquals(new byte[]{-66, 58, 28, -10, -71, -19, 115, -37}, AudioSampleFormat.toBytes(-6.079939134478311E-9, AudioSampleFormat.F64MSB));
    assertArrayEquals(new byte[]{-65, -16, 0, 0, 0, 0, 0, 0}, AudioSampleFormat.toBytes(-1, AudioSampleFormat.F64MSB));
    assertArrayEquals(new byte[]{-65, -32, 0, 0, 0, 0, 0, 0}, AudioSampleFormat.toBytes(-0.5, AudioSampleFormat.F64MSB));
    assertArrayEquals(new byte[]{0, 0, 0, 0, 0, 0, 0, 0}, AudioSampleFormat.toBytes(0, AudioSampleFormat.F64MSB));
    assertArrayEquals(new byte[]{63, -32, 0, 0, 0, 0, 0, 0}, AudioSampleFormat.toBytes(0.5, AudioSampleFormat.F64MSB));
    assertArrayEquals(new byte[]{63, -16, 0, 0, 0, 0, 0, 0}, AudioSampleFormat.toBytes(1, AudioSampleFormat.F64MSB));
  }

  @Test
  public void ofBytes_F64MSB() {
    assertEquals(-2.9665893574863208E-9, AudioSampleFormat.fromBytes(new byte[]{-66, 41, 123, -103, 87, 50, -82, 101}, AudioSampleFormat.F64MSB), 0);
    assertEquals(-2.1981605675431764E-9, AudioSampleFormat.fromBytes(new byte[]{-66, 34, -31, -50, 99, -100, -57, 58}, AudioSampleFormat.F64MSB), 0);
    assertEquals(-8.328858649346046E-9, AudioSampleFormat.fromBytes(new byte[]{-66, 65, -30, -42, -91, -83, 75, 91}, AudioSampleFormat.F64MSB), 0);
    assertEquals(-6.079939134478311E-9, AudioSampleFormat.fromBytes(new byte[]{-66, 58, 28, -10, -71, -19, 115, -37}, AudioSampleFormat.F64MSB), 0);
    assertEquals(-1, AudioSampleFormat.fromBytes(new byte[]{-65, -16, 0, 0, 0, 0, 0, 0}, AudioSampleFormat.F64MSB), 0);
    assertEquals(-0.5, AudioSampleFormat.fromBytes(new byte[]{-65, -32, 0, 0, 0, 0, 0, 0}, AudioSampleFormat.F64MSB), 0);
    assertEquals(0, AudioSampleFormat.fromBytes(new byte[]{0, 0, 0, 0, 0, 0, 0, 0}, AudioSampleFormat.F64MSB), 0);
    assertEquals(0.5, AudioSampleFormat.fromBytes(new byte[]{63, -32, 0, 0, 0, 0, 0, 0}, AudioSampleFormat.F64MSB), 0);
    assertEquals(1, AudioSampleFormat.fromBytes(new byte[]{63, -16, 0, 0, 0, 0, 0, 0}, AudioSampleFormat.F64MSB), 0);
  }

}
