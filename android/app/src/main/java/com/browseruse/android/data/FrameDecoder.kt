package com.browseruse.android.data

data class FrameHeader(val type: Int, val length: Int, val seq: Int)

object FrameDecoder {

    const val HEADER_SIZE = 9
    const val TYPE_JPEG = 1
    const val TYPE_STATUS = 2

    fun header(bytes: ByteArray): FrameHeader? {
        if (bytes.size < HEADER_SIZE) return null
        val type = bytes[0].toInt() and 0xFF
        val length = readInt(bytes, 1)
        val seq = readInt(bytes, 5)
        if (length < 0 || HEADER_SIZE + length > bytes.size) return null
        return FrameHeader(type, length, seq)
    }

    fun payload(bytes: ByteArray): ByteArray? {
        val header = header(bytes) ?: return null
        return bytes.copyOfRange(HEADER_SIZE, HEADER_SIZE + header.length)
    }

    private fun readInt(bytes: ByteArray, offset: Int): Int {
        return ((bytes[offset].toInt() and 0xFF) shl 24) or
            ((bytes[offset + 1].toInt() and 0xFF) shl 16) or
            ((bytes[offset + 2].toInt() and 0xFF) shl 8) or
            (bytes[offset + 3].toInt() and 0xFF)
    }
}
