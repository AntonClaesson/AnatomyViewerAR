package com.example.anatomyviewer.ar.ui

import android.widget.RadioGroup
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LiveData

@BindingAdapter("android:clearChecked")
fun clearChecked(radioGroup: RadioGroup, doClear: LiveData<Boolean>) {
    if (doClear.value == true){
        radioGroup.clearCheck()
    }
}