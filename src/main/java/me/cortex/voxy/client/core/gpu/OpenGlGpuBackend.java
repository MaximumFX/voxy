package me.cortex.voxy.client.core.gpu;

import me.cortex.voxy.client.core.gl.GlBuffer;
import me.cortex.voxy.client.core.gl.GlFence;
import me.cortex.voxy.client.core.gl.GlPersistentMappedBuffer;
import me.cortex.voxy.client.core.gl.GlTexture;

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
    }
}
