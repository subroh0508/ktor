/*
 * Copyright 2014-2020 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.tests.http.cio.websocket

import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlin.coroutines.*
import kotlin.test.*

class DefaultWebSocketSessionImplTest {
    @Test
    fun testIncomingClose() {
        val incoming = Channel<Frame>()
        val outgoing = Channel<Frame>()
        val job = Job()

        val rawSession = TestWebSocketSession(incoming, outgoing, job)
        val defaultSession = DefaultWebSocketSessionImpl(rawSession)

        incoming.close()

        val closeReason = runBlocking {
            withTimeout(1000) {
                try {
                    defaultSession.closeReason.await()
                } catch (_: CancellationException) { /* Do nothing. */
                }
            }
        }

        assertNotNull(closeReason)
    }

    private class TestWebSocketSession(
        override val incoming: ReceiveChannel<Frame>,
        override val outgoing: SendChannel<Frame>,
        override val coroutineContext: CoroutineContext
    ) : WebSocketSession {
        override var masking = false

        override var maxFrameSize = 0L

        override suspend fun flush() {}

        override fun terminate() {}
    }
}
