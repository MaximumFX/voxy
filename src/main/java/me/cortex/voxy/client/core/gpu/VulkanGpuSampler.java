package me.cortex.voxy.client.core.gpu;

final class VulkanGpuSampler implements GpuSampler {
    private final String label;

    VulkanGpuSampler(String label) {
        this.label = label;
    }

    @Override
    public void close() {
    }

    @Override
    public String toString() {
        return "VulkanGpuSampler{label=" + this.label + "}";
    }
}
