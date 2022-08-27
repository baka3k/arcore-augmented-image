package com.baka3k.test.bankcardaugument.ar.node

import com.baka3k.test.bankcardaugument.utils.Logger
import com.google.ar.core.AugmentedImage
import com.google.ar.sceneform.AnchorNode
import kotlin.math.abs


abstract class AugmentedImageAnchorNode : AnchorNode() {
    companion object {
        const val SizeChangeThreshold = 0.001f // 1cm
    }

    // the real image size
    abstract val imageWidth: Float
    abstract val imageHeight: Float

    // the size get from AugmentedImage
    var arWidth: Float = 1f
    var arHeight: Float = 1f

    // get scaled size from  arSize / imageSize
    var scaledWidth: Float = 1f

    var scaledHeight: Float = 1f
    open fun init(image: AugmentedImage): AugmentedImageAnchorNode {
        Logger.d("${javaClass.simpleName} initialized size(${image.extentX}/${image.extentZ}")
        // Set the anchor based on the center of the image.
        anchor = image.createAnchor(image.centerPose)
        updateSize(abs(image.extentX), abs(image.extentZ))
        onInit()
        return this
    }

    fun update(image: AugmentedImage): Boolean {
        val nWidth = abs(image.extentX)
        val nHeight = abs(image.extentZ)
        if (abs(nWidth - arWidth) > SizeChangeThreshold || abs(nHeight - arHeight) > SizeChangeThreshold) {
            updateSize(nWidth, nHeight)
            Logger.d("${javaClass.simpleName} initialized image.extentX/image.extentZ ${nWidth}/${nHeight}")
            this.children.forEach {
                if (it is AugmentedImageNode) {
                    it.initLayout()
                    it.modifyLayout()
                }
            }
            return true
        }

        return false
    }

    private fun updateSize(width: Float, height: Float) {
        arWidth = width
        arHeight = height

        scaledWidth = arWidth / imageWidth
        scaledHeight = arHeight / imageHeight
        Logger.d("#updateSize() arSize($arWidth,$arHeight) imageSize($imageWidth,$imageHeight) scale($scaledWidth,$scaledHeight) ")
    }

    protected abstract fun onInit()
}