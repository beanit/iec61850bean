/*
 * Copyright 2011 The IEC61850bean Authors
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
package com.beanit.josistack;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Simple InputStream wrapper around a {@link ByteBuffer} object
 *
 * @author Karsten Mueller-Bier
 */
public final class ByteBufferInputStream extends InputStream {

  private final ByteBuffer buf;

  public ByteBufferInputStream(ByteBuffer buf) {
    this.buf = buf;
  }

  @Override
  public int read() throws IOException {
    if (buf.hasRemaining() == false) {
      return -1;
    }
    return buf.get() & 0xFF;
  }

  @Override
  public int read(byte[] b, int off, int len) throws IOException {
    if (buf.hasRemaining() == false) {
      return -1;
    }
    int size = Math.min(len, available());

    buf.get(b, off, size);
    return size;
  }

  @Override
  public int available() throws IOException {
    return buf.limit() - buf.position();
  }
}
