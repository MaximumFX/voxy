package me.cortex.voxy.client.core.gpu;

final class VulkanGpuPipelineLayout implements GpuPipelineLayout {
    private final String label;

    VulkanGpuPipelineLayout(String label) {
        this.label = label;
    }

    @Override
    public void close() {
    }

    @Override
    public String toString() {
        return "VulkanGpuPipelineLayout{label=" + this.label + "}";
    }
}
