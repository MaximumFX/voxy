package me.cortex.voxy.client.core.gpu;

/**
 * Device-level interface for immutable resource creation.
 */
public interface GpuDevice {
    record BufferDescriptor(long sizeBytes, String label, int flags, boolean zeroed) {
        public BufferDescriptor(long sizeBytes, String label) {
            this(sizeBytes, label, 0, true);
        }

        public BufferDescriptor(long sizeBytes, String label, int flags) {
            this(sizeBytes, label, flags, true);
        }

        public BufferDescriptor(long sizeBytes, String label, boolean zeroed) {
            this(sizeBytes, label, 0, zeroed);
        }
    }

    record TextureDescriptor(int format, int levels, int width, int height, int depth, String label) {
        public TextureDescriptor(int format, int levels, int width, int height, String label) {
            this(format, levels, width, height, 1, label);
        }
    }

    record SamplerDescriptor(String label) {}

    record ShaderModuleDescriptor(byte[] code, String label) {}

    record PipelineLayoutDescriptor(String label) {}

    record PipelineDescriptor(String label) {}

    record QueryPoolDescriptor(int queryCount, String label) {}

    record MappedBufferDescriptor(long sizeBytes, int mapFlags, String label) {
        public MappedBufferDescriptor(long sizeBytes, int mapFlags) {
            this(sizeBytes, mapFlags, null);
        }
    }

    GpuBuffer createBuffer(BufferDescriptor descriptor);

    GpuTexture createTexture(TextureDescriptor descriptor);

    GpuSampler createSampler(SamplerDescriptor descriptor);

    GpuShaderModule createShaderModule(ShaderModuleDescriptor descriptor);

    GpuPipelineLayout createPipelineLayout(PipelineLayoutDescriptor descriptor);

    GpuPipeline createPipeline(PipelineDescriptor descriptor);

    GpuFence createFence();

    GpuQueryPool createQueryPool(QueryPoolDescriptor descriptor);

    GpuBuffer createMappedBuffer(MappedBufferDescriptor descriptor);
}
