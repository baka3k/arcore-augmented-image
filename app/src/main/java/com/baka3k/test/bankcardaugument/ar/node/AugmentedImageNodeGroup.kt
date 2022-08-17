package com.baka3k.test.bankcardaugument.ar.node

import com.google.ar.sceneform.Node

abstract class AugmentedImageNodeGroup : Node() {
    lateinit var anchorNode: AugmentedImageAnchorNode
    fun init(anchorNode: AugmentedImageAnchorNode): AugmentedImageNodeGroup {
        this.isEnabled = false
        this.anchorNode = anchorNode
        name = this.javaClass.simpleName.replace("AugmentedImageNodeGroup", "")
        setParent(anchorNode)
        onInit()
        return this
    }
    protected abstract fun onInit()
}