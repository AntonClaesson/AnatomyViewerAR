package com.example.anatomyviewer.ar.di

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import com.example.anatomyviewer.ar.ARViewModel
import com.google.ar.sceneform.ArSceneView
import com.google.ar.sceneform.ux.TransformationSystem
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ArModule(
    private val viewModel: ARViewModel,
    private val arSceneView: ArSceneView,
    private val transformationSystem: TransformationSystem,
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner
) {
    @Provides
    @Singleton
    fun provideContext() = context

    @Provides
    @Singleton
    fun provideLifeCycleOwner() = lifecycleOwner

    @Provides
    @Singleton
    fun provideTransformationSystem() = transformationSystem

    @Provides
    @Singleton
    fun provideViewModel() = viewModel

    @Provides
    @Singleton
    fun provideArSceneView() = arSceneView
}
