/*
 * Copyright 2018 beanit
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
package com.beanit.openiec61850;

public class HexConverter {

  private static final char[] hexArray = "0123456789ABCDEF".toCharArray();

  /** Don't let anyone instantiate this class. */
  private HexConverter() {}

  public static String toHexString(byte b) {
    StringBuilder builder = new StringBuilder();
    appendHexString(b, builder);
    return builder.toString();
  }

  public static String toHexString(byte[] bytes) {
    return toHexString(bytes, 0, bytes.length);
  }

  public static String toHexString(byte[] bytes, int offset, int length) {
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
      appendHexString(bytes[i], builder);
      if (i != offset + length - 1) {
        builder.append(' ');
      }
    }

    return builder.toString();
  }

  /**
   * Returns the integer value as hex string filled with leading zeros. If you do not want leading
   * zeros use Integer.toHexString(int i) instead.
   *
   * @param i the integer value to be converted
   * @return the hex string
   */
  public static String toShortHexString(int i) {
    byte[] bytes = new byte[] {(byte) (i >> 24), (byte) (i >> 16), (byte) (i >> 8), (byte) (i)};
    return toShortHexString(bytes);
  }

  /**
   * Returns the long value as hex string filled with leading zeros. If you do not want leading
   * zeros use Long.toHexString(long i) instead.
   *
   * @param l the long value to be converted
   * @return the hex string
   */
  public static String toShortHexString(long l) {
    byte[] bytes =
        new byte[] {
          (byte) (l >> 56),
          (byte) (l >> 48),
          (byte) (l >> 40),
          (byte) (l >> 32),
          (byte) (l >> 24),
          (byte) (l >> 16),
          (byte) (l >> 8),
          (byte) (l)
        };
    return toShortHexString(bytes);
  }

  /**
   * Returns the byte as a hex string. If b is less than 16 the hex string returned contains a
   * leading zero.
   *
   * @param b the byte to be converted
   * @return the byte as a hex string.
   */
  public static String toShortHexString(byte b) {
    return toShortHexString(new byte[] {b});
  }

  public static String toShortHexString(byte[] bytes) {
    return toShortHexString(bytes, 0, bytes.length);
  }

  public static String toShortHexString(byte[] bytes, int offset, int length) {
    char[] hexChars = new char[length * 2];
    for (int j = 0; j < length; j++) {
      int v = bytes[j + offset] & 0xff;
      hexChars[j * 2] = hexArray[v >>> 4];
      hexChars[j * 2 + 1] = hexArray[v & 0x0f];
    }
    return new String(hexChars);
  }

  public static byte[] fromShortHexString(String shortHexString) throws NumberFormatException {

    validate(shortHexString);

    int length = shortHexString.length();

    byte[] data = new byte[length / 2];
    for (int i = 0; i < length; i += 2) {
      int firstCharacter = Character.digit(shortHexString.charAt(i), 16);
      int secondCharacter = Character.digit(shortHexString.charAt(i + 1), 16);

      if (firstCharacter == -1 || secondCharacter == -1) {
        throw new NumberFormatException("string is not a legal hex string.");
      }

      data[i / 2] = (byte) ((firstCharacter << 4) + secondCharacter);
    }
    return data;
  }

  public static void appendShortHexString(byte b, StringBuilder builder) {
    builder.append(toShortHexString(b));
  }

  public static void appendShortHexString(
      StringBuilder builder, byte[] bytes, int offset, int length) {
    builder.append(toShortHexString(bytes, offset, length));
  }

  public static void appendHexString(byte b, StringBuilder builder) {
    builder.append("0x");
    appendShortHexString(b, builder);
  }

  public static void appendHexString(
      StringBuilder builder, byte[] byteArray, int offset, int length) {
    int l = 1;
    for (int i = offset; i < (offset + length); i++) {
      if ((l != 1) && ((l - 1) % 8 == 0)) {
        builder.append(' ');
      }
      if ((l != 1) && ((l - 1) % 16 == 0)) {
        builder.append('\n');
      }
      l++;
      appendHexString(byteArray[i], builder);
      if (i != offset + length - 1) {
        builder.append(' ');
      }
    }
  }

  private static void validate(String s) {
    if (s == null) {
      throw new IllegalArgumentException("string s may not be null");
    }

    if ((s.length() == 0) || ((s.length() % 2) != 0)) {
      throw new NumberFormatException("string is not a legal hex string.");
    }
  }
}
