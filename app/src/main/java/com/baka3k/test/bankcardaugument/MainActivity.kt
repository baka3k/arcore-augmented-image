package com.baka3k.test.bankcardaugument

import android.view.View
import com.baka3k.test.bankcardaugument.ar.AugmentedImageSampleFragment


class MainActivity : ArBaseActivity() {
    override val viewId: Int = R.layout.activity_main
    private lateinit var arFragment: AugmentedImageSampleFragment
    override fun startAr() {
        addArFragment()
    }

    fun buttonOnClicked(view: View) {
//        arFragment.reset()
        addArFragment()
    }

    private fun addArFragment() {
        arFragment = AugmentedImageSampleFragment()
        supportFragmentManager.beginTransaction().replace(R.id.ar_fragment, arFragment).commit()
    }
}