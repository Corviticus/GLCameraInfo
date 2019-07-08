package com.example.cameracheckup

import android.util.Range
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CameraViewModel : ViewModel() {

    // LEGACY, LIMITED or FULL
    val supportLevel: MutableLiveData<String> by lazy { MutableLiveData<String>() }

    val evCompValues: MutableLiveData<Range<Int>>? by lazy { MutableLiveData<Range<Int>>() }
    val isoValues: MutableLiveData<Range<Int>>? by lazy { MutableLiveData<Range<Int>>() }
    val exposureValues: MutableLiveData<Range<Long>>? by lazy { MutableLiveData<Range<Long>>() }

    val supportsEVComp: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }
    val supportsISO: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }
    val supportsEV: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }
    val supportsFocusLock: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }
}

