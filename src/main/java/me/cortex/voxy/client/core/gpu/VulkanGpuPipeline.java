package me.cortex.voxy.client.core.gpu;

import java.util.List;

final class VulkanGpuPipeline implements GpuPipeline {
    private final VulkanGpuPipelineLayout layout;
    private final List<GpuDevice.ShaderStageDescriptor> stages;
    private final String label;

    VulkanGpuPipeline(VulkanGpuPipelineLayout layout, List<GpuDevice.ShaderStageDescriptor> stages, String label) {
        this.layout = layout;
        this.stages = List.copyOf(stages);
        this.label = label;
    }

    @Override
    public void close() {
    }

    @Override
    public String toString() {
        return "VulkanGpuPipeline{stages=" + this.stages.size() + ", layout=" + this.layout + ", label=" + this.label + "}";
    }
}
