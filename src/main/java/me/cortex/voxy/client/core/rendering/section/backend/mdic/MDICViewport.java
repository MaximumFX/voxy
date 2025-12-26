package me.cortex.voxy.client.core.rendering.section.backend.mdic;

import me.cortex.voxy.client.VoxyClient;
import me.cortex.voxy.client.core.gpu.GpuBuffer;
import me.cortex.voxy.client.core.gpu.GpuDevice;
import me.cortex.voxy.client.core.rendering.Viewport;
import me.cortex.voxy.client.core.rendering.hierachical.HierarchicalOcclusionTraverser;

public class MDICViewport extends Viewport<MDICViewport> {
    public final GpuBuffer drawCountCallBuffer;
    public final GpuBuffer drawCallBuffer;
    public final GpuBuffer positionScratchBuffer;
    public final GpuBuffer indirectLookupBuffer;
    public final GpuBuffer visibilityBuffer;

    public MDICViewport(int maxSectionCount) {
        GpuDevice device = VoxyClient.getBackend().device();
        this.drawCountCallBuffer = device.createBuffer(new GpuDevice.BufferDescriptor(1024, "MDICDrawCount")).zero();
        this.drawCallBuffer = device.createBuffer(new GpuDevice.BufferDescriptor(5L * 4 * (400_000 + 100_000 + 100_000), "MDICDrawCalls")).zero();
        this.positionScratchBuffer = device.createBuffer(new GpuDevice.BufferDescriptor(8L * 400_000, "MDICPositionScratch")).zero();
        this.indirectLookupBuffer = device.createBuffer(new GpuDevice.BufferDescriptor(HierarchicalOcclusionTraverser.MAX_QUEUE_SIZE * 4L + 4, "MDICIndirectLookup"));
        this.visibilityBuffer = device.createBuffer(new GpuDevice.BufferDescriptor(maxSectionCount * 4L, "MDICVisibility"));
    }

    @Override
    protected void delete0() {
        super.delete0();
        this.visibilityBuffer.free();
        this.indirectLookupBuffer.free();
        this.drawCountCallBuffer.free();
        this.drawCallBuffer.free();
        this.positionScratchBuffer.free();
    }

    @Override
    public GpuBuffer getRenderList() {
        return this.indirectLookupBuffer;
    }
}
