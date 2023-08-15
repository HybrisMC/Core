/*
 * HybrisMC, a Minecraft toolchain and client
 * Copyright (C) 2023, The HybrisMC Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package dev.hybrismc.core

import java.util.ServiceLoader
import kotlin.jvm.optionals.getOrNull

interface VersionAdapter {
    fun preLaunch()
    // TODO: other stuff
}

val currentVersionAdapter by lazy { ServiceLoader.load(VersionAdapter::class.java).findFirst().getOrNull() }