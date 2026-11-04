package com.unciv.web

import com.github.xpenatan.gdx.teavm.backends.web.WebApplication
import com.github.xpenatan.gdx.teavm.backends.web.WebApplicationConfiguration
import com.unciv.UncivGame
import com.unciv.logic.files.UncivFiles
import com.unciv.ui.components.fonts.Fonts
import com.unciv.utils.Display
import com.unciv.utils.Log

/** Browser entry point; the TeaVM main class (see web/build.gradle.kts). Mirrors DesktopLauncher.main. */
object WebLauncher {
    @JvmStatic
    fun main(args: Array<String>) {
        Log.backend = WebLogBackend()
        Display.platform = WebDisplay()
        Fonts.fontImplementation = WebFont()
        UncivFiles.saverLoader = WebSaverLoader()
        UncivFiles.preferExternalStorage = false

        val config = WebApplicationConfiguration().apply {
            width = 0   // 0 = fill the browser window
            height = 0
        }
        WebApplication(WebGame(), config)
    }
}

class WebGame : UncivGame() {
    override fun getGcCount() = 0
}
