// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.mixer.impl.audio;

import io.xj.mixer.impl.exception.FormatException;

import org.junit.Test;

import javax.sound.sampled.AudioFormat;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class AudioSampleTest {

  @Test
  public void typeOf_U8() throws Exception {
    assertEquals(AudioSample.U8, AudioSample.typeOfInput(
      new AudioFormat(AudioFormat.Encoding.PCM_UNSIGNED,
        4000, 8, 1, 1, 4000, false)));
  }

  @Test(expected = FormatException.class)
  public void typeOf_U8_unsupportedForOutput() throws Exception {
    assertEquals(AudioSample.U8, AudioSample.typeOfOutput(
      new AudioFormat(AudioFormat.Encoding.PCM_UNSIGNED,
        4000, 8, 1, 1, 4000, false)));
  }

  @Test
  public void typeOf_S8() throws Exception {
    assertEquals(AudioSample.S8, AudioSample.typeOfOutput(
      new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
        4000, 8, 1, 1, 4000, false)));
  }

  @Test
  public void typeOf_U16LSB() throws Exception {
    assertEquals(AudioSample.U16LSB, AudioSample.typeOfInput(
      new AudioFormat(AudioFormat.Encoding.PCM_UNSIGNED,
        22000, 16, 2, 4, 22000, false)));
  }

  @Test(expected = FormatException.class)
  public void typeOf_U16LSB_unsupportedForOutput() throws Exception {
    assertEquals(AudioSample.U16LSB, AudioSample.typeOfOutput(
      new AudioFormat(AudioFormat.Encoding.PCM_UNSIGNED,
        22000, 16, 2, 4, 22000, false)));
  }

  @Test
  public void typeOf_S16LSB() throws Exception {
    assertEquals(AudioSample.S16LSB, AudioSample.typeOfOutput(
      new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
        22000, 16, 2, 4, 22000, false)));
  }

  @Test
  public void typeOf_S32LSB() throws Exception {
    assertEquals(AudioSample.S32LSB, AudioSample.typeOfOutput(
      new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
        44100, 32, 2, 8, 44100, false)));
  }

  @Test
  public void typeOf_F32LSB() throws Exception {
    assertEquals(AudioSample.F32LSB, AudioSample.typeOfOutput(
      new AudioFormat(AudioFormat.Encoding.PCM_FLOAT,
        48000, 32, 2, 8, 48000, false)));
  }

  @Test
  public void typeOf_F64LSB() throws Exception {
    assertEquals(AudioSample.F64LSB, AudioSample.typeOfOutput(
      new AudioFormat(AudioFormat.Encoding.PCM_FLOAT,
        48000, 64, 2, 16, 48000, false)));
  }

  @Test
  public void typeOf_U16MSB() throws Exception {
    assertEquals(AudioSample.U16MSB, AudioSample.typeOfInput(
      new AudioFormat(AudioFormat.Encoding.PCM_UNSIGNED,
        22000, 16, 2, 4, 22000, true)));
  }

  @Test(expected = FormatException.class)
  public void typeOf_U16MSB_unsupportedForOutput() throws Exception {
    assertEquals(AudioSample.U16MSB, AudioSample.typeOfOutput(
      new AudioFormat(AudioFormat.Encoding.PCM_UNSIGNED,
        22000, 16, 2, 4, 22000, true)));
  }

  @Test
  public void typeOf_S16MSB() throws Exception {
    assertEquals(AudioSample.S16MSB, AudioSample.typeOfOutput(
      new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
        22000, 16, 2, 4, 22000, true)));
  }

  @Test
  public void typeOf_S32MSB() throws Exception {
    assertEquals(AudioSample.S32MSB, AudioSample.typeOfOutput(
      new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
        44100, 32, 2, 8, 44100, true)));
  }

  @Test
  public void typeOf_F32MSB() throws Exception {
    assertEquals(AudioSample.F32MSB, AudioSample.typeOfOutput(
      new AudioFormat(AudioFormat.Encoding.PCM_FLOAT,
        48000, 32, 2, 8, 48000, true)));
  }

  @Test
  public void typeOf_F64MSB() throws Exception {
    assertEquals(AudioSample.F64MSB, AudioSample.typeOfOutput(
      new AudioFormat(AudioFormat.Encoding.PCM_FLOAT,
        48000, 64, 2, 16, 48000, true)));
  }


  @Test
  public void ofBytes_U8() throws Exception {
    assertEquals(0.015625, AudioSample.fromBytes(new byte[]{-126}, AudioSample.U8), 0);
    assertEquals(0.0234375, AudioSample.fromBytes(new byte[]{-125}, AudioSample.U8), 0);
    assertEquals(0.34375, AudioSample.fromBytes(new byte[]{-84}, AudioSample.U8), 0);
    assertEquals(0.6171875, AudioSample.fromBytes(new byte[]{-49}, AudioSample.U8), 0);
  }

  @Test
  public void toBytes_S8() throws Exception {
    assertArrayEquals(new byte[]{1}, AudioSample.toBytes(0.014007995849482712, AudioSample.S8));
    assertArrayEquals(new byte[]{1}, AudioSample.toBytes(0.01425214392529069, AudioSample.S8)); // note collision with preceding test value, due to reduction of resolution from 64-bit float to 8-bit int
    assertArrayEquals(new byte[]{44}, AudioSample.toBytes(0.34404736472670675, AudioSample.S8));
    assertArrayEquals(new byte[]{78}, AudioSample.toBytes(0.614740440076906645, AudioSample.S8));
  }

  @Test
  public void ofBytes_S8() throws Exception {
    assertEquals(-0.984375, AudioSample.fromBytes(new byte[]{-126}, AudioSample.S8), 0);
    assertEquals(-0.9765625, AudioSample.fromBytes(new byte[]{-125}, AudioSample.S8), 0);
    assertEquals(0.09375, AudioSample.fromBytes(new byte[]{12}, AudioSample.S8), 0);
    assertEquals(0.4609375, AudioSample.fromBytes(new byte[]{59}, AudioSample.S8), 0);
  }

  @Test
  public void ofBytes_U16LSB() throws Exception {
    assertEquals(0.014007568359375, AudioSample.fromBytes(new byte[]{-53, -127}, AudioSample.U16LSB), 0);
    assertEquals(0.014251708984375, AudioSample.fromBytes(new byte[]{-45, -127}, AudioSample.U16LSB), 0);
    assertEquals(-0.985595703125, AudioSample.fromBytes(new byte[]{-40, 1}, AudioSample.U16LSB), 0);
    assertEquals(-0.985198974609375, AudioSample.fromBytes(new byte[]{-27, 1}, AudioSample.U16LSB), 0);
  }

  @Test
  public void toBytes_S16LSB() throws Exception {
    assertArrayEquals(new byte[]{-53, 1}, AudioSample.toBytes(0.014007995849482712, AudioSample.S16LSB));
    assertArrayEquals(new byte[]{-45, 1}, AudioSample.toBytes(0.01425214392529069, AudioSample.S16LSB));
    assertArrayEquals(new byte[]{-40, 1}, AudioSample.toBytes(0.014404736472670675, AudioSample.S16LSB));
    assertArrayEquals(new byte[]{-29, 1}, AudioSample.toBytes(0.014740440076906645, AudioSample.S16LSB));
    assertArrayEquals(new byte[]{0, -128}, AudioSample.toBytes(-1, AudioSample.S16LSB));
    assertArrayEquals(new byte[]{-1, 127}, AudioSample.toBytes(0.999969482421875, AudioSample.S16LSB));
  }

  @Test
  public void ofBytes_S16LSB() throws Exception {
    assertEquals(0.014007568359375, AudioSample.fromBytes(new byte[]{-53, 1}, AudioSample.S16LSB), 0);
    assertEquals(0.014251708984375, AudioSample.fromBytes(new byte[]{-45, 1}, AudioSample.S16LSB), 0);
    assertEquals(0.014404296875, AudioSample.fromBytes(new byte[]{-40, 1}, AudioSample.S16LSB), 0);
    assertEquals(0.014739990234375, AudioSample.fromBytes(new byte[]{-29, 1}, AudioSample.S16LSB), 0);
    assertEquals(-1.0, AudioSample.fromBytes(new byte[]{0, -128}, AudioSample.S16LSB), 0);
    assertEquals(0.999969482421875, AudioSample.fromBytes(new byte[]{-1, 127}, AudioSample.S16LSB), 0);
  }

  @Test
  public void toBytes_S32LSB() throws Exception {
    assertArrayEquals(new byte[]{106, -4, 52, -2}, AudioSample.toBytes(0.014007995849482712, AudioSample.S32LSB));
    assertArrayEquals(new byte[]{90, -4, 44, -2}, AudioSample.toBytes(0.01425214392529069, AudioSample.S32LSB));
    assertArrayEquals(new byte[]{29, -55, -12, -54}, AudioSample.toBytes(0.414404736472670675, AudioSample.S32LSB));
    assertArrayEquals(new byte[]{95, -99, 124, 91}, AudioSample.toBytes(-0.714740440076906645, AudioSample.S32LSB));
    assertArrayEquals(new byte[]{0, 0, 0, -128}, AudioSample.toBytes(1, AudioSample.S32LSB));
    assertArrayEquals(new byte[]{-1, -1, -1, 127}, AudioSample.toBytes(-1, AudioSample.S32LSB));
  }

  @Test
  public void ofBytes_S32LSB() throws Exception {
    assertEquals(0.014007995836436749, AudioSample.fromBytes(new byte[]{106, -4, 52, -2}, AudioSample.S32LSB), 0);
    assertEquals(0.014252143912017345, AudioSample.fromBytes(new byte[]{90, -4, 44, -2}, AudioSample.S32LSB), 0);
    assertEquals(0.014404736459255219, AudioSample.fromBytes(new byte[]{80, -4, 39, -2}, AudioSample.S32LSB), 0);
    assertEquals(0.01474044006317854, AudioSample.fromBytes(new byte[]{58, -4, 28, -2}, AudioSample.S32LSB), 0);
    assertEquals(1.0, AudioSample.fromBytes(new byte[]{0, 0, 0, -128}, AudioSample.S32LSB), 0);
    assertEquals(-0.9999999995343387, AudioSample.fromBytes(new byte[]{-1, -1, -1, 127}, AudioSample.S32LSB), 0);
  }

  @Test
  public void toBytes_F32LSB() throws Exception {
    assertArrayEquals(new byte[]{-53, -36, 75, -79}, AudioSample.toBytes(-2.9665893574863208E-9, AudioSample.F32LSB));
    assertArrayEquals(new byte[]{115, 14, 23, -79}, AudioSample.toBytes(-2.1981605675431764E-9, AudioSample.F32LSB));
    assertArrayEquals(new byte[]{-75, 22, 15, -78}, AudioSample.toBytes(-8.328858649346046E-9, AudioSample.F32LSB));
    assertArrayEquals(new byte[]{-74, -25, -48, -79}, AudioSample.toBytes(-6.079939134478311E-9, AudioSample.F32LSB));
    assertArrayEquals(new byte[]{0, 0, -128, -65}, AudioSample.toBytes(-1, AudioSample.F32LSB));
    assertArrayEquals(new byte[]{0, 0, 0, -65}, AudioSample.toBytes(-0.5, AudioSample.F32LSB));
    assertArrayEquals(new byte[]{0, 0, 0, 0}, AudioSample.toBytes(0, AudioSample.F32LSB));
    assertArrayEquals(new byte[]{0, 0, 0, 63}, AudioSample.toBytes(0.5, AudioSample.F32LSB));
    assertArrayEquals(new byte[]{0, 0, -128, 63}, AudioSample.toBytes(1, AudioSample.F32LSB));
  }

  @Test
  public void ofBytes_F32LSB() throws Exception {
    assertEquals(-9.720623347675428E-5, AudioSample.fromBytes(new byte[]{51, -37, -53, -72}, AudioSample.F32LSB), 0);
    assertEquals(-7.202712731668726E-5, AudioSample.fromBytes(new byte[]{69, 13, -105, -72}, AudioSample.F32LSB), 0);
    assertEquals(-2.7291171136312187E-4, AudioSample.fromBytes(new byte[]{-105, 21, -113, -71}, AudioSample.F32LSB), 0);
    assertEquals(-1.992213656194508E-4, AudioSample.fromBytes(new byte[]{20, -26, 80, -71}, AudioSample.F32LSB), 0);
    assertEquals(-1, AudioSample.fromBytes(new byte[]{0, 0, -128, -65}, AudioSample.F32LSB), 0);
    assertEquals(-0.5, AudioSample.fromBytes(new byte[]{0, 0, 0, -65}, AudioSample.F32LSB), 0);
    assertEquals(0, AudioSample.fromBytes(new byte[]{0, 0, 0, 0}, AudioSample.F32LSB), 0);
    assertEquals(0.5, AudioSample.fromBytes(new byte[]{0, 0, 0, 63}, AudioSample.F32LSB), 0);
    assertEquals(1, AudioSample.fromBytes(new byte[]{0, 0, -128, 63}, AudioSample.F32LSB), 0);
  }

  @Test
  public void toBytes_F64LSB() throws Exception {
    assertArrayEquals(new byte[]{101, -82, 50, 87, -103, 123, 41, -66}, AudioSample.toBytes(-2.9665893574863208E-9, AudioSample.F64LSB));
    assertArrayEquals(new byte[]{58, -57, -100, 99, -50, -31, 34, -66}, AudioSample.toBytes(-2.1981605675431764E-9, AudioSample.F64LSB));
    assertArrayEquals(new byte[]{91, 75, -83, -91, -42, -30, 65, -66}, AudioSample.toBytes(-8.328858649346046E-9, AudioSample.F64LSB));
    assertArrayEquals(new byte[]{-37, 115, -19, -71, -10, 28, 58, -66}, AudioSample.toBytes(-6.079939134478311E-9, AudioSample.F64LSB));
    assertArrayEquals(new byte[]{0, 0, 0, 0, 0, 0, -16, -65}, AudioSample.toBytes(-1, AudioSample.F64LSB));
    assertArrayEquals(new byte[]{0, 0, 0, 0, 0, 0, -32, -65}, AudioSample.toBytes(-0.5, AudioSample.F64LSB));
    assertArrayEquals(new byte[]{0, 0, 0, 0, 0, 0, 0, 0}, AudioSample.toBytes(0, AudioSample.F64LSB));
    assertArrayEquals(new byte[]{0, 0, 0, 0, 0, 0, -32, 63}, AudioSample.toBytes(0.5, AudioSample.F64LSB));
    assertArrayEquals(new byte[]{0, 0, 0, 0, 0, 0, -16, 63}, AudioSample.toBytes(1, AudioSample.F64LSB));
  }

  @Test
  public void ofBytes_F64LSB() throws Exception {
    assertEquals(-2.9665893574863208E-9, AudioSample.fromBytes(new byte[]{101, -82, 50, 87, -103, 123, 41, -66}, AudioSample.F64LSB), 0);
    assertEquals(-2.1981605675431764E-9, AudioSample.fromBytes(new byte[]{58, -57, -100, 99, -50, -31, 34, -66}, AudioSample.F64LSB), 0);
    assertEquals(-8.328858649346046E-9, AudioSample.fromBytes(new byte[]{91, 75, -83, -91, -42, -30, 65, -66}, AudioSample.F64LSB), 0);
    assertEquals(-6.079939134478311E-9, AudioSample.fromBytes(new byte[]{-37, 115, -19, -71, -10, 28, 58, -66}, AudioSample.F64LSB), 0);
    assertEquals(-1, AudioSample.fromBytes(new byte[]{0, 0, 0, 0, 0, 0, -16, -65}, AudioSample.F64LSB), 0);
    assertEquals(-0.5, AudioSample.fromBytes(new byte[]{0, 0, 0, 0, 0, 0, -32, -65}, AudioSample.F64LSB), 0);
    assertEquals(0, AudioSample.fromBytes(new byte[]{0, 0, 0, 0, 0, 0, 0, 0}, AudioSample.F64LSB), 0);
    assertEquals(0.5, AudioSample.fromBytes(new byte[]{0, 0, 0, 0, 0, 0, -32, 63}, AudioSample.F64LSB), 0);
    assertEquals(1, AudioSample.fromBytes(new byte[]{0, 0, 0, 0, 0, 0, -16, 63}, AudioSample.F64LSB), 0);
  }

  @Test
  public void ofBytes_U16MSB() throws Exception {
    assertEquals(0.014007568359375, AudioSample.fromBytes(new byte[]{-127, -53}, AudioSample.U16MSB), 0);
    assertEquals(0.014251708984375, AudioSample.fromBytes(new byte[]{-127, -45}, AudioSample.U16MSB), 0);
    assertEquals(-0.985595703125, AudioSample.fromBytes(new byte[]{1, -40}, AudioSample.U16MSB), 0);
    assertEquals(-0.985198974609375, AudioSample.fromBytes(new byte[]{1, -27}, AudioSample.U16MSB), 0);
  }

  @Test
  public void toBytes_S16MSB() throws Exception {
    assertArrayEquals(new byte[]{1, -53}, AudioSample.toBytes(0.014007995849482712, AudioSample.S16MSB));
    assertArrayEquals(new byte[]{1, -45}, AudioSample.toBytes(0.01425214392529069, AudioSample.S16MSB));
    assertArrayEquals(new byte[]{1, -40}, AudioSample.toBytes(0.014404736472670675, AudioSample.S16MSB));
    assertArrayEquals(new byte[]{1, -29}, AudioSample.toBytes(0.014740440076906645, AudioSample.S16MSB));
    assertArrayEquals(new byte[]{-128, 0}, AudioSample.toBytes(-1, AudioSample.S16MSB));
    assertArrayEquals(new byte[]{127, -1}, AudioSample.toBytes(0.999969482421875, AudioSample.S16MSB));
  }

  @Test
  public void ofBytes_S16MSB() throws Exception {
    assertEquals(0.014007568359375, AudioSample.fromBytes(new byte[]{1, -53}, AudioSample.S16MSB), 0);
    assertEquals(0.014251708984375, AudioSample.fromBytes(new byte[]{1, -45}, AudioSample.S16MSB), 0);
    assertEquals(0.014404296875, AudioSample.fromBytes(new byte[]{1, -40}, AudioSample.S16MSB), 0);
    assertEquals(0.014739990234375, AudioSample.fromBytes(new byte[]{1, -29}, AudioSample.S16MSB), 0);
    assertEquals(-1.0, AudioSample.fromBytes(new byte[]{-128, 0}, AudioSample.S16MSB), 0);
    assertEquals(0.999969482421875, AudioSample.fromBytes(new byte[]{127, -1}, AudioSample.S16MSB), 0);
  }

  @Test
  public void toBytes_S32MSB() throws Exception {
    assertArrayEquals(new byte[]{-2, 52, -4, 106}, AudioSample.toBytes(0.014007995849482712, AudioSample.S32MSB));
    assertArrayEquals(new byte[]{-2, 44, -4, 90}, AudioSample.toBytes(0.01425214392529069, AudioSample.S32MSB));
    assertArrayEquals(new byte[]{-54, -12, -55, 29}, AudioSample.toBytes(0.414404736472670675, AudioSample.S32MSB));
    assertArrayEquals(new byte[]{91, 124, -99, 95}, AudioSample.toBytes(-0.714740440076906645, AudioSample.S32MSB));
    assertArrayEquals(new byte[]{-128, 0, 0, 0}, AudioSample.toBytes(1, AudioSample.S32MSB));
    assertArrayEquals(new byte[]{127, -1, -1, -1}, AudioSample.toBytes(-1, AudioSample.S32MSB));
  }

  @Test
  public void ofBytes_S32MSB() throws Exception {
    assertEquals(0.014007995836436749, AudioSample.fromBytes(new byte[]{-2, 52, -4, 106}, AudioSample.S32MSB), 0);
    assertEquals(0.014252143912017345, AudioSample.fromBytes(new byte[]{-2, 44, -4, 90}, AudioSample.S32MSB), 0);
    assertEquals(0.014404736459255219, AudioSample.fromBytes(new byte[]{-2, 39, -4, 80}, AudioSample.S32MSB), 0);
    assertEquals(0.01474044006317854, AudioSample.fromBytes(new byte[]{-2, 28, -4, 58}, AudioSample.S32MSB), 0);
    assertEquals(1.0, AudioSample.fromBytes(new byte[]{-128, 0, 0, 0}, AudioSample.S32MSB), 0);
    assertEquals(-0.9999999995343387, AudioSample.fromBytes(new byte[]{127, -1, -1, -1}, AudioSample.S32MSB), 0);
  }

  @Test
  public void toBytes_F32MSB() throws Exception {
    assertArrayEquals(new byte[]{-79, 75, -36, -53}, AudioSample.toBytes(-2.9665893574863208E-9, AudioSample.F32MSB));
    assertArrayEquals(new byte[]{-79, 23, 14, 115}, AudioSample.toBytes(-2.1981605675431764E-9, AudioSample.F32MSB));
    assertArrayEquals(new byte[]{-78, 15, 22, -75}, AudioSample.toBytes(-8.328858649346046E-9, AudioSample.F32MSB));
    assertArrayEquals(new byte[]{-79, -48, -25, -74}, AudioSample.toBytes(-6.079939134478311E-9, AudioSample.F32MSB));
    assertArrayEquals(new byte[]{-65, -128, 0, 0}, AudioSample.toBytes(-1, AudioSample.F32MSB));
    assertArrayEquals(new byte[]{-65, 0, 0, 0}, AudioSample.toBytes(-0.5, AudioSample.F32MSB));
    assertArrayEquals(new byte[]{0, 0, 0, 0}, AudioSample.toBytes(0, AudioSample.F32MSB));
    assertArrayEquals(new byte[]{63, 0, 0, 0}, AudioSample.toBytes(0.5, AudioSample.F32MSB));
    assertArrayEquals(new byte[]{63, -128, 0, 0}, AudioSample.toBytes(1, AudioSample.F32MSB));
  }

  @Test
  public void ofBytes_F32MSB() throws Exception {
    assertEquals(-9.720623347675428E-5, AudioSample.fromBytes(new byte[]{-72, -53, -37, 51}, AudioSample.F32MSB), 0);
    assertEquals(-7.202712731668726E-5, AudioSample.fromBytes(new byte[]{-72, -105, 13, 69}, AudioSample.F32MSB), 0);
    assertEquals(-2.7291171136312187E-4, AudioSample.fromBytes(new byte[]{-71, -113, 21, -105}, AudioSample.F32MSB), 0);
    assertEquals(-1.992213656194508E-4, AudioSample.fromBytes(new byte[]{-71, 80, -26, 20}, AudioSample.F32MSB), 0);
    assertEquals(-1, AudioSample.fromBytes(new byte[]{-65, -128, 0, 0}, AudioSample.F32MSB), 0);
    assertEquals(-0.5, AudioSample.fromBytes(new byte[]{-65, 0, 0, 0}, AudioSample.F32MSB), 0);
    assertEquals(0, AudioSample.fromBytes(new byte[]{0, 0, 0, 0}, AudioSample.F32MSB), 0);
    assertEquals(0.5, AudioSample.fromBytes(new byte[]{63, 0, 0, 0}, AudioSample.F32MSB), 0);
    assertEquals(1, AudioSample.fromBytes(new byte[]{63, -128, 0, 0}, AudioSample.F32MSB), 0);
  }

  @Test
  public void toBytes_F64MSB() throws Exception {
    assertArrayEquals(new byte[]{-66, 41, 123, -103, 87, 50, -82, 101}, AudioSample.toBytes(-2.9665893574863208E-9, AudioSample.F64MSB));
    assertArrayEquals(new byte[]{-66, 34, -31, -50, 99, -100, -57, 58}, AudioSample.toBytes(-2.1981605675431764E-9, AudioSample.F64MSB));
    assertArrayEquals(new byte[]{-66, 65, -30, -42, -91, -83, 75, 91}, AudioSample.toBytes(-8.328858649346046E-9, AudioSample.F64MSB));
    assertArrayEquals(new byte[]{-66, 58, 28, -10, -71, -19, 115, -37}, AudioSample.toBytes(-6.079939134478311E-9, AudioSample.F64MSB));
    assertArrayEquals(new byte[]{-65, -16, 0, 0, 0, 0, 0, 0}, AudioSample.toBytes(-1, AudioSample.F64MSB));
    assertArrayEquals(new byte[]{-65, -32, 0, 0, 0, 0, 0, 0}, AudioSample.toBytes(-0.5, AudioSample.F64MSB));
    assertArrayEquals(new byte[]{0, 0, 0, 0, 0, 0, 0, 0}, AudioSample.toBytes(0, AudioSample.F64MSB));
    assertArrayEquals(new byte[]{63, -32, 0, 0, 0, 0, 0, 0}, AudioSample.toBytes(0.5, AudioSample.F64MSB));
    assertArrayEquals(new byte[]{63, -16, 0, 0, 0, 0, 0, 0}, AudioSample.toBytes(1, AudioSample.F64MSB));
  }

  @Test
  public void ofBytes_F64MSB() throws Exception {
    assertEquals(-2.9665893574863208E-9, AudioSample.fromBytes(new byte[]{-66, 41, 123, -103, 87, 50, -82, 101}, AudioSample.F64MSB), 0);
    assertEquals(-2.1981605675431764E-9, AudioSample.fromBytes(new byte[]{-66, 34, -31, -50, 99, -100, -57, 58}, AudioSample.F64MSB), 0);
    assertEquals(-8.328858649346046E-9, AudioSample.fromBytes(new byte[]{-66, 65, -30, -42, -91, -83, 75, 91}, AudioSample.F64MSB), 0);
    assertEquals(-6.079939134478311E-9, AudioSample.fromBytes(new byte[]{-66, 58, 28, -10, -71, -19, 115, -37}, AudioSample.F64MSB), 0);
    assertEquals(-1, AudioSample.fromBytes(new byte[]{-65, -16, 0, 0, 0, 0, 0, 0}, AudioSample.F64MSB), 0);
    assertEquals(-0.5, AudioSample.fromBytes(new byte[]{-65, -32, 0, 0, 0, 0, 0, 0}, AudioSample.F64MSB), 0);
    assertEquals(0, AudioSample.fromBytes(new byte[]{0, 0, 0, 0, 0, 0, 0, 0}, AudioSample.F64MSB), 0);
    assertEquals(0.5, AudioSample.fromBytes(new byte[]{63, -32, 0, 0, 0, 0, 0, 0}, AudioSample.F64MSB), 0);
    assertEquals(1, AudioSample.fromBytes(new byte[]{63, -16, 0, 0, 0, 0, 0, 0}, AudioSample.F64MSB), 0);
  }

}
