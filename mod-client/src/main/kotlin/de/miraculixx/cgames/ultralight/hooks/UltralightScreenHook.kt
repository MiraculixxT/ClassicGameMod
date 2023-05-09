/*
 * This file is part of LiquidBounce (https://github.com/CCBlueX/LiquidBounce)
 *
 * Copyright (c) 2016 - 2023 CCBlueX
 *
 * LiquidBounce is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LiquidBounce is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LiquidBounce. If not, see <https://www.gnu.org/licenses/>.
 */
package de.miraculixx.cgames.ultralight.hooks

import de.miraculixx.cgames.ultralight.ScreenViewOverlay
import de.miraculixx.cgames.ultralight.UltralightEngine

object UltralightScreenHook  {

//    /**
//     * Handle opening new screens
//     */
//    val screenHandler = handler<ScreenEvent> { event ->
//        UltralightEngine.cursorAdapter.unfocus()
//
//        val activeView = UltralightEngine.inputAwareOverlay
//        if (activeView is ScreenViewOverlay) {
//            if (activeView.context.events._fireViewClose()) {
//                UltralightEngine.removeView(activeView)
//            }
//        }
//
//        val screen = event.screen ?: if (mc.world != null) return@handler else TitleScreen()
//        val name = UltralightJsPages.get(screen)?.name ?: return@handler
//        val page = ThemeManager.page(name) ?: return@handler
//
//        val emptyScreen = EmptyScreen()
//        UltralightEngine.newScreenView(emptyScreen, adaptedScreen = screen, parentScreen = mc.currentScreen).apply {
//            loadPage(page)
//        }
//
//        mc.setScreen(emptyScreen)
//        event.cancelEvent()
//    }

}
