package com.baka3k.test.bankcardaugument.ar

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.baka3k.test.bankcardaugument.R
import com.baka3k.test.bankcardaugument.ar.config.ARConfig
import com.baka3k.test.bankcardaugument.ar.node.AugmentedImageAnchorNode
import com.baka3k.test.bankcardaugument.ar.resource.ArResources
import com.baka3k.test.bankcardaugument.ar.scene.BankCardScene
import com.baka3k.test.bankcardaugument.ar.scene.EarthScene
import com.baka3k.test.bankcardaugument.ar.scene.IdolScene
import com.baka3k.test.bankcardaugument.utils.GlUtils
import com.baka3k.test.bankcardaugument.utils.Logger
import com.google.ar.core.*
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.Sun
import com.google.ar.sceneform.ux.ArFragment
import kotlin.math.abs

open class AugmentedImageSampleFragment : ArFragment() {
    private val trackableMap = mutableMapOf<String, AugmentedImageAnchorNode>()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        view?.visibility = View.GONE
        configARView()
        return view
    }

    private var currentNodeName = ""
    private val swipeAnGestureDetector =
        GestureDetector(null, object : GestureDetector.SimpleOnGestureListener() {
            private val SWIPE_DISTANCE_THRESHOLD = 100
            private val SWIPE_VELOCITY_THRESHOLD = 100

            override fun onDown(e: MotionEvent?): Boolean {
                return true
            }

            override fun onFling(
                e1: MotionEvent,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                val sampleScene = trackableMap[currentNodeName] as? EarthScene

                if (sampleScene != null && sampleScene.isActive) {
                    val distanceX = e2.x - e1.x
                    val distanceY = e2.y - e1.y
                    if (abs(distanceX) > abs(distanceY) && abs(distanceX) > SWIPE_DISTANCE_THRESHOLD && abs(
                            velocityX
                        ) > SWIPE_VELOCITY_THRESHOLD
                    ) {
                        if (distanceX > 0) {
                            sampleScene.forwardScene()
                        } else {
                            sampleScene.backwardScene()
                        }

                        return true
                    }
                }

                return false
            }
        })

    private fun configARView() {
        planeDiscoveryController.hide()
        planeDiscoveryController.setInstructionView(null)
        arSceneView.planeRenderer.isEnabled = false

        arSceneView.scene.setOnTouchListener { _, motionEvent ->
            swipeAnGestureDetector.onTouchEvent(motionEvent)
        }
        arSceneView.scene.addOnUpdateListener(::onUpdateFrame)
        ArResources.init(this.requireContext()).handle { _, _ ->
            view?.visibility = View.VISIBLE
        }
    }

    private fun onUpdateFrame(frameTime: FrameTime?) {
        val frame = arSceneView.arFrame

        if (frame == null || frame.camera.trackingState != TrackingState.TRACKING) {
            //Logger.w("#onUpdateFrame() Frame is not stable - do not handle $frame")
            return
        }
        frame.getUpdatedTrackables(AugmentedImage::class.java).forEach { image ->
            when (image.trackingState) {
                TrackingState.TRACKING -> {
                    if (trackableMap.contains(image.name)) {
                        if (trackableMap[image.name]?.update(image) == true) {
                            Logger.d("update node: ${image.name}(${image.index}), pose: ${image.centerPose}, ex: ${image.extentX}, ez: ${image.extentZ}")
                        }
                    } else {
                        clearArView()
                        if (image.name.equals("001.jpg")) {
                            createIdolArNode(image)
                        } else if (image.name.equals("004.png") || image.name.equals("003.png")) {
                            createCardNode(image)
                        } else {
                            createEarthArNode(image)
                        }

                    }
                }
                TrackingState.STOPPED -> {
                    Logger.d("remove node: ${image.name}(${image.index})")
                    Toast.makeText(context, "${image.name} removed", Toast.LENGTH_LONG).show()
                    trackableMap.remove(image.name).let {
                        arSceneView.scene.removeChild(it)
                    }
                }
                else -> {
                    Logger.w("#onUpdateFrame() Unknow State - Do nothing ${image.trackingState} ${image.name}")
                }
            }
        }
    }

    private fun createCardNode(image: AugmentedImage) {
        Logger.w("#onUpdateFrame() createCardNode ${image.trackingState}")
        val node = BankCardScene().init(image)
        createArNode(image, node)
        loadTestData()

    }

    private fun loadTestData() {
        val rootView = ArResources.cardViewRenderable.get().view
        if (rootView != null) {
            val recyclerView = rootView.findViewById<RecyclerView>(R.id.recycleView)
            recyclerView?.adapter = TestAdapter()
        }
    }


    private fun createEarthArNode(image: AugmentedImage) {
        Logger.w("#onUpdateFrame() createEarthArNode ${image.trackingState}")
        val node = EarthScene().init(image)
        createArNode(image, node)
    }

    private fun createIdolArNode(image: AugmentedImage) {
        val node = IdolScene().init(image)
        createArNode(image, node)
    }

    private fun createArNode(
        image: AugmentedImage,
        node: AugmentedImageAnchorNode
    ) {
        Logger.d("#createArNode() : ${image.name}(${image.index}), pose: ${image.centerPose}, ex: ${image.extentX}, ez: ${image.extentZ}")

        currentNodeName = image.name
        trackableMap[image.name] = node
        arSceneView.scene.addChild(node)

        Toast.makeText(context, "${image.name} added", Toast.LENGTH_LONG).show()
    }

    override fun onPause() {
        arSceneView.scene.removeOnUpdateListener(::onUpdateFrame)
        clearArView()
        super.onPause()
    }

    private fun clearArView() {
        trackableMap.forEach {
            arSceneView.scene.removeChild(it.value)
        }
        trackableMap.clear()
    }

    fun reset() {
        clearArView()
        val children = ArrayList(arSceneView.scene.children)
        children.forEach { node ->
            if (node is AnchorNode) {
                if (node.anchor != null) {
                    node.anchor?.detach()
                }
            }
            if (node !is com.google.ar.sceneform.Camera && node !is Sun) {
                node.setParent(null)
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        GlUtils.configOpenGLMode(context)
    }

    override fun getSessionConfiguration(session: Session?): Config {
        val config = super.getSessionConfiguration(session)
        ARConfig.configArTarget(session, config, requireContext())
        return config
    }

    override fun onResume() {
        super.onResume()
        updateCameraConfig()
    }

    private fun updateCameraConfig() {
        val session = Session(context)
        val configuration = Config(session)
        configuration.focusMode = Config.FocusMode.AUTO // auto focus
        configuration.updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
        configuration.depthMode = Config.DepthMode.AUTOMATIC
        session.configure(configuration)
        // camera auto fix focust at time set autofocus, so we need trick by pause and resume again
        arSceneView.pause()
        arSceneView.setupSession(session)
        arSceneView.resume()
    }
}