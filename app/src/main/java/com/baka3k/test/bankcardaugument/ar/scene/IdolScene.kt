package com.baka3k.test.bankcardaugument.ar.scene

import com.baka3k.test.bankcardaugument.ArApplication
import com.baka3k.test.bankcardaugument.R
import com.baka3k.test.bankcardaugument.ar.node.AugmentedImageAnchorNode
import com.baka3k.test.bankcardaugument.ar.node.AugmentedImageNode
import com.baka3k.test.bankcardaugument.ar.node.AugmentedImageNodeGroup
import com.baka3k.test.bankcardaugument.ar.resource.ArResources

class IdolScene : AugmentedImageAnchorNode() {
    override val imageWidth: Float = 1F
    override val imageHeight: Float = 1F
    private var currentSceneIndex = 0
    private val sceneList = mutableListOf<AugmentedImageNodeGroup>()
    override fun onInit() {
        sceneList.add(VideoIdolAugmentedImageGroup().init(anchorNode = this))
    }

    override fun onActivate() {
        super.onActivate()
        changeScene(0)
    }

    private fun changeScene(index: Int) {
        currentSceneIndex = index
        sceneList.forEachIndexed { i, scene ->
            scene.isEnabled = i == currentSceneIndex
        }
    }

    fun forwardScene() {
        changeScene((currentSceneIndex + 1) % sceneList.size)
    }

    fun backwardScene() {
        changeScene((currentSceneIndex - 1 + sceneList.size) % sceneList.size)
    }
}

class VideoIdolAugmentedImageGroup : AugmentedImageNodeGroup() {
    override fun onInit() {
        VideoIdolAugmentedImageNode().init(anchorNode, this)
    }
}

class VideoIdolAugmentedImageNode : AugmentedImageNode(ArResources.videoRenderable) {
    override fun initLayout() {
        super.initLayout()
        val videoRatio =
            ArResources.videoPlayer.videoWidth.toFloat() / ArResources.videoPlayer.videoHeight
        val scaleRatio = 1.1F
        scaledWidth *= scaleRatio
        scaledHeight = scaledHeight * scaleRatio / videoRatio
        scaledDeep = 1f

        offsetZ = (scaledHeight / 2.0f)
        localRotation = ArResources.viewRenderableRotation
    }

    override fun onActivate() {
        super.onActivate()
        startIdolVideo()
    }

    private fun startIdolVideo() {
        ArResources.videoPlayer.reset()
        val fileDescriptor =
            ArApplication.applicationInstant.resources.openRawResourceFd(R.raw.karen)
        ArResources.videoPlayer.setDataSource(
            fileDescriptor.fileDescriptor,
            fileDescriptor.startOffset,
            fileDescriptor.declaredLength
        )
        ArResources.videoPlayer.prepare()
        ArResources.videoPlayer.start()
        fileDescriptor.close()
    }

    override fun onDeactivate() {
        super.onDeactivate()

        if (ArResources.videoPlayer.isPlaying) {
            ArResources.videoPlayer.pause()
        }
    }
}