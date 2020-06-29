/*
 * Copyright 2019 beanit
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.beanit.iec61850bean.internal;

import java.nio.ByteBuffer;
import java.util.Objects;

public class HexString {

  private static final char[] hexArray = "0123456789ABCDEF".toCharArray();

  /** Don't let anyone instantiate this class. */
  private HexString() {}

  /**
   * Returns the byte as a hex string. If b is less than 16 the hex string returned contains a
   * leading zero.
   *
   * @param b the byte to be converted
   * @return the hex string.
   */
  public static String fromByte(byte b) {
    return fromBytes(new byte[] {b});
  }

  public static String fromByte(int b) {
    return fromBytes(new byte[] {(byte) b});
  }

  /**
   * Returns the integer value as hex string filled with leading zeros.
   *
   * @param i the integer value to be converted
   * @return the hex string
   */
  public static String fromInt(int i) {
    byte[] bytes = new byte[] {(byte) (i >> 24), (byte) (i >> 16), (byte) (i >> 8), (byte) i};
    return fromBytes(bytes);
  }

  /**
   * Returns the long value as hex string filled with leading zeros.
   *
   * @param l the long value to be converted
   * @return the hex string
   */
  public static String fromLong(long l) {
    byte[] bytes =
        new byte[] {
          (byte) (l >> 56),
          (byte) (l >> 48),
          (byte) (l >> 40),
          (byte) (l >> 32),
          (byte) (l >> 24),
          (byte) (l >> 16),
          (byte) (l >> 8),
          (byte) l
        };
    return fromBytes(bytes);
  }

  public static String fromBytes(byte[] bytes) {
    return fromBytes(bytes, 0, bytes.length);
  }

  public static String fromBytesFormatted(byte[] bytes) {
    return fromBytesFormatted(bytes, 0, bytes.length);
  }

  public static String fromBytes(byte[] bytes, int offset, int length) {
    char[] hexChars = new char[length * 2];
    for (int j = 0; j < length; j++) {
      int v = bytes[j + offset] & 0xff;
      hexChars[j * 2] = hexArray[v >>> 4];
      hexChars[j * 2 + 1] = hexArray[v & 0x0f];
    }
    return new String(hexChars);
  }

  public static String fromBytes(ByteBuffer buffer) {
    return fromBytes(buffer.array(), buffer.arrayOffset(), buffer.arrayOffset() + buffer.limit());
  }

  public static String fromBytesFormatted(byte[] bytes, int offset, int length) {
    StringBuilder builder = new StringBuilder();

    int l = 1;
    for (int i = offset; i < (offset + length); i++) {
      if ((l != 1) && ((l - 1) % 8 == 0)) {
        builder.append(' ');
      }
      if ((l != 1) && ((l - 1) % 16 == 0)) {
        builder.append('\n');
      }
      l++;
      appendFromByte(bytes[i], builder);
      if (i != offset + length - 1) {
        builder.append(' ');
      }
    }
    return builder.toString();
  }

  /**
   * Converts the given hex string to a byte array.
   *
   * @param hexString the hex string
   * @return the bytes
   * @throws NumberFormatException if the string is not a valid hex string
   */
  public static byte[] toBytes(String hexString) {

    Objects.requireNonNull(hexString);
    if ((hexString.length() == 0) || ((hexString.length() % 2) != 0)) {
      throw new NumberFormatException("argument is not a valid hex string");
    }

    int length = hexString.length();

    byte[] data = new byte[length / 2];
    for (int i = 0; i < length; i += 2) {
      int firstCharacter = Character.digit(hexString.charAt(i), 16);
      int secondCharacter = Character.digit(hexString.charAt(i + 1), 16);

      if (firstCharacter == -1 || secondCharacter == -1) {
        throw new NumberFormatException("argument is not a valid hex string");
      }

      data[i / 2] = (byte) ((firstCharacter << 4) + secondCharacter);
    }
    return data;
  }

  public static void appendFromByte(byte b, StringBuilder builder) {
    builder.append(fromByte(b));
  }

  public static void appendFromBytes(StringBuilder builder, byte[] bytes, int offset, int length) {
    builder.append(fromBytes(bytes, offset, length));
  }
}
