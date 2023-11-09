// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.mixer;

import javax.sound.sampled.AudioFormat;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 Utilities for converting back and forth between a `float` and various `byte[]` for different bit-rates
 a float is a single value for a channel of a frame of some audio.

 @link https://docs.oracle.com/javase/tutorial/java/nutsandbolts/datatypes.html */
@SuppressWarnings("CommentedOutCode")
public enum AudioSampleFormat {

  // sample types
  U8, // unsigned 8-bit integer
  S8, // signed 8-bit integer
  F32LSB, // 32-bit floating point, LSB order
  F32MSB, // 32-bit floating point, MSB order
  F64LSB, // 64-bit floating point, LSB order
  F64MSB, // 64-bit floating point, MSB order
  S16LSB, // 16-bit signed integer, LSB order
  S16MSB, // 16-bit signed integer, MSB order
  S24LSB, // 24-bit signed integer, LSB order
  S24MSB, // 24-bit signed integer, MSB order
  S32LSB, // 32-bit signed integer, LSB order
  S32MSB, // 32-bit signed integer, MSB order
  U16LSB, // 16-bit unsigned integer, LSB order
  U16MSB; // 16-bit unsigned integer, MSB order

  public static final int SIGNED_16BIT_MAX = 0x8000;
  public static final int SIGNED_24BIT_MAX = 0x800000;
  public static final int SIGNED_32BIT_MAX = 0x80000000;
  public static final int SIGNED_8BIT_MAX = 0x80;
  public static final int UNSIGNED_16BIT_MAX = 0xffff;
  public static final int UNSIGNED_8BIT_MAX = 0xff;
  // format encoding types
  static final AudioFormat.Encoding PCM_UNSIGNED = AudioFormat.Encoding.PCM_UNSIGNED;
  static final AudioFormat.Encoding PCM_SIGNED = AudioFormat.Encoding.PCM_SIGNED;
  static final AudioFormat.Encoding PCM_FLOAT = AudioFormat.Encoding.PCM_FLOAT;

  /**
   Get the proprietary (to this class) type for output audio
   which can be used later to quickly build sample bytes from `float` values

   @param format of audio from which to extract proprietary sample format
   @return proprietary sample format
   @throws FormatException if format is unsupported
   */
  public static AudioSampleFormat typeOfOutput(AudioFormat format) throws FormatException {
    return typeOf(format, true);
  }

  /**
   Get the proprietary (to this class) type for input audio
   which can be used later to quickly build sample bytes from `float` values

   @param format of audio from which to extract proprietary sample format
   @return proprietary sample format
   @throws FormatException if format is unsupported
   */
  public static AudioSampleFormat typeOfInput(AudioFormat format) throws FormatException {
    return typeOf(format, false);
  }

  /**
   Get the proprietary (to this class) type
   which can be used later to quickly build sample bytes from `float` values

   @param format   of audio from which to extract proprietary sample format
   @param isOutput whether this format will be used for output (which affects rules)
   @return proprietary sample format
   @throws FormatException if format is unsupported
   */
  static AudioSampleFormat typeOf(AudioFormat format, boolean isOutput) throws FormatException {
    // switch based on frame size (bytes) and encoding
    AudioFormat.Encoding encoding = format.getEncoding();
    int sampleSizeInBits = format.getSampleSizeInBits();
    switch (sampleSizeInBits) {
      case 8 -> {
        if (!isOutput && encoding.equals(PCM_UNSIGNED)) {
          return U8;
        } else if (encoding.equals((PCM_SIGNED))) {
          return S8;
        } else {
          throw new FormatException("Unsupported 8-bit " + (isOutput ? "output " : "") + "encoding: " + encoding);
        }
      }
      case 16 -> {
        if (!isOutput && encoding.equals(PCM_UNSIGNED)) {
          return format.isBigEndian() ? U16MSB : U16LSB;
        } else if (encoding.equals((PCM_SIGNED))) {
          return format.isBigEndian() ? S16MSB : S16LSB;
        } else {
          throw new FormatException("Unsupported 16-bit " + (isOutput ? "output " : "") + "encoding: " + encoding);
        }
      }
      case 24 -> {
        if (!isOutput && encoding.equals(PCM_SIGNED)) {
          return format.isBigEndian() ? S24MSB : S24LSB;
        } else {
          throw new FormatException("Unsupported 24-bit " + (isOutput ? "output " : "") + "encoding: " + encoding);
        }
      }
      case 32 -> {
        if (encoding.equals(PCM_SIGNED)) {
          return format.isBigEndian() ? S32MSB : S32LSB;
        } else if (encoding.equals((PCM_FLOAT))) {
          return format.isBigEndian() ? F32MSB : F32LSB;
        } else {
          throw new FormatException("Unsupported 32-bit " + (isOutput ? "output " : "") + "encoding: " + encoding);
        }
      }
      case 64 -> {
        if (encoding.equals(PCM_FLOAT)) {
          return format.isBigEndian() ? F64MSB : F64LSB;
        } else {
          throw new FormatException("Unsupported 64-bit " + (isOutput ? "output " : "") + "encoding: " + encoding);
        }
      }
      default ->
        throw new FormatException("Unsupported " + (isOutput ? "output " : "") + " sample size: " + sampleSizeInBits + " bits");
    }
  }

  /**
   Convert a `float` value to output bytes based on its proprietary sample type

   @param value to convert
   @param type  of sample
   @return output bytes
   */
  public static byte[] toBytes(float value, AudioSampleFormat type) {
    return switch (type) {
      case S8 -> toBytesS8(value);
      case S16LSB -> toBytesS16LSB(value);
      case S16MSB -> toBytesS16MSB(value);
      case S32LSB -> toBytesS32LSB(value);
      case S32MSB -> toBytesS32MSB(value);
      case F32LSB -> toBytesF32LSB(value);
      case F32MSB -> toBytesF32MSB(value);
      case F64LSB -> toBytesF64LSB(value);
      case F64MSB -> toBytesF64MSB(value);
      default -> new byte[0];
    };
  }

  /**
   Convert input bytes to a `float` value based on its proprietary sample type

   @param value to convert
   @param type  of sample
   @return value
   */
  public static float fromBytes(byte[] value, AudioSampleFormat type) {
    return switch (type) {
      case U8 -> fromBytesU8(value);
      case S8 -> fromBytesS8(value);
      case U16LSB -> fromBytesU16LSB(value);
      case U16MSB -> fromBytesU16MSB(value);
      case S16LSB -> fromBytesS16LSB(value);
      case S16MSB -> fromBytesS16MSB(value);
      case S24LSB -> fromBytesS24LSB(value);
      case S24MSB -> fromBytesS24MSB(value);
      case S32LSB -> fromBytesS32LSB(value);
      case S32MSB -> fromBytesS32MSB(value);
      case F32LSB -> fromBytesF32LSB(value);
      case F32MSB -> fromBytesF32MSB(value);
      case F64LSB -> fromBytesF64LSB(value);
      case F64MSB -> fromBytesF64MSB(value);
    };
  }

  /**
   to bytes encoded as 8-bit signed int LSB

   @param value to encode
   @return encoded bytes
   */
  static byte[] toBytesS8(float value) {
    return new byte[]{(byte) (SIGNED_8BIT_MAX * value)};
  }

  /**
   to bytes encoded as 16-bit signed int LSB

   @param value to encode
   @return encoded bytes
   */
  static byte[] toBytesS16LSB(float value) {
    return ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort((short) (SIGNED_16BIT_MAX * value)).array();
  }

  /**
   to bytes encoded as 16-bit signed int MSB

   @param value to encode
   @return encoded bytes
   */
  static byte[] toBytesS16MSB(float value) {
    return ByteBuffer.allocate(2).order(ByteOrder.BIG_ENDIAN).putShort((short) (SIGNED_16BIT_MAX * value)).array();
  }

  /**
   to bytes encoded as 32-bit signed int LSB

   @param value to encode
   @return encoded bytes
   */
  static byte[] toBytesS32LSB(float value) {
    return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt((int) (SIGNED_32BIT_MAX * value)).array();
  }

  /**
   to bytes encoded as 32-bit signed int MSB

   @param value to encode
   @return encoded bytes
   */
  static byte[] toBytesS32MSB(float value) {
    return ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt((int) (SIGNED_32BIT_MAX * value)).array();
  }

  /**
   to bytes encoded as 32-bit float LSB

   @param value to encode
   @return encoded bytes
   */
  static byte[] toBytesF32LSB(float value) {
    return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putFloat((float) value).array();
  }

  /**
   to bytes encoded as 32-bit float MSB

   @param value to encode
   @return encoded bytes
   */
  static byte[] toBytesF32MSB(float value) {
    return ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putFloat((float) value).array();
  }

  /**
   to bytes encoded as 64-bit float LSB

   @param value to encode
   @return encoded bytes
   */
  static byte[] toBytesF64LSB(float value) {
    return ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putFloat(value).array();
  }

  /**
   to bytes encoded as 64-bit float MSB

   @param value to encode
   @return encoded bytes
   */
  static byte[] toBytesF64MSB(float value) {
    return ByteBuffer.allocate(8).order(ByteOrder.BIG_ENDIAN).putFloat(value).array();
  }

  /**
   from bytes encoded as 8-bit unsigned int LSB

   @param sample to decode
   @return value
   */
  static float fromBytesU8(byte[] sample) {
    return (float) (ByteBuffer.wrap(sample)
      .order(ByteOrder.LITTLE_ENDIAN)
      .get() & UNSIGNED_8BIT_MAX) / (float) SIGNED_8BIT_MAX - 1;
  }

  /**
   from bytes encoded as 8-bit signed int LSB

   @param sample to decode
   @return value
   */
  static float fromBytesS8(byte[] sample) {
    return (float) ByteBuffer.wrap(sample)
      .order(ByteOrder.LITTLE_ENDIAN)
      .get() / (float) SIGNED_8BIT_MAX;
  }

  /**
   from bytes encoded as 16-bit unsigned int LSB

   @param sample to decode
   @return value
   */
  static float fromBytesU16LSB(byte[] sample) {
    return (float) (ByteBuffer.wrap(sample)
      .order(ByteOrder.LITTLE_ENDIAN)
      .getShort() & UNSIGNED_16BIT_MAX) / (float) SIGNED_16BIT_MAX - 1;
  }

  /**
   from bytes encoded as 16-bit unsigned int MSB

   @param sample to decode
   @return value
   */
  static float fromBytesU16MSB(byte[] sample) {
    return (float) (ByteBuffer.wrap(sample)
      .order(ByteOrder.BIG_ENDIAN)
      .getShort() & UNSIGNED_16BIT_MAX) / (float) SIGNED_16BIT_MAX - 1;
  }

  /**
   from bytes encoded as 16-bit signed int LSB

   @param sample to decode
   @return value
   */
  static float fromBytesS16LSB(byte[] sample) {
    return (float) ByteBuffer.wrap(sample)
      .order(ByteOrder.LITTLE_ENDIAN)
      .getShort() / (float) SIGNED_16BIT_MAX;
  }

  /**
   from bytes encoded as 16-bit signed int MSB

   @param sample to decode
   @return value
   */
  static float fromBytesS16MSB(byte[] sample) {
    return (float) ByteBuffer.wrap(sample)
      .order(ByteOrder.BIG_ENDIAN)
      .getShort() / (float) SIGNED_16BIT_MAX;
  }

  /**
   from bytes encoded as 24-bit signed int LSB

   @param sample to decode
   @return value
   */
  static float fromBytesS24LSB(byte[] sample) {
    return (float) ((sample[2]) << 16 | (sample[1] & 0xFF) << 8 | (sample[0] & 0xFF)) / (float) SIGNED_24BIT_MAX;
/*
    return (float) ByteBuffer.wrap(sample)
      .order(ByteOrder.LITTLE_ENDIAN)
      .getShort() / (float) 0x8000;
*/
  }


  /**
   from bytes encoded as 24-bit signed int MSB

   @param sample to decode
   @return value
   */
  static float fromBytesS24MSB(byte[] sample) {
    return (float) ((sample[0]) << 16 | (sample[1] & 0xFF) << 8 | (sample[2] & 0xFF)) / (float) SIGNED_24BIT_MAX;

    /*
    return (float) ByteBuffer.wrap(sample)
      .order(ByteOrder.BIG_ENDIAN)
      .getShort() / (float) 0x800000;
*/
  }

  /**
   from bytes encoded as 32-bit signed int LSB

   @param sample to decode
   @return value
   */
  static float fromBytesS32LSB(byte[] sample) {
    return (float) ByteBuffer.wrap(sample)
      .order(ByteOrder.LITTLE_ENDIAN)
      .getInt() / (float) SIGNED_32BIT_MAX;
  }

  /**
   from bytes encoded as 32-bit signed int MSB

   @param sample to decode
   @return value
   */
  static float fromBytesS32MSB(byte[] sample) {
    return (float) ByteBuffer.wrap(sample)
      .order(ByteOrder.BIG_ENDIAN)
      .getInt() / (float) SIGNED_32BIT_MAX;
  }

  /**
   from bytes encoded as 32-bit float LSB

   @param sample to decode
   @return value
   */
  static float fromBytesF32LSB(byte[] sample) {
    return ByteBuffer.wrap(sample)
      .order(ByteOrder.LITTLE_ENDIAN)
      .getFloat();
  }

  /**
   from bytes encoded as 32-bit float MSB

   @param sample to decode
   @return value
   */
  static float fromBytesF32MSB(byte[] sample) {
    return ByteBuffer.wrap(sample)
      .order(ByteOrder.BIG_ENDIAN)
      .getFloat();
  }

  /**
   from bytes encoded as 64-bit float LSB

   @param sample to decode
   @return value
   */
  static float fromBytesF64LSB(byte[] sample) {
    return ByteBuffer.wrap(sample)
      .order(ByteOrder.LITTLE_ENDIAN)
      .getFloat();
  }

  /**
   from bytes encoded as 64-bit float MSB

   @param sample to decode
   @return value
   */
  static float fromBytesF64MSB(byte[] sample) {
    return ByteBuffer.wrap(sample)
      .order(ByteOrder.BIG_ENDIAN)
      .getFloat();
  }

}

