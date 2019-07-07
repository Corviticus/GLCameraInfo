package com.example.cameracheckup

import android.util.Range
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CameraViewModel : ViewModel() {

    // LEGACY, LIMITED or FULL
    val supportLevel: MutableLiveData<String> by lazy { MutableLiveData() }

    //
    val supportsEVComp: MutableLiveData<Boolean> by lazy { MutableLiveData() }
    val supportsISO: MutableLiveData<Boolean> by lazy { MutableLiveData() }
    val supportsEV: MutableLiveData<Boolean> by lazy { MutableLiveData() }
    val supportsFocusLock: MutableLiveData<Boolean> by lazy { MutableLiveData() }

    //
    val evCompValues: MutableLiveData<Range<Int>> by lazy { MutableLiveData() }
    val isoValues: MutableLiveData<Range<Int>> by lazy { MutableLiveData() }
    val exposureValues: MutableLiveData<Range<Long>> by lazy { MutableLiveData() }
}


