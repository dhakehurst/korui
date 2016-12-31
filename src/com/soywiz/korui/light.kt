package com.soywiz.korui

import com.jtransc.annotation.JTranscMethodBody
import com.soywiz.kimage.bitmap.Bitmap
import com.soywiz.korio.vfs.VfsFile
import com.soywiz.korui.awt.AwtLightComponents

open class LightComponents {
    companion object {
        // Types
        val TYPE_FRAME = "frame"
        val TYPE_BUTTON = "button"
        val TYPE_IMAGE = "image"
        val TYPE_LABEL = "label"
        // Events
        val EVENT_CLICK = "click"
    }

    //open fun destroy(obj: Any): Unit {
    //}

    open fun create(type: String): Any = throw UnsupportedOperationException()
    open fun setParent(c: Any, parent: Any?): Unit = throw UnsupportedOperationException()
    open fun setEventHandler(c: Any, type: String, handler: () -> Unit): Unit = throw UnsupportedOperationException()
    open fun setText(c: Any, text: String): Unit = throw UnsupportedOperationException()
    open fun setImage(c: Any, bmp: Bitmap): Unit = throw UnsupportedOperationException()
    open fun setVisible(c: Any, visible: Boolean): Unit = throw UnsupportedOperationException()
    open fun setBounds(c: Any, x: Int, y: Int, width: Int, height: Int): Unit = throw UnsupportedOperationException()
    open suspend fun dialogAlert(c: Any, message: String): Unit = throw UnsupportedOperationException()
    open suspend fun dialogPrompt(c: Any, message: String): String = throw UnsupportedOperationException()
    open suspend fun dialogOpenFile(c: Any, filter: String): VfsFile = throw UnsupportedOperationException()
}

val light by lazy { _LightComponents() }

@JTranscMethodBody(target = "js", value = """
    return {% CONSTRUCTOR com.soywiz.korui.HtmlLightComponents:()V %}();
""")
fun _LightComponents() = AwtLightComponents()
