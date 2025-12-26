package me.cortex.voxy.client.core;

import me.cortex.voxy.client.VoxyClient;
import me.cortex.voxy.client.config.VoxyConfig;
import me.cortex.voxy.client.core.gl.GlFramebuffer;
import me.cortex.voxy.client.core.gpu.GpuDevice;
import me.cortex.voxy.client.core.gpu.GpuTexture;
import me.cortex.voxy.client.core.gl.shader.Shader;
import me.cortex.voxy.client.core.gl.shader.ShaderType;
import me.cortex.voxy.client.core.rendering.Viewport;
import me.cortex.voxy.client.core.rendering.hierachical.AsyncNodeManager;
import me.cortex.voxy.client.core.rendering.hierachical.HierarchicalOcclusionTraverser;
import me.cortex.voxy.client.core.rendering.hierachical.NodeCleaner;
import me.cortex.voxy.client.core.rendering.post.FullscreenBlit;
import me.cortex.voxy.client.core.rendering.util.DepthFramebuffer;
import net.minecraft.client.Minecraft;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;

import java.util.function.BooleanSupplier;

import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_ONE;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11C.GL_NEAREST;
import static org.lwjgl.opengl.GL11C.GL_RGBA8;
import static org.lwjgl.opengl.GL15.GL_READ_WRITE;
import static org.lwjgl.opengl.GL30C.*;
import static org.lwjgl.opengl.GL43.GL_DEPTH_STENCIL_TEXTURE_MODE;

public class NormalRenderPipeline extends AbstractRenderPipeline {
    private GpuTexture colourTex;
    private GpuTexture colourSSAOTex;
    private final GlFramebuffer fbSSAO = new GlFramebuffer();

    private final boolean useEnvFog;
    private final FullscreenBlit finalBlit;

    private final Shader ssaoCompute = Shader.make()
            .add(ShaderType.COMPUTE, "voxy:post/ssao.comp")
            .compile();

    protected NormalRenderPipeline(AsyncNodeManager nodeManager, NodeCleaner nodeCleaner, HierarchicalOcclusionTraverser traversal, BooleanSupplier frexSupplier) {
        super(nodeManager, nodeCleaner, traversal, frexSupplier, false);
        this.useEnvFog = VoxyConfig.CONFIG.useEnvironmentalFog;
        this.finalBlit = new FullscreenBlit("voxy:post/blit_texture_depth_cutout.frag",
                a->a.defineIf("USE_ENV_FOG", this.useEnvFog).define("EMIT_COLOUR"));
    }

    @Override
    protected int setup(Viewport<?> viewport, int sourceFB, int srcWidth, int srcHeight) {
        if (this.colourTex == null || this.colourTex.getHeight() != viewport.height || this.colourTex.getWidth() != viewport.width) {
            if (this.colourTex != null) {
                this.colourTex.free();
                this.colourSSAOTex.free();
            }
            this.fb.resize(viewport.width, viewport.height);

            GpuDevice device = VoxyClient.getBackend().device();
            this.colourTex = device.createTexture(new GpuDevice.TextureDescriptor(GL_RGBA8, 1, viewport.width, viewport.height, "Colour"));
            this.colourSSAOTex = device.createTexture(new GpuDevice.TextureDescriptor(GL_RGBA8, 1, viewport.width, viewport.height, "ColourSSAO"));

            this.fb.framebuffer.bind(GL_COLOR_ATTACHMENT0, this.colourTex).verify();
            this.fbSSAO.bind(this.fb.getDepthAttachmentType(), this.fb.getDepthTex()).bind(GL_COLOR_ATTACHMENT0, this.colourSSAOTex).verify();


            this.commandList.textureParameterf(this.colourTex.id(), GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            this.commandList.textureParameterf(this.colourTex.id(), GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            this.commandList.textureParameterf(this.colourSSAOTex.id(), GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            this.commandList.textureParameterf(this.colourSSAOTex.id(), GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            this.commandList.textureParameterf(this.fb.getDepthTex().id(), GL_DEPTH_STENCIL_TEXTURE_MODE, GL_DEPTH_COMPONENT);
        }

        this.initDepthStencil(sourceFB, this.fb.framebuffer.id, viewport.width, viewport.height, viewport.width, viewport.height);

        return this.fb.getDepthTex().id();
    }

    @Override
    protected void postOpaquePreTranslucent(Viewport<?> viewport) {
        this.ssaoCompute.bind();
        try (var stack = MemoryStack.stackPush()) {
            long ptr = stack.nmalloc(4*4*4);
            viewport.MVP.getToAddress(ptr);
            this.commandList.setUniformMatrix4fv(3, 1, false, ptr);//MVP
            viewport.MVP.invert(new Matrix4f()).getToAddress(ptr);
            this.commandList.setUniformMatrix4fv(4, 1, false, ptr);//invMVP
        }


        this.commandList.bindImageTexture(0, this.colourSSAOTex.id(), 0, false,0, GL_READ_WRITE, GL_RGBA8);
        this.commandList.bindDescriptors(1, this.fb.getDepthTex().id(), this.colourTex.id());

        this.commandList.dispatchCompute((viewport.width+31)/32, (viewport.height+31)/32, 1);

        this.commandList.beginRenderPass(this.fbSSAO.id);
    }

    @Override
    protected void finish(Viewport<?> viewport, int sourceFrameBuffer, int srcWidth, int srcHeight) {
        this.finalBlit.bind();
        if (this.useEnvFog) {
            float start = viewport.fogParameters.environmentalStart();
            float end = viewport.fogParameters.environmentalEnd();
            if (Math.abs(end-start)>1) {
                float invEndFogDelta = 1f / (end - start);
                float endDistance = Math.max(Minecraft.getInstance().gameRenderer.getRenderDistance(), 20*16);//TODO: make this constant a config option
                endDistance *= (float)Math.sqrt(3);
                float startDelta = -start * invEndFogDelta;
                this.commandList.setUniform4f(4, invEndFogDelta, startDelta, Math.clamp(endDistance*invEndFogDelta+startDelta, 0, 1),0);//
                this.commandList.setUniform4f(5, viewport.fogParameters.red(), viewport.fogParameters.green(), viewport.fogParameters.blue(), viewport.fogParameters.alpha());
            } else {
                this.commandList.setUniform4f(4, 0, 0, 0, 0);
                this.commandList.setUniform4f(5, 0, 0, 0, 0);
            }
        }

        this.commandList.bindDescriptors(3, this.colourSSAOTex.id());

        //Do alpha blending

        this.commandList.enable(GL_BLEND);
        this.commandList.blendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
        AbstractRenderPipeline.transformBlitDepth(this.commandList, this.finalBlit, this.fb.getDepthTex().id(), sourceFrameBuffer, viewport, new Matrix4f(viewport.vanillaProjection).mul(viewport.modelView));
        this.commandList.disable(GL_BLEND);
        //glBlitNamedFramebuffer(this.fbSSAO.id, sourceFrameBuffer, 0,0, viewport.width, viewport.height, 0,0, viewport.width, viewport.height, GL_COLOR_BUFFER_BIT, GL_NEAREST);
    }

    @Override
    public void setupAndBindOpaque(Viewport<?> viewport) {
        this.commandList.beginRenderPass(this.fb.framebuffer.id);
    }

    @Override
    public void setupAndBindTranslucent(Viewport<?> viewport) {
        this.commandList.beginRenderPass(this.fbSSAO.id);
    }

    @Override
    public void free() {
        this.finalBlit.delete();
        this.ssaoCompute.free();
        this.fbSSAO.free();
        if (this.colourTex != null) {
            this.colourTex.free();
            this.colourSSAOTex.free();
        }
        super.free0();
    }
}
