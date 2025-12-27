package me.cortex.voxy.client.core.gpu;

final class VulkanGpuDevice implements GpuDevice {
    @Override
    public GpuBuffer createBuffer(BufferDescriptor descriptor) {
        return VulkanGpuBuffer.unmapped(descriptor);
    }

    @Override
    public GpuTexture createTexture(TextureDescriptor descriptor) {
        VulkanGpuTexture texture = new VulkanGpuTexture();
        texture.store(descriptor.format(), descriptor.levels(), descriptor.width(), descriptor.height());
        if (descriptor.label() != null) {
            texture.name(descriptor.label());
        }
        return texture;
    }

    @Override
    public GpuSampler createSampler(SamplerDescriptor descriptor) {
        return new VulkanGpuSampler(descriptor.label());
    }

    @Override
    public GpuShaderModule createShaderModule(ShaderModuleDescriptor descriptor) {
        byte[] code = descriptor.code();
        if (code == null && descriptor.resourceId() != null) {
            code = GpuShaderResources.loadSpirv(descriptor.resourceId());
        }
        if (code == null) {
            throw new IllegalArgumentException("Vulkan shader module requires SPIR-V code or resource id");
        }
        return new VulkanGpuShaderModule(descriptor.label(), code);
    }

    @Override
    public GpuPipelineLayout createPipelineLayout(PipelineLayoutDescriptor descriptor) {
        return new VulkanGpuPipelineLayout(descriptor.bindings(), descriptor.label());
    }

    @Override
    public GpuPipeline createPipeline(PipelineDescriptor descriptor) {
        if (!(descriptor.layout() instanceof VulkanGpuPipelineLayout layout)) {
            throw new IllegalArgumentException("Vulkan pipeline requires a Vulkan pipeline layout");
        }
        if (descriptor.stages().isEmpty()) {
            throw new IllegalArgumentException("Vulkan pipeline requires at least one shader stage");
        }
        return new VulkanGpuPipeline(layout, descriptor.stages(), descriptor.label());
    }

    @Override
    public GpuFence createFence() {
        return new VulkanGpuFence();
    }

    @Override
    public GpuQueryPool createQueryPool(QueryPoolDescriptor descriptor) {
        return new VulkanGpuQueryPool(descriptor.queryCount(), descriptor.label());
    }

    @Override
    public GpuBuffer createMappedBuffer(MappedBufferDescriptor descriptor) {
        return VulkanGpuBuffer.mapped(descriptor);
    }

    @Override
    public void flushMappedRange(GpuBuffer buffer, long offset, long size) {
    }

    @Override
    public void copyBuffer(GpuBuffer src, GpuBuffer dst, long srcOffset, long dstOffset, long size) {
        if (!(src instanceof VulkanGpuBuffer srcBuffer) || !(dst instanceof VulkanGpuBuffer dstBuffer)) {
            throw new IllegalArgumentException("Vulkan buffer copy requires VulkanGpuBuffer instances");
        }
        long srcAddress = srcBuffer.deviceAddress() + srcOffset;
        long dstAddress = dstBuffer.deviceAddress() + dstOffset;
        org.lwjgl.system.MemoryUtil.memCopy(srcAddress, dstAddress, size);
    }

    @Override
    public void bufferBarrier(int barrierBits) {
        VulkanPipelineBarrier.fromGlBarrierBits(barrierBits);
    }

    @Override
    public void waitForIdle() {
        this.queue.waitForIdle();
    }
}
