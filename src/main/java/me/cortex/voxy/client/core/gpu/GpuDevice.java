package me.cortex.voxy.client.core.gpu;

/**
 * Device-level interface for immutable resource creation.
 */
public interface GpuDevice {
    record BufferDescriptor(long sizeBytes, String label) {}

    record TextureDescriptor(int width, int height, int depth, String label) {}

    record SamplerDescriptor(String label) {}

    record ShaderModuleDescriptor(byte[] code, String label) {}

    record PipelineLayoutDescriptor(String label) {}

    record PipelineDescriptor(String label) {}

    record QueryPoolDescriptor(int queryCount, String label) {}

    GpuBuffer createBuffer(BufferDescriptor descriptor);

    GpuTexture createTexture(TextureDescriptor descriptor);

    GpuSampler createSampler(SamplerDescriptor descriptor);

    GpuShaderModule createShaderModule(ShaderModuleDescriptor descriptor);

    GpuPipelineLayout createPipelineLayout(PipelineLayoutDescriptor descriptor);

    GpuPipeline createPipeline(PipelineDescriptor descriptor);

    GpuFence createFence();

    GpuQueryPool createQueryPool(QueryPoolDescriptor descriptor);
}
