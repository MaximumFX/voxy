package me.cortex.voxy.client.core.gpu;

/**
 * Entry point for GPU backends. Immutable resource creation is handled by {@link GpuDevice},
 * while per-frame command recording is handled by {@link GpuCommandList}.
 */
public interface GpuBackend {
    GpuDevice device();

    GpuCommandList createCommandList();

    void submit(GpuCommandList commandList, GpuFence fence);
}
