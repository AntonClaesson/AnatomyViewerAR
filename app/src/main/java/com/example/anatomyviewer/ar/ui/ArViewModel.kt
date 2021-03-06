package com.example.anatomyviewer.ar.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.anatomyviewer.ar.helpers.UiEvent

class ArViewModel: ViewModel() {
    private val TAG: String = ArViewModel::class.java.toString()

    //Image data
    val IMAGE_1_NAME: String = "hand_bone.png"
    val IMAGE_2_NAME: String = "abdomen_no_skin.png"

    //region LiveData
    private var _uiEvents = MutableLiveData<UiEvent?>(null)
    val uiEvents: LiveData<UiEvent?> = _uiEvents

    private val _startQuizBtnVisible = MutableLiveData<Boolean>(false)
    val startQuizBtnVisible: LiveData<Boolean> = _startQuizBtnVisible

    private val _settingsBtnVisible = MutableLiveData<Boolean>(false)
    val settingsBtnVisible: LiveData<Boolean> = _settingsBtnVisible

    private val _settingsVisible = MutableLiveData<Boolean>(false)
    val settingsVisible: LiveData<Boolean> = _settingsVisible

    private val _modelSkinVisibility = MutableLiveData<Boolean>(false)
    val modelSkinVisibility: LiveData<Boolean> = _modelSkinVisibility
    //endregion

    //region ui changes
    fun setStartQuizBtnVisibility(value: Boolean){
        _startQuizBtnVisible.value = value
    }

    fun setSettingsBtnVisibility(value: Boolean){
        _settingsBtnVisible.value = value
     }

    //region button events
    fun resetBtnClicked(){
        _uiEvents.value = UiEvent.RESET_BUTTON_CLICKED
    }

    fun startQuizBtnClicked(){
        _uiEvents.value = UiEvent.START_QUIZ_BUTTON_CLICKED
    }

    fun settingsBtnClicked(){
        _settingsVisible.value = true
    }

    fun confirmSettingsBtnClicked(){
        _settingsVisible.value = false
        _uiEvents.value = UiEvent.SETTINGS_CONFIRMED_CLICKED
    }

    fun hideSkinSetting(){
        _modelSkinVisibility.value = false
    }

    fun showSkinSetting(){
        _modelSkinVisibility.value = true
    }
    //endregion
}

