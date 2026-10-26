package com.browseruse.android

import com.browseruse.android.data.FrameDecoder
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FrameDecoderTest {

    @Test
    fun `decodes header and payload`() {
        val payload = byteArrayOf(1, 2, 3, 4)
        val frame = ByteArray(FrameDecoder.HEADER_SIZE + payload.size)
        frame[0] = FrameDecoder.TYPE_JPEG.toByte()
        writeInt(frame, 1, payload.size)
        writeInt(frame, 5, 42)
        payload.copyInto(frame, FrameDecoder.HEADER_SIZE)

        val header = FrameDecoder.header(frame)
        assertEquals(FrameDecoder.TYPE_JPEG, header!!.type)
        assertEquals(payload.size, header.length)
        assertEquals(42, header.seq)
        assertArrayEquals(payload, FrameDecoder.payload(frame))
    }

    @Test
    fun `rejects short frames`() {
        assertNull(FrameDecoder.header(byteArrayOf(1, 2)))
    }

    @Test
    fun `rejects frames whose length exceeds the buffer`() {
        val frame = ByteArray(FrameDecoder.HEADER_SIZE)
        frame[0] = FrameDecoder.TYPE_JPEG.toByte()
        writeInt(frame, 1, 100)
        assertNull(FrameDecoder.header(frame))
    }

    private fun writeInt(bytes: ByteArray, offset: Int, value: Int) {
        bytes[offset] = (value ushr 24).toByte()
        bytes[offset + 1] = (value ushr 16).toByte()
        bytes[offset + 2] = (value ushr 8).toByte()
        bytes[offset + 3] = value.toByte()
    }
}
