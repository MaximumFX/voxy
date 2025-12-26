package me.cortex.voxy.client.core.gpu;

final class VulkanGpuQueryPool implements GpuQueryPool {
    private final int queryCount;
    private final String label;

    VulkanGpuQueryPool(int queryCount, String label) {
        this.queryCount = queryCount;
        this.label = label;
    }

    @Override
    public void close() {
    }

    @Override
    public String toString() {
        return "VulkanGpuQueryPool{queryCount=" + this.queryCount + ", label=" + this.label + "}";
    }
}
