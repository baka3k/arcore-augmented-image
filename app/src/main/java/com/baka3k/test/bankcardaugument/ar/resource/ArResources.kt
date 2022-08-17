package com.baka3k.test.bankcardaugument.ar.resource

import android.content.Context
import android.media.MediaPlayer
import com.baka3k.test.bankcardaugument.R
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ExternalTexture
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.ViewRenderable
import java.util.concurrent.CompletableFuture

object ArResources {
    fun init(context: Context): CompletableFuture<Void> {
        val texture = initMediaPlayer(context)

        videoRenderable = ModelRenderable.builder()
            .setSource(context, com.google.ar.sceneform.rendering.R.raw.sceneform_view_renderable)
            .build().also {
            it.thenAccept { renderable ->
                renderable.material.setExternalTexture("viewTexture", texture)
            }
        }
        earthViewRenderable = ViewRenderable.builder().setView(context, R.layout.layout_earth)
            .setVerticalAlignment(ViewRenderable.VerticalAlignment.CENTER).build()
        return CompletableFuture.allOf(
            earthViewRenderable,
            videoRenderable
        )
    }

    private fun initMediaPlayer(context: Context): ExternalTexture {
        videoPlayer = MediaPlayer.create(context, R.raw.sample)
        val texture = ExternalTexture()
        videoPlayer.setSurface(texture.surface)
        videoPlayer.isLooping = true
        return texture
    }

    lateinit var earthViewRenderable: CompletableFuture<ViewRenderable>
    val viewRenderableRotation = Quaternion(Vector3(1f, 0f, 0f), -90f)

    lateinit var videoRenderable: CompletableFuture<ModelRenderable>
    lateinit var videoPlayer: MediaPlayer
}