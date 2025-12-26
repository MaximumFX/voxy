package me.cortex.voxy.client.core.gpu;

import me.cortex.voxy.client.core.gl.GlBuffer;
import me.cortex.voxy.client.core.gl.GlFence;
import me.cortex.voxy.client.core.gl.GlPersistentMappedBuffer;
import me.cortex.voxy.client.core.gl.GlTexture;

import static org.lwjgl.opengl.GL11C.glEnable;
import static org.lwjgl.opengl.GL11C.glDisable;
import static org.lwjgl.opengl.GL11C.glDepthFunc;
import static org.lwjgl.opengl.GL11C.glStencilFunc;
import static org.lwjgl.opengl.GL11C.glStencilMask;
import static org.lwjgl.opengl.GL11C.glStencilOp;
import static org.lwjgl.opengl.GL11C.glColorMask;
import static org.lwjgl.opengl.GL14.glBlendFuncSeparate;
import static org.lwjgl.opengl.GL30C.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30C.glBindFramebuffer;
import static org.lwjgl.opengl.GL33C.glBindSampler;
import static org.lwjgl.opengl.GL42.glMemoryBarrier;
import static org.lwjgl.opengl.GL42C.glDrawArraysInstancedBaseInstance;
import static org.lwjgl.opengl.GL42C.glDrawElementsInstancedBaseVertexBaseInstance;
import static org.lwjgl.opengl.GL43.glBindImageTexture;
import static org.lwjgl.opengl.GL45.glClearNamedFramebufferfi;
import static org.lwjgl.opengl.GL45.glGetNamedFramebufferAttachmentParameteri;
import static org.lwjgl.opengl.GL45C.glBindTextureUnit;
import static org.lwjgl.opengl.GL45C.glTextureParameterf;
import static org.lwjgl.opengl.GL45C.nglUniformMatrix4fv;
import static org.lwjgl.opengl.GL45C.glUniform2f;
import static org.lwjgl.opengl.GL45C.glUniform4f;
import static org.lwjgl.opengl.ARBComputeShader.glDispatchCompute;

final class OpenGlGpuBackend implements GpuBackend {
    private final GpuDevice device = new OpenGlGpuDevice();

    @Override
    public GpuDevice device() {
        return this.device;
    }

    @Override
    public GpuCommandList createCommandList() {
        return new OpenGlGpuCommandList();
    }

    @Override
    public void submit(GpuCommandList commandList, GpuFence fence) {
        if (fence != null) {
            throw new UnsupportedOperationException("OpenGL backend fence signaling is not implemented yet");
        }
    }

    private static final class OpenGlGpuDevice implements GpuDevice {
        @Override
        public GpuBuffer createBuffer(BufferDescriptor descriptor) {
            GlBuffer buffer = new GlBuffer(descriptor.sizeBytes(), descriptor.flags(), descriptor.zeroed());
            if (descriptor.label() != null) {
                buffer.name(descriptor.label());
            }
            return buffer;
        }

        @Override
        public GpuTexture createTexture(TextureDescriptor descriptor) {
            GlTexture texture = new GlTexture();
            texture.store(descriptor.format(), descriptor.levels(), descriptor.width(), descriptor.height());
            if (descriptor.label() != null) {
                texture.name(descriptor.label());
            }
            return texture;
        }

        @Override
        public GpuSampler createSampler(SamplerDescriptor descriptor) {
            throw new UnsupportedOperationException("OpenGL sampler creation is not implemented yet");
        }

        @Override
        public GpuShaderModule createShaderModule(ShaderModuleDescriptor descriptor) {
            throw new UnsupportedOperationException("OpenGL shader module creation is not implemented yet");
        }

        @Override
        public GpuPipelineLayout createPipelineLayout(PipelineLayoutDescriptor descriptor) {
            throw new UnsupportedOperationException("OpenGL pipeline layout creation is not implemented yet");
        }

        @Override
        public GpuPipeline createPipeline(PipelineDescriptor descriptor) {
            throw new UnsupportedOperationException("OpenGL pipeline creation is not implemented yet");
        }

        @Override
        public GpuFence createFence() {
            return new GlFence();
        }

        @Override
        public GpuQueryPool createQueryPool(QueryPoolDescriptor descriptor) {
            throw new UnsupportedOperationException("OpenGL query pool creation is not implemented yet");
        }

        @Override
        public GpuBuffer createMappedBuffer(MappedBufferDescriptor descriptor) {
            GlPersistentMappedBuffer buffer = new GlPersistentMappedBuffer(descriptor.sizeBytes(), descriptor.mapFlags());
            if (descriptor.label() != null) {
                buffer.name(descriptor.label());
            }
            return buffer;
        }
    }

    private static final class OpenGlGpuCommandList implements GpuCommandList {
        @Override
        public void begin() {
        }

        @Override
        public void end() {
        }

        @Override
        public void beginRenderPass(int framebufferId) {
            glBindFramebuffer(GL_FRAMEBUFFER, framebufferId);
        }

        @Override
        public void endRenderPass() {
        }

        @Override
        public void bindPipeline(GpuPipeline pipeline) {
        }

        @Override
        public void bindDescriptors(int firstUnit, int... textureIds) {
            for (int i = 0; i < textureIds.length; i++) {
                glBindTextureUnit(firstUnit + i, textureIds[i]);
            }
        }

        @Override
        public void bindImageTexture(int unit, int textureId, int level, boolean layered, int layer, int access, int format) {
            glBindImageTexture(unit, textureId, level, layered, layer, access, format);
        }

        @Override
        public void bindSampler(int unit, int samplerId) {
            glBindSampler(unit, samplerId);
        }

        @Override
        public void draw(int mode, int vertexCount, int instanceCount, int firstVertex, int firstInstance) {
            glDrawArraysInstancedBaseInstance(mode, firstVertex, vertexCount, instanceCount, firstInstance);
        }

        @Override
        public void drawIndexed(int mode, int indexCount, int indexType, long indicesOffset, int instanceCount, int baseVertex, int baseInstance) {
            glDrawElementsInstancedBaseVertexBaseInstance(mode, indexCount, indexType, indicesOffset, instanceCount, baseVertex, baseInstance);
        }

        @Override
        public void dispatchCompute(int groupCountX, int groupCountY, int groupCountZ) {
            glDispatchCompute(groupCountX, groupCountY, groupCountZ);
        }

        @Override
        public void bufferBarrier(int barriers) {
            glMemoryBarrier(barriers);
        }

        @Override
        public void imageBarrier(int barriers) {
            glMemoryBarrier(barriers);
        }

        @Override
        public void setUniformMatrix4fv(int location, int count, boolean transpose, long value) {
            nglUniformMatrix4fv(location, count, transpose, value);
        }

        @Override
        public void setUniform4f(int location, float v0, float v1, float v2, float v3) {
            glUniform4f(location, v0, v1, v2, v3);
        }

        @Override
        public void setUniform2f(int location, float v0, float v1) {
            glUniform2f(location, v0, v1);
        }

        @Override
        public void setColorMask(boolean red, boolean green, boolean blue, boolean alpha) {
            glColorMask(red, green, blue, alpha);
        }

        @Override
        public void enable(int cap) {
            glEnable(cap);
        }

        @Override
        public void disable(int cap) {
            glDisable(cap);
        }

        @Override
        public void blendFuncSeparate(int srcRgb, int dstRgb, int srcAlpha, int dstAlpha) {
            glBlendFuncSeparate(srcRgb, dstRgb, srcAlpha, dstAlpha);
        }

        @Override
        public void depthFunc(int func) {
            glDepthFunc(func);
        }

        @Override
        public void stencilOp(int sfail, int dpfail, int dppass) {
            glStencilOp(sfail, dpfail, dppass);
        }

        @Override
        public void stencilFunc(int func, int ref, int mask) {
            glStencilFunc(func, ref, mask);
        }

        @Override
        public void stencilMask(int mask) {
            glStencilMask(mask);
        }

        @Override
        public void clearNamedFramebufferfi(int framebuffer, int buffer, int drawbuffer, float depth, int stencil) {
            glClearNamedFramebufferfi(framebuffer, buffer, drawbuffer, depth, stencil);
        }

        @Override
        public int getNamedFramebufferAttachmentParameteri(int framebuffer, int attachment, int pname) {
            return glGetNamedFramebufferAttachmentParameteri(framebuffer, attachment, pname);
        }

        @Override
        public void textureParameterf(int texture, int pname, float param) {
            glTextureParameterf(texture, pname, param);
        }
    }
}
