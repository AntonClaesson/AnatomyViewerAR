package com.example.anatomyviewer.Models

import com.google.ar.sceneform.rendering.Material

class MaterialDefinition(val material: Material, var id: Int) {

    override fun equals(other: Any?): Boolean {
        (other as? MaterialDefinition)?.let {
            return it.id == this.id
        }
        return false
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}