package me.cortex.voxy.client.core.gpu;

final class VulkanGpuShaderModule implements GpuShaderModule {
    private final String label;
    private final byte[] code;

    VulkanGpuShaderModule(String label, byte[] code) {
        this.label = label;
        this.code = code == null ? null : code.clone();
    }

    @Override
    public void close() {
    }

    @Override
    public String toString() {
        return "VulkanGpuShaderModule{label=" + this.label + ", codeSize=" + (this.code == null ? 0 : this.code.length) + "}";
    }
}
