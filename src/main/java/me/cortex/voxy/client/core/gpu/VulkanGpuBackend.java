package me.cortex.voxy.client.core.gpu;

final class VulkanGpuBackend implements GpuBackend {
    static boolean isSupported() {
        return Boolean.getBoolean("voxy.gpu.vulkan");
    }

    @Override
    public GpuDevice device() {
        throw new UnsupportedOperationException("Vulkan backend is not implemented yet");
    }

    @Override
    public GpuCommandList createCommandList() {
        throw new UnsupportedOperationException("Vulkan backend is not implemented yet");
    }

    @Override
    public void submit(GpuCommandList commandList, GpuFence fence) {
        throw new UnsupportedOperationException("Vulkan backend is not implemented yet");
    }
}
