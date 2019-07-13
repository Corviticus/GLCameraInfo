package com.example.glcamerainfo

import android.util.Range
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CameraViewModel : ViewModel() {


    // LEGACY, LIMITED or FULL
    val supportLevel: MutableLiveData<String> by lazy { MutableLiveData<String>() }

    // EV Compensation
    private val evMinCompValue: MutableLiveData<Int>? by lazy { MutableLiveData<Int>() }
    private val evMaxCompValue: MutableLiveData<Int>? by lazy { MutableLiveData<Int>() }
    val evCompValues: MutableLiveData<Range<Int>>? by lazy { MutableLiveData<Range<Int>>() }

    // ISO values
    private val isoMinValue: MutableLiveData<Int>? by lazy { MutableLiveData<Int>() }
    private val isoMaxValue: MutableLiveData<Int>? by lazy { MutableLiveData<Int>() }
    val isoValues: MutableLiveData<Range<Int>>? by lazy { MutableLiveData<Range<Int>>() }

    // EV values
    private val evMinValue: MutableLiveData<Long>? by lazy { MutableLiveData<Long>() }
    private val evMaxValue: MutableLiveData<Long>? by lazy { MutableLiveData<Long>() }
    val exposureValues: MutableLiveData<Range<Long>>? by lazy { MutableLiveData<Range<Long>>() }

    // The four categories of interest to this app
    val supportsEVComp: MutableLiveData<Boolean> = mutableLiveData(false)
    val supportsISO: MutableLiveData<Boolean> = mutableLiveData(false)
    val supportsEV: MutableLiveData<Boolean> = mutableLiveData(false)
    val supportsFocusLock: MutableLiveData<Boolean> = mutableLiveData(false)

    /** function to allow assignment of default values */
    private fun <T : Any?> mutableLiveData(defaultValue: T) = MutableLiveData<T>().apply { setValue(defaultValue) }
}
