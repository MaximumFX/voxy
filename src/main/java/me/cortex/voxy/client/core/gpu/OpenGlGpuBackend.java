package me.cortex.voxy.client.core.gpu;

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
            throw new UnsupportedOperationException("OpenGL buffer creation is not implemented yet");
        }

        @Override
        public GpuTexture createTexture(TextureDescriptor descriptor) {
            throw new UnsupportedOperationException("OpenGL texture creation is not implemented yet");
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
            throw new UnsupportedOperationException("OpenGL fence creation is not implemented yet");
        }

        @Override
        public GpuQueryPool createQueryPool(QueryPoolDescriptor descriptor) {
            throw new UnsupportedOperationException("OpenGL query pool creation is not implemented yet");
        }
    }

    private static final class OpenGlGpuCommandList implements GpuCommandList {
        @Override
        public void begin() {
        }

        @Override
        public void end() {
        }
    }
}
