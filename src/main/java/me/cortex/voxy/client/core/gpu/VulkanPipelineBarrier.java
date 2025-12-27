package me.cortex.voxy.client.core.gpu;

import static org.lwjgl.opengl.GL42.GL_BUFFER_UPDATE_BARRIER_BIT;
import static org.lwjgl.opengl.GL42.GL_PIXEL_BUFFER_BARRIER_BIT;
import static org.lwjgl.opengl.GL43.GL_SHADER_STORAGE_BARRIER_BIT;
import static org.lwjgl.opengl.GL44.GL_CLIENT_MAPPED_BUFFER_BARRIER_BIT;
import static org.lwjgl.opengl.GL45C.GL_FRAMEBUFFER_BARRIER_BIT;

final class VulkanPipelineBarrier {
    static final int STAGE_TRANSFER = 1 << 0;
    static final int STAGE_HOST = 1 << 1;
    static final int STAGE_DRAW = 1 << 2;
    static final int STAGE_COMPUTE = 1 << 3;
    static final int STAGE_ALL = STAGE_TRANSFER | STAGE_HOST | STAGE_DRAW | STAGE_COMPUTE;

    static final int ACCESS_TRANSFER_READ = 1 << 0;
    static final int ACCESS_TRANSFER_WRITE = 1 << 1;
    static final int ACCESS_SHADER_READ = 1 << 2;
    static final int ACCESS_SHADER_WRITE = 1 << 3;
    static final int ACCESS_HOST_READ = 1 << 4;
    static final int ACCESS_HOST_WRITE = 1 << 5;
    static final int ACCESS_MEMORY_READ = 1 << 6;
    static final int ACCESS_MEMORY_WRITE = 1 << 7;
    static final int ACCESS_COLOR_ATTACHMENT_WRITE = 1 << 8;

    final int srcStageMask;
    final int dstStageMask;
    final int srcAccessMask;
    final int dstAccessMask;

    private VulkanPipelineBarrier(int srcStageMask, int dstStageMask, int srcAccessMask, int dstAccessMask) {
        this.srcStageMask = srcStageMask;
        this.dstStageMask = dstStageMask;
        this.srcAccessMask = srcAccessMask;
        this.dstAccessMask = dstAccessMask;
    }

    static VulkanPipelineBarrier fromGlBarrierBits(int barrierBits) {
        int srcStageMask = 0;
        int dstStageMask = 0;
        int srcAccessMask = 0;
        int dstAccessMask = 0;

        if ((barrierBits & GL_BUFFER_UPDATE_BARRIER_BIT) != 0) {
            srcStageMask |= STAGE_TRANSFER;
            dstStageMask |= STAGE_ALL;
            srcAccessMask |= ACCESS_TRANSFER_WRITE;
            dstAccessMask |= ACCESS_TRANSFER_READ | ACCESS_SHADER_READ | ACCESS_SHADER_WRITE;
        }

        if ((barrierBits & GL_CLIENT_MAPPED_BUFFER_BARRIER_BIT) != 0) {
            srcStageMask |= STAGE_TRANSFER;
            dstStageMask |= STAGE_HOST;
            srcAccessMask |= ACCESS_TRANSFER_WRITE;
            dstAccessMask |= ACCESS_HOST_READ;
        }

        if ((barrierBits & GL_PIXEL_BUFFER_BARRIER_BIT) != 0) {
            srcStageMask |= STAGE_TRANSFER;
            dstStageMask |= STAGE_TRANSFER;
            srcAccessMask |= ACCESS_TRANSFER_WRITE;
            dstAccessMask |= ACCESS_TRANSFER_READ;
        }

        if ((barrierBits & GL_FRAMEBUFFER_BARRIER_BIT) != 0) {
            srcStageMask |= STAGE_DRAW;
            dstStageMask |= STAGE_DRAW;
            srcAccessMask |= ACCESS_COLOR_ATTACHMENT_WRITE;
            dstAccessMask |= ACCESS_SHADER_READ | ACCESS_SHADER_WRITE;
        }

        if ((barrierBits & GL_SHADER_STORAGE_BARRIER_BIT) != 0) {
            srcStageMask |= STAGE_COMPUTE | STAGE_DRAW;
            dstStageMask |= STAGE_COMPUTE | STAGE_DRAW;
            srcAccessMask |= ACCESS_SHADER_WRITE;
            dstAccessMask |= ACCESS_SHADER_READ | ACCESS_SHADER_WRITE;
        }

        if (srcStageMask == 0 && dstStageMask == 0) {
            srcStageMask = STAGE_ALL;
            dstStageMask = STAGE_ALL;
            srcAccessMask = ACCESS_MEMORY_WRITE;
            dstAccessMask = ACCESS_MEMORY_READ | ACCESS_MEMORY_WRITE;
        }

        return new VulkanPipelineBarrier(srcStageMask, dstStageMask, srcAccessMask, dstAccessMask);
    }
}
