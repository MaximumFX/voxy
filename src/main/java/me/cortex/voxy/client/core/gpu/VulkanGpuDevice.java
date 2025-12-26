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
        return new VulkanGpuShaderModule(descriptor.label(), descriptor.code());
    }

    @Override
    public GpuPipelineLayout createPipelineLayout(PipelineLayoutDescriptor descriptor) {
        return new VulkanGpuPipelineLayout(descriptor.label());
    }

    @Override
    public GpuPipeline createPipeline(PipelineDescriptor descriptor) {
        return new VulkanGpuPipeline(descriptor.label());
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
}
