package com.browseruse.server

import java.awt.Color
import java.awt.Font
import java.awt.GradientPaint
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

class DemoFrames {

    fun frame(sessionId: String, seq: Int): ByteArray {
        val width = 1280
        val height = 720
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        val g = image.createGraphics()
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        g.paint = GradientPaint(0f, 0f, Color(39, 39, 42), width.toFloat(), height.toFloat(), Color(9, 9, 11))
        g.fillRect(0, 0, width, height)

        g.color = Color.WHITE
        g.font = Font(Font.SANS_SERIF, Font.BOLD, 44)
        g.drawString("Cloud browser", 80, 300)

        g.color = Color(161, 161, 170)
        g.font = Font(Font.SANS_SERIF, Font.PLAIN, 24)
        g.drawString("Agent is driving a real Chromium next to the engine", 80, 350)

        g.font = Font(Font.SANS_SERIF, Font.PLAIN, 20)
        g.drawString("Demo CDP frame stream - session $sessionId - frame $seq", 80, 395)

        g.color = Color(113, 113, 122)
        g.font = Font(Font.MONOSPACED, Font.PLAIN, 18)
        g.drawString("https://github.com/browser-use/browser-use", 80, 650)

        g.dispose()
        val out = ByteArrayOutputStream()
        ImageIO.write(image, "jpg", out)
        return out.toByteArray()
    }
}
