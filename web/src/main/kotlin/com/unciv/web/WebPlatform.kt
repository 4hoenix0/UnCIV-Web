package com.unciv.web

import com.badlogic.gdx.Gdx
import com.unciv.logic.files.PlatformSaverLoader
import com.unciv.utils.DefaultLogBackend
import com.unciv.utils.PlatformDisplay
import com.unciv.utils.Tag
import java.io.InputStream
import java.io.OutputStream

/** Release-mode logging (no timestamps via java.time.Instant, which TeaVM may not have). */
class WebLogBackend : DefaultLogBackend() {
    override fun debug(tag: Tag, curThreadName: String, msg: String) {
        Gdx.app?.log(tag.name, msg) ?: println("[${tag.name}] $msg")
    }
    override fun error(tag: Tag, curThreadName: String, msg: String) {
        Gdx.app?.error(tag.name, msg) ?: println("[${tag.name}] [ERROR] $msg")
    }
    override fun isRelease() = true
    override fun getSystemInfo() = "Web browser (TeaVM)"
}

/** Nothing to configure: the browser tab is the window. */
class WebDisplay : PlatformDisplay

/**
 * Save-to-file / load-from-file. TODO: implement with a download link (save) and an <input type=file> (load)
 * via TeaVM JSO. Until then "copy save to clipboard" and the in-browser autosave (IndexedDB via gdx-teavm local
 * storage) are the way to keep games.
 */
class WebSaverLoader : PlatformSaverLoader {
    override fun requestSaveLocation(
        suggestedLocation: String, mimeType: String,
        onLocationChosen: (OutputStream, String) -> Unit, onError: (Exception) -> Unit
    ) = onError(UnsupportedOperationException("Saving to a file is not implemented in the web build yet"))

    override fun requestLoadLocation(
        onLocationChosen: (InputStream, String) -> Unit, onError: (Exception) -> Unit
    ) = onError(UnsupportedOperationException("Loading from a file is not implemented in the web build yet"))
}
