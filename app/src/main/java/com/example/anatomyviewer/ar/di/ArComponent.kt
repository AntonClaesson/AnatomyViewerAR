package com.example.anatomyviewer.ar.di
import com.example.anatomyviewer.ar.AnatomyViewerFragment
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [ArModule::class])
interface ArComponent {
    fun inject(target: AnatomyViewerFragment)
}
