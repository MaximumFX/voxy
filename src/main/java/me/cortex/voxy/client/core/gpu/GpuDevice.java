package me.cortex.voxy.client.core.gpu;

import java.util.List;
import java.util.Objects;
import java.util.Set;

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

    record ShaderModuleDescriptor(byte[] code, String label, String resourceId) {
        public ShaderModuleDescriptor(byte[] code, String label) {
            this(code, label, null);
        }

        public static ShaderModuleDescriptor fromSpirvResource(String resourceId, String label) {
            return new ShaderModuleDescriptor(null, label, resourceId);
        }
    }

    enum ShaderStage {
        VERTEX,
        FRAGMENT,
        COMPUTE
    }

    enum DescriptorType {
        UNIFORM_BUFFER,
        STORAGE_BUFFER,
        SAMPLED_IMAGE,
        STORAGE_IMAGE,
        SAMPLER
    }

    record DescriptorBinding(int binding, DescriptorType type, int count, Set<ShaderStage> stages) {
        public DescriptorBinding {
            Objects.requireNonNull(type, "type");
            Objects.requireNonNull(stages, "stages");
        }
    }

    record ShaderStageDescriptor(GpuShaderModule module, ShaderStage stage, String entryPoint) {
        public ShaderStageDescriptor {
            Objects.requireNonNull(module, "module");
            Objects.requireNonNull(stage, "stage");
            Objects.requireNonNull(entryPoint, "entryPoint");
        }
    }

    record PipelineLayoutDescriptor(List<DescriptorBinding> bindings, String label) {
        public PipelineLayoutDescriptor(String label) {
            this(List.of(), label);
        }
    }

    record PipelineDescriptor(GpuPipelineLayout layout, List<ShaderStageDescriptor> stages, String label) {
        public PipelineDescriptor(GpuPipelineLayout layout, List<ShaderStageDescriptor> stages) {
            this(layout, stages, null);
        }

        public PipelineDescriptor(String label) {
            this(null, List.of(), label);
        }
    }

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

    default void flushMappedRange(GpuBuffer buffer, long offset, long size) {
    }

    default void copyBuffer(GpuBuffer src, GpuBuffer dst, long srcOffset, long dstOffset, long size) {
        throw new UnsupportedOperationException("Buffer copy not supported by this backend");
    }

    default void bufferBarrier(int barrierBits) {
    }

    default void waitForIdle() {
    }
}
