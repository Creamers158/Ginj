/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package org.apache.hc.core5.http.io.entity;

import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.io.IOCallback;
import org.apache.hc.core5.util.Args;

import java.io.*;

/**
 * Entity that delegates the process of content generation to a {@link IOCallback}
 * with {@link OutputStream} as output sink.
 *
 * @since 4.0
 */
@Contract(threading = ThreadingBehavior.IMMUTABLE_CONDITIONAL)
public final class EntityTemplate extends AbstractHttpEntity {

    private final long contentLength;
    private final IOCallback<OutputStream> callback;

    public EntityTemplate(
            final long contentLength, final ContentType contentType, final String contentEncoding,
            final IOCallback<OutputStream> callback) {
        super(contentType, contentEncoding);
        this.contentLength = contentLength;
        this.callback = Args.notNull(callback, "I/O callback");
    }

    @Override
    public long getContentLength() {
        return contentLength;
    }

    @Override
    public InputStream getContent() throws IOException {
        final ByteArrayOutputStream buf = new ByteArrayOutputStream();
        writeTo(buf);
        return new ByteArrayInputStream(buf.toByteArray());
    }

    @Override
    public boolean isRepeatable() {
        return true;
    }

    @Override
    public void writeTo(final OutputStream outStream) throws IOException {
        Args.notNull(outStream, "Output stream");
        this.callback.execute(outStream);
    }

    @Override
    public boolean isStreaming() {
        return false;
    }

    @Override
    public void close() throws IOException {
    }

}
