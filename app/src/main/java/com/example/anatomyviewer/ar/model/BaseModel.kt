package com.example.anatomyviewer.ar.model
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.ux.TransformableNode

open class BaseModel() {

    var modelID: Int = -1

    var modelAnchorNode: AnchorNode? = null //Top level node attatched to the augmented image
    var baseNode: TransformableNode? = null //Node attached to the anchor node, containing the renderable

    var childModelIDs: MutableList<Int> = mutableListOf() //Renderables to attatch as child nodes to the base node

}