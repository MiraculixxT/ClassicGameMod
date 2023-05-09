/*
 * This file is part of LiquidBounce (https://github.com/CCBlueX/LiquidBounce)
 *
 * Copyright (c) 2016 - 2023 CCBlueX
 *
 * LiquidBounce is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LiquidBounce is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LiquidBounce. If not, see <https://www.gnu.org/licenses/>.
 */
package de.miraculixx.cgames.ultralight.impl.renderer

import com.google.common.collect.ImmutableMap
import com.labymedia.ultralight.UltralightView
import com.labymedia.ultralight.bitmap.UltralightBitmapSurface
import com.labymedia.ultralight.config.UltralightViewConfig
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.Tesselator
import com.mojang.blaze3d.vertex.VertexFormat
import de.miraculixx.cgames.client
import net.minecraft.client.renderer.GameRenderer
import org.joml.Matrix3dStack
import org.lwjgl.opengl.GL12.*
import java.nio.ByteBuffer

/**
 * A cpu renderer which is being supported on OpenGL functionality from version 1.2.
 */
class CpuViewRenderer : ViewRenderer {

    private var glTexture = -1

    override fun setupConfig(viewConfig: UltralightViewConfig) {
        // CPU rendering is not accelerated
        // viewConfig.isAccelerated(false)
    }

    override fun delete() {
        glDeleteTextures(glTexture)
        glTexture = -1
    }

    /**
     * Render the current view
     */
    override fun render(view: UltralightView, matrices: Matrix3dStack) {
        if (glTexture == -1) {
            createGlTexture()
        }

        // As we are using the CPU renderer, draw with a bitmap (we did not set a custom surface)
        val surface = view.surface() as UltralightBitmapSurface
        val bitmap = surface.bitmap()
        val width = view.width().toInt()
        val height = view.height().toInt()

        // Prepare OpenGL for 2D textures and bind our texture
        RenderSystem.bindTexture(glTexture)

        val dirtyBounds = surface.dirtyBounds()

        if (dirtyBounds.isValid) {
            val imageData = bitmap.lockPixels()

            glPixelStorei(GL_UNPACK_SKIP_ROWS, 0)
            glPixelStorei(GL_UNPACK_SKIP_PIXELS, 0)
            glPixelStorei(GL_UNPACK_SKIP_IMAGES, 0)
            glPixelStorei(GL_UNPACK_ROW_LENGTH, bitmap.rowBytes().toInt() / 4)

            if (dirtyBounds.width() == width && dirtyBounds.height() == height) {
                // Update full image
                glTexImage2D(
                    GL_TEXTURE_2D,
                    0,
                    GL_RGBA8,
                    width,
                    height,
                    0,
                    GL_BGRA,
                    GL_UNSIGNED_INT_8_8_8_8_REV,
                    imageData
                )
            } else {
                // Update partial image
                val x = dirtyBounds.x()
                val y = dirtyBounds.y()
                val dirtyWidth = dirtyBounds.width()
                val dirtyHeight = dirtyBounds.height()
                val startOffset = (y * bitmap.rowBytes() + x * 4).toInt()

                glTexSubImage2D(
                    GL_TEXTURE_2D,
                    0,
                    x, y, dirtyWidth, dirtyHeight,
                    GL_BGRA, GL_UNSIGNED_INT_8_8_8_8_REV,
                    imageData.position(startOffset) as ByteBuffer
                )
            }
            glPixelStorei(GL_UNPACK_ROW_LENGTH, 0)

            bitmap.unlockPixels()
            surface.clearDirtyBounds()
        }

        val tessellator = Tesselator.getInstance()
        val bufferBuilder = tessellator.builder
        val scaleFactor = client.window.guiScale.toFloat()

        RenderSystem.setShader { GameRenderer.getPositionTexColorShader() }
        RenderSystem.setShaderTexture(0, glTexture)
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f)
        RenderSystem.enableBlend()
        bufferBuilder.begin(VertexFormat.Mode.QUADS, VertexFormat(ImmutableMap.of()))

        bufferBuilder
            .vertex(0.0, height.toDouble(), 0.0)
            .color(255, 255, 255, 255)
            .endVertex()
        bufferBuilder
            .vertex(width.toDouble(), height.toDouble(), 0.0)
            .color(255, 255, 255, 255)
            .endVertex()
        bufferBuilder
            .vertex(width.toDouble(), 0.0, 0.0)
            .color(255, 255, 255, 255)
            .endVertex()

        bufferBuilder
            .vertex(0.0, 0.0, 0.0)
            .color(255, 255, 255, 255)
            .endVertex()

        tessellator.end()
        RenderSystem.disableBlend()
    }

    /**
     * Sets up the OpenGL texture for rendering
     */
    private fun createGlTexture() {
        glTexture = glGenTextures()
        glBindTexture(GL_TEXTURE_2D, glTexture)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
        glBindTexture(GL_TEXTURE_2D, 0)
    }

}
