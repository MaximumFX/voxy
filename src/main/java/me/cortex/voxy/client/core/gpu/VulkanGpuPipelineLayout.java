package me.cortex.voxy.client.core.gpu;

import java.util.List;

final class VulkanGpuPipelineLayout implements GpuPipelineLayout {
    private final List<GpuDevice.DescriptorBinding> bindings;
    private final String label;

    VulkanGpuPipelineLayout(List<GpuDevice.DescriptorBinding> bindings, String label) {
        this.bindings = List.copyOf(bindings);
        this.label = label;
    }

    List<GpuDevice.DescriptorBinding> bindings() {
        return this.bindings;
    }

    @Override
    public void close() {
    }

    @Override
    public String toString() {
        return "VulkanGpuPipelineLayout{bindings=" + this.bindings.size() + ", label=" + this.label + "}";
    }
}
