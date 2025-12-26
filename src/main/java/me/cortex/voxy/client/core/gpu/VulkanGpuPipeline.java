package me.cortex.voxy.client.core.gpu;

final class VulkanGpuPipeline implements GpuPipeline {
    private final String label;

    VulkanGpuPipeline(String label) {
        this.label = label;
    }

    @Override
    public void close() {
    }

    @Override
    public String toString() {
        return "VulkanGpuPipeline{label=" + this.label + "}";
    }
}
