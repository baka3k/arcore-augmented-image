package com.baka3k.test.bankcardaugument.ar.scene

import com.baka3k.test.bankcardaugument.ar.node.AugmentedImageAnchorNode
import com.baka3k.test.bankcardaugument.ar.resource.ArResources
import com.baka3k.test.bankcardaugument.utils.Logger
import com.google.ar.core.AugmentedImage
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.Renderable
import java.util.concurrent.CompletableFuture
import kotlin.math.abs

class TestScene : BankCardAugmentedImageAnchorNode() {
    override val imageWidth: Float = 0.856F
    override val imageHeight: Float = 0.5398F

    private var currentSceneIndex = 0
    private val sceneList = mutableListOf<BankCardAugmentedImageNodeGroup>()
    override fun onInit() {
        sceneList.add(CardAugmentedImageGroup().init(anchorNode = this))
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
abstract class BankCardAugmentedImageNodeGroup : Node() {
    lateinit var anchorNode: BankCardAugmentedImageAnchorNode
    fun init(anchorNode: BankCardAugmentedImageAnchorNode): BankCardAugmentedImageNodeGroup {
        this.isEnabled = false
        this.anchorNode = anchorNode
        name = this.javaClass.simpleName.replace("AugmentedImageNodeGroup", "")
        setParent(anchorNode)
        onInit()
        return this
    }
    protected abstract fun onInit()
}

class CardAugmentedImageGroup : BankCardAugmentedImageNodeGroup() {
    override fun onInit() {
        CardAugmentedImageNode().init(anchorNode, this)
    }

}

class CardAugmentedImageNode : BankCardNode(ArResources.cardViewRenderable) {
    override fun initLayout() {
        super.initLayout()
        localRotation = ArResources.viewRenderableRotation
    }
}

abstract class BankCardNode(resource: CompletableFuture<*>? = null) : Node() {
    var scaledWidth: Float = 1f
    var scaledHeight: Float = 1f
    var scaledDeep: Float = 1f
    var offsetX: Float = 0.0f
    var offsetY: Float = 0.0f
    var offsetZ: Float = 0.0f
    lateinit var anchorNode: BankCardAugmentedImageAnchorNode
    init {
        if (resource != null) {
            renderable = resource.getNow(null) as? Renderable
        }
    }
    fun init(
        anchorNode: BankCardAugmentedImageAnchorNode,
        parrentNode: BankCardAugmentedImageNodeGroup? = null
    ): BankCardNode {
        this.anchorNode = anchorNode
        name = this.javaClass.simpleName.replace("AugmentedImageNode", "")
        setParent(parrentNode ?: anchorNode)
        initLayout()
        modifyLayout()
        return this
    }

    open fun initLayout() {
        scaledWidth = anchorNode.scaledWidth
        scaledHeight = anchorNode.scaledHeight
        scaledDeep = anchorNode.scaledWidth
    }

    fun modifyLayout(config: BankCardNode.() -> Unit) {
        config()
        modifyLayout()
    }

    open fun modifyLayout() {
        localScale = Vector3(scaledWidth, scaledHeight, scaledDeep)
        localPosition = Vector3(offsetX, offsetY, offsetZ)
        Logger.d("${javaClass.simpleName} modifyLayout: scale: ($scaledWidth, $scaledHeight, $scaledDeep), offset x,y,z: ($offsetX, $offsetY, $offsetZ)")
    }
}



abstract class BankCardAugmentedImageAnchorNode : AugmentedImageAnchorNode() {

    override fun init(image: AugmentedImage): BankCardAugmentedImageAnchorNode {
        Logger.d("${javaClass.simpleName} initialized size(${image.extentX}/${image.extentZ}")
        // Set the anchor based on the center of the image.
        anchor = image.createAnchor(image.centerPose)
        updateSizeNode(abs(image.extentX), abs(image.extentZ))
        onInit()
        return this
    }
    private fun updateSizeNode(width: Float, height: Float) {
        arWidth = width
        arHeight = height
        val ratio = arWidth/arHeight
        scaledWidth = arWidth / imageWidth
        scaledHeight = arHeight / imageHeight
        scaledHeight = scaledWidth / ratio
        Logger.d("#updateSize() arSize($arWidth,$arHeight) imageSize($imageWidth,$imageHeight) scale($scaledWidth,$scaledHeight) ratio:$ratio ")
    }
}
