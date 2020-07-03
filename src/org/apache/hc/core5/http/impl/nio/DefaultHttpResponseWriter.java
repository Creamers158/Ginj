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

package org.apache.hc.core5.http.impl.nio;

import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.HttpVersion;
import org.apache.hc.core5.http.ProtocolVersion;
import org.apache.hc.core5.http.message.LineFormatter;
import org.apache.hc.core5.http.message.StatusLine;
import org.apache.hc.core5.util.CharArrayBuffer;

import java.io.IOException;

/**
 * Default {@link org.apache.hc.core5.http.nio.NHttpMessageWriter} implementation for {@link HttpResponse}s.
 *
 * @since 4.1
 */
public class DefaultHttpResponseWriter<T extends HttpResponse> extends AbstractMessageWriter<T> {

    /**
     * Creates an instance of DefaultHttpResponseWriter.
     *
     * @param formatter the line formatter If {@code null}
     *   {@link org.apache.hc.core5.http.message.BasicLineFormatter#INSTANCE} will be used.
     *
     * @since 4.3
     */
    public DefaultHttpResponseWriter(final LineFormatter formatter) {
        super(formatter);
    }

    /**
     * @since 4.3
     */
    public DefaultHttpResponseWriter() {
        super(null);
    }

    @Override
    protected void writeHeadLine(final T message, final CharArrayBuffer lineBuf) throws IOException {
        lineBuf.clear();
        final ProtocolVersion transportVersion = message.getVersion();
        getLineFormatter().formatStatusLine(lineBuf, new StatusLine(
                transportVersion != null ? transportVersion : HttpVersion.HTTP_1_1,
                message.getCode(),
                message.getReasonPhrase()));
    }

}
