package me.cortex.voxy.client.core.gpu;

import java.util.ArrayList;
import java.util.List;

final class VulkanGpuCommandList implements GpuCommandList {
    private final List<VulkanCommand> commands = new ArrayList<>();
    private final List<VulkanGpuSemaphore> waitSemaphores = new ArrayList<>();
    private final List<VulkanGpuSemaphore> signalSemaphores = new ArrayList<>();
    private boolean recording;

    @Override
    public void begin() {
        this.commands.clear();
        this.recording = true;
    }

    @Override
    public void end() {
        this.recording = false;
    }

    @Override
    public void beginRenderPass(int framebufferId) {
        record(context -> context.beginRenderPass(framebufferId));
    }

    @Override
    public void endRenderPass() {
        record(VulkanCommandContext::endRenderPass);
    }

    @Override
    public void bindPipeline(GpuPipeline pipeline) {
        record(context -> context.bindPipeline(pipeline));
    }

    @Override
    public void bindDescriptors(int firstUnit, int... textureIds) {
        int[] textures = textureIds.clone();
        record(context -> context.bindDescriptors(firstUnit, textures));
    }

    @Override
    public void bindImageTexture(int unit, int textureId, int level, boolean layered, int layer, int access, int format) {
        record(context -> context.bindImageTexture(unit, textureId, level, layered, layer, access, format));
    }

    @Override
    public void bindSampler(int unit, int samplerId) {
        record(context -> context.bindSampler(unit, samplerId));
    }

    @Override
    public void draw(int mode, int vertexCount, int instanceCount, int firstVertex, int firstInstance) {
        record(context -> context.draw(mode, vertexCount, instanceCount, firstVertex, firstInstance));
    }

    @Override
    public void drawIndexed(int mode, int indexCount, int indexType, long indicesOffset, int instanceCount, int baseVertex, int baseInstance) {
        record(context -> context.drawIndexed(mode, indexCount, indexType, indicesOffset, instanceCount, baseVertex, baseInstance));
    }

    @Override
    public void dispatchCompute(int groupCountX, int groupCountY, int groupCountZ) {
        record(context -> context.dispatch(groupCountX, groupCountY, groupCountZ));
    }

    @Override
    public void bufferBarrier(int barriers) {
        record(context -> context.bufferBarrier(barriers));
    }

    @Override
    public void imageBarrier(int barriers) {
        record(context -> context.imageBarrier(barriers));
    }

    @Override
    public void setUniformMatrix4fv(int location, int count, boolean transpose, long value) {
        record(context -> context.setUniformMatrix4fv(location, count, transpose, value));
    }

    @Override
    public void setUniform4f(int location, float v0, float v1, float v2, float v3) {
        record(context -> context.setUniform4f(location, v0, v1, v2, v3));
    }

    @Override
    public void setUniform2f(int location, float v0, float v1) {
        record(context -> context.setUniform2f(location, v0, v1));
    }

    @Override
    public void setColorMask(boolean red, boolean green, boolean blue, boolean alpha) {
        record(context -> context.setColorMask(red, green, blue, alpha));
    }

    @Override
    public void clearColor(float red, float green, float blue, float alpha) {
        record(context -> context.clearColor(red, green, blue, alpha));
    }

    @Override
    public void clear(int mask) {
        record(context -> context.clear(mask));
    }

    @Override
    public void enable(int cap) {
        record(context -> context.enable(cap));
    }

    @Override
    public void disable(int cap) {
        record(context -> context.disable(cap));
    }

    @Override
    public void blitNamedFramebuffer(int srcFramebuffer, int dstFramebuffer, int srcX0, int srcY0, int srcX1, int srcY1,
                                     int dstX0, int dstY0, int dstX1, int dstY1, int mask, int filter) {
        record(context -> context.blit(srcFramebuffer, dstFramebuffer, srcX0, srcY0, srcX1, srcY1,
                dstX0, dstY0, dstX1, dstY1, mask, filter));
    }

    @Override
    public void blendFuncSeparate(int srcRgb, int dstRgb, int srcAlpha, int dstAlpha) {
        record(context -> context.blendFunc(srcRgb, dstRgb, srcAlpha, dstAlpha));
    }

    @Override
    public void depthFunc(int func) {
        record(context -> context.depthFunc(func));
    }

    @Override
    public void stencilOp(int sfail, int dpfail, int dppass) {
        record(context -> context.stencilOp(sfail, dpfail, dppass));
    }

    @Override
    public void stencilFunc(int func, int ref, int mask) {
        record(context -> context.stencilFunc(func, ref, mask));
    }

    @Override
    public void stencilMask(int mask) {
        record(context -> context.stencilMask(mask));
    }

    @Override
    public void clearNamedFramebufferfi(int framebuffer, int buffer, int drawbuffer, float depth, int stencil) {
        record(context -> context.clearNamedFramebufferfi(framebuffer, buffer, drawbuffer, depth, stencil));
    }

    @Override
    public int getNamedFramebufferAttachmentParameteri(int framebuffer, int attachment, int pname) {
        VulkanCommandContext context = new VulkanCommandContext();
        context.getNamedFramebufferAttachmentParameteri(framebuffer, attachment, pname);
        return 0;
    }

    @Override
    public void textureParameterf(int texture, int pname, float param) {
        record(context -> context.textureParameterf(texture, pname, param));
    }

    void addWaitSemaphore(VulkanGpuSemaphore semaphore) {
        this.waitSemaphores.add(semaphore);
    }

    void addSignalSemaphore(VulkanGpuSemaphore semaphore) {
        this.signalSemaphores.add(semaphore);
    }

    void awaitSemaphores() {
        for (VulkanGpuSemaphore semaphore : this.waitSemaphores) {
            semaphore.await();
        }
    }

    void signalSemaphores() {
        for (VulkanGpuSemaphore semaphore : this.signalSemaphores) {
            semaphore.signal();
        }
    }

    void execute() {
        VulkanCommandContext context = new VulkanCommandContext();
        for (VulkanCommand command : this.commands) {
            command.execute(context);
        }
        this.commands.clear();
    }

    private void record(VulkanCommand command) {
        if (!this.recording) {
            begin();
        }
        this.commands.add(command);
    }

    private interface VulkanCommand {
        void execute(VulkanCommandContext context);
    }

    private static final class VulkanCommandContext {
        private boolean renderPassActive;
        private GpuPipeline pipeline;

        void beginRenderPass(int framebufferId) {
            this.renderPassActive = true;
        }

        void endRenderPass() {
            this.renderPassActive = false;
        }

        void bindPipeline(GpuPipeline pipeline) {
            this.pipeline = pipeline;
        }

        void bindDescriptors(int firstUnit, int[] textures) {
        }

        void bindImageTexture(int unit, int textureId, int level, boolean layered, int layer, int access, int format) {
        }

        void bindSampler(int unit, int samplerId) {
        }

        void draw(int mode, int vertexCount, int instanceCount, int firstVertex, int firstInstance) {
        }

        void drawIndexed(int mode, int indexCount, int indexType, long indicesOffset, int instanceCount, int baseVertex, int baseInstance) {
        }

        void dispatch(int groupCountX, int groupCountY, int groupCountZ) {
        }

        void bufferBarrier(int barriers) {
        }

        void imageBarrier(int barriers) {
        }

        void setUniformMatrix4fv(int location, int count, boolean transpose, long value) {
        }

        void setUniform4f(int location, float v0, float v1, float v2, float v3) {
        }

        void setUniform2f(int location, float v0, float v1) {
        }

        void setColorMask(boolean red, boolean green, boolean blue, boolean alpha) {
        }

        void clearColor(float red, float green, float blue, float alpha) {
        }

        void clear(int mask) {
        }

        void enable(int cap) {
        }

        void disable(int cap) {
        }

        void blit(int srcFramebuffer, int dstFramebuffer, int srcX0, int srcY0, int srcX1, int srcY1,
                  int dstX0, int dstY0, int dstX1, int dstY1, int mask, int filter) {
        }

        void blendFunc(int srcRgb, int dstRgb, int srcAlpha, int dstAlpha) {
        }

        void depthFunc(int func) {
        }

        void stencilOp(int sfail, int dpfail, int dppass) {
        }

        void stencilFunc(int func, int ref, int mask) {
        }

        void stencilMask(int mask) {
        }

        void clearNamedFramebufferfi(int framebuffer, int buffer, int drawbuffer, float depth, int stencil) {
        }

        int getNamedFramebufferAttachmentParameteri(int framebuffer, int attachment, int pname) {
            return 0;
        }

        void textureParameterf(int texture, int pname, float param) {
        }
    }
}
