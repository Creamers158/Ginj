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

package org.apache.hc.core5.reactor;

import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.net.NamedEndpoint;
import org.apache.hc.core5.util.Timeout;

import java.net.SocketAddress;
import java.util.concurrent.Future;

/**
 * Non-blocking connection initiator.
 *
 * @since 5.0
 */
public interface ConnectionInitiator {

    /**
     * Requests a connection to a remote host.
     * <p>
     * Opening a connection to a remote host usually tends to be a time
     * consuming process and may take a while to complete. One can monitor and
     * control the process of session initialization by means of the
     * {@link Future} interface.
     * <p>
     * There are several parameters one can use to exert a greater control over
     * the process of session initialization:
     * <p>
     * A non-null local socket address parameter can be used to bind the socket
     * to a specific local address.
     * <p>
     * An attachment object can added to the new session's context upon
     * initialization. This object can be used to pass an initial processing
     * state to the protocol handler.
     * <p>
     * It is often desirable to be able to react to the completion of a session
     * request asynchronously without having to wait for it, blocking the
     * current thread of execution. One can optionally provide an implementation
     * {@link FutureCallback} instance to get notified of events related
     * to session requests, such as request completion, cancellation, failure or
     * timeout.
     *
     * @param remoteEndpoint name of the remote host.
     * @param remoteAddress remote socket address.
     * @param localAddress local socket address. Can be {@code null},
     *    in which can the default local address and a random port will be used.
     * @param timeout connect timeout.
     * @param attachment the attachment object. Can be {@code null}.
     * @param callback interface. Can be {@code null}.
     * @return session request object.
     */
    Future<IOSession> connect(
            NamedEndpoint remoteEndpoint,
            SocketAddress remoteAddress,
            SocketAddress localAddress,
            Timeout timeout,
            Object attachment,
            FutureCallback<IOSession> callback);

}
