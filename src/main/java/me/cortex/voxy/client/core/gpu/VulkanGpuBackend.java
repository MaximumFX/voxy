package me.cortex.voxy.client.core.gpu;

final class VulkanGpuBackend implements GpuBackend {
    private final VulkanGpuDevice device = new VulkanGpuDevice();
    private final VulkanGpuQueue queue = new VulkanGpuQueue();

    static boolean isSupported() {
        return Boolean.getBoolean("voxy.gpu.vulkan");
    }

    @Override
    public GpuDevice device() {
        return this.device;
    }

    @Override
    public GpuCommandList createCommandList() {
        return new VulkanGpuCommandList();
    }

    @Override
    public void submit(GpuCommandList commandList, GpuFence fence) {
        if (!(commandList instanceof VulkanGpuCommandList vulkanCommandList)) {
            throw new IllegalArgumentException("Unsupported command list type: " + commandList);
        }
        if (fence != null && !(fence instanceof VulkanGpuFence)) {
            throw new IllegalArgumentException("Unsupported fence type: " + fence);
        }
        VulkanGpuFence vulkanFence = (VulkanGpuFence) fence;
        this.queue.submit(vulkanCommandList, vulkanFence);
    }

    @Override
    public BackendType backendType() {
        return BackendType.VULKAN;
    }
}
