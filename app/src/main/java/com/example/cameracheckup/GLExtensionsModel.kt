package com.example.cameracheckup

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class GLExtensionsModel : ViewModel() {

    val supportsGL3: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    val glVersion: MutableLiveData<String> by lazy { MutableLiveData<String>() }

    val extensionName: MutableLiveData<MutableList<String>> by lazy { MutableLiveData<MutableList<String>>() }

}