package me.cortex.voxy.client.core.gpu;

/**
 * Per-frame command recording interface.
 */
public interface GpuCommandList {
    void begin();

    void end();

    void beginRenderPass(int framebufferId);

    void endRenderPass();

    void bindPipeline(GpuPipeline pipeline);

    void bindDescriptors(int firstUnit, int... textureIds);

    void bindImageTexture(int unit, int textureId, int level, boolean layered, int layer, int access, int format);

    void bindSampler(int unit, int samplerId);

    void draw(int mode, int vertexCount, int instanceCount, int firstVertex, int firstInstance);

    void drawIndexed(int mode, int indexCount, int indexType, long indicesOffset, int instanceCount, int baseVertex, int baseInstance);

    void dispatchCompute(int groupCountX, int groupCountY, int groupCountZ);

    void bufferBarrier(int barriers);

    void imageBarrier(int barriers);

    void setUniformMatrix4fv(int location, int count, boolean transpose, long value);

    void setUniform4f(int location, float v0, float v1, float v2, float v3);

    void setUniform2f(int location, float v0, float v1);

    void setColorMask(boolean red, boolean green, boolean blue, boolean alpha);

    void enable(int cap);

    void disable(int cap);

    void blendFuncSeparate(int srcRgb, int dstRgb, int srcAlpha, int dstAlpha);

    void depthFunc(int func);

    void stencilOp(int sfail, int dpfail, int dppass);

    void stencilFunc(int func, int ref, int mask);

    void stencilMask(int mask);

    void clearNamedFramebufferfi(int framebuffer, int buffer, int drawbuffer, float depth, int stencil);

    int getNamedFramebufferAttachmentParameteri(int framebuffer, int attachment, int pname);

    void textureParameterf(int texture, int pname, float param);
}
