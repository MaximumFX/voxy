package me.cortex.voxy.client.core.model;

import me.cortex.voxy.client.VoxyClient;
import me.cortex.voxy.client.core.gpu.GpuBuffer;
import me.cortex.voxy.client.core.gpu.GpuDevice;
import me.cortex.voxy.client.core.gpu.GpuTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.Identifier;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11C.GL_NEAREST;
import static org.lwjgl.opengl.GL11C.GL_NEAREST_MIPMAP_LINEAR;
import static org.lwjgl.opengl.GL12C.GL_TEXTURE_MAX_LOD;
import static org.lwjgl.opengl.GL12C.GL_TEXTURE_MIN_LOD;
import static org.lwjgl.opengl.GL30.glBindBufferBase;
import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.opengl.GL33C.glSamplerParameteri;
import static org.lwjgl.opengl.GL43.GL_SHADER_STORAGE_BUFFER;
import static org.lwjgl.opengl.GL45.glBindTextureUnit;

public class ModelStore {
    public static final int MODEL_SIZE = 64;
    final GpuBuffer modelBuffer;
    final GpuBuffer modelColourBuffer;
    final GpuTexture textures;
    public final int blockSampler = glGenSamplers();

    public ModelStore() {
        GpuDevice device = VoxyClient.getBackend().device();
        this.modelBuffer = device.createBuffer(new GpuDevice.BufferDescriptor(MODEL_SIZE * (1<<16), "ModelData"));
        this.modelColourBuffer = device.createBuffer(new GpuDevice.BufferDescriptor(4 * (1<<16), "ModelColour"));
        this.textures = device.createTexture(new GpuDevice.TextureDescriptor(
                GL_RGBA8,
                Integer.numberOfTrailingZeros(ModelFactory.MODEL_TEXTURE_SIZE),
                ModelFactory.MODEL_TEXTURE_SIZE * 3 * 256,
                ModelFactory.MODEL_TEXTURE_SIZE * 2 * 256,
                "ModelTextures"
        ));


        //Limit the mips of the texture to match that of the terrain atlas
        int mipLvl = ((TextureAtlas) Minecraft.getInstance().getTextureManager()
                .getTexture(Identifier.fromNamespaceAndPath("minecraft", "textures/atlas/blocks.png")))
                .maxMipLevel;

        glSamplerParameteri(this.blockSampler, GL_TEXTURE_MIN_FILTER, GL_NEAREST_MIPMAP_LINEAR);
        glSamplerParameteri(this.blockSampler, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glSamplerParameteri(this.blockSampler, GL_TEXTURE_MIN_LOD, 0);
        glSamplerParameteri(this.blockSampler, GL_TEXTURE_MAX_LOD, mipLvl);//Integer.numberOfTrailingZeros(ModelFactory.MODEL_TEXTURE_SIZE)
    }


    public void free() {
        this.modelBuffer.free();
        this.modelColourBuffer.free();
        this.textures.free();
        glDeleteSamplers(this.blockSampler);
    }


    public void bind(int modelBindingIndex, int colourBindingIndex, int textureBindingIndex) {
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, modelBindingIndex, this.modelBuffer.id());
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, colourBindingIndex, this.modelColourBuffer.id());
        glBindTextureUnit(textureBindingIndex, this.textures.id());
        glBindSampler(textureBindingIndex, this.blockSampler);
    }
}
