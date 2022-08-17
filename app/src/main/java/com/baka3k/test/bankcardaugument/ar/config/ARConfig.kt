package com.baka3k.test.bankcardaugument.ar.config

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.baka3k.test.bankcardaugument.utils.Logger
import com.google.ar.core.AugmentedImageDatabase
import com.google.ar.core.Config
import com.google.ar.core.Session


object ARConfig {
    private fun optimizeCamera(config: Config) {
        config.focusMode = Config.FocusMode.AUTO // auto focus
        config.updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
        config.depthMode = Config.DepthMode.AUTOMATIC
        Logger.d("#ARConfig::#optimizeCamera() ${config.focusMode}")
    }

    fun configArTarget(
        session: Session?,
        config: Config, context: Context
    ) {
        optimizeCamera(config) // optimize camera
        if (USE_IMAGE_TARGET) {
            setImageAugmentTarget(config, session, context)
        } else {
            setDataBaseAugmentTarget(config, session, context)
        }
    }

    private fun setDataBaseAugmentTarget(
        config: Config,
        session: Session?, context: Context
    ) {
        config.augmentedImageDatabase = AugmentedImageDatabase.deserialize(
            session,
            context.resources.assets.open(IMAGE_DATABASE)
        )
    }

    private fun setImageAugmentTarget(config: Config, session: Session?, context: Context) {
        val augmentedImageDatabase = AugmentedImageDatabase(session)
        IMAGES.forEach {
            val augmentedImageBitmap: Bitmap =
                context.assets.open(it).use { inputStream ->
                    BitmapFactory.decodeStream(
                        inputStream
                    )
                }
            // add photo to detector
            // we can add many photo at runtime
            augmentedImageDatabase.addImage(it, augmentedImageBitmap)
        }
        config.augmentedImageDatabase = augmentedImageDatabase
    }
    /**
     * add image target at runtime
     * */
    fun addImageAugmentTarget(
        bitmap: Bitmap,
        photoname: String,
        config: Config,
        session: Session?,
    ) {
        val augmentedImageDatabase = AugmentedImageDatabase(session)
        augmentedImageDatabase.addImage(photoname, bitmap)
        config.augmentedImageDatabase = augmentedImageDatabase
    }


    private const val IMAGE_DATABASE = "arcore.imgdb"
    private const val USE_IMAGE_TARGET = false // select a special target image
    private val IMAGES = arrayOf("000.jpg","001.jpg")
}
