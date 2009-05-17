/*
 * Copyright (c) 2006-2009 Chris Smith, Shane Mc Cormack, Gregory Holmes
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.dmdirc.addons.ui_swing;

import com.dmdirc.plugins.PluginManager;
import com.dmdirc.addons.ui_swing.components.MenuBar;
import com.dmdirc.plugins.NoSuchProviderException;

/**
 * Integrate DMDirc with OS X better.
 * This class interfaces with ui_apple if available.
 */
public final class Apple {
	/** The singleton instance of Apple. */
	private static Apple me;
	
	/**
	 * Get the "Apple" instance.
	 *
	 * @return Apple instance.
	 */
	public static Apple getApple() {
		if (me == null) {
			me = new Apple();
		}
		return me;
	}
	
	/**
	 * Create the Apple class.
	 */
	public Apple() {
	}

	/**
	 * Get the "Application" object
	 *
	 * @return Object that on OSX will be an "Application"
	 */
	public static Object getApplication() {
		try {
			return PluginManager.getPluginManager().getExportedService("apple_getApplication").execute();
		} catch (final NoSuchProviderException nspe) {
			return null;
		}
	}
	
	/**
	 * Get the "NSApplication" object
	 *
	 * @return Object that on OSX will be an "NSApplication"
	 */
	public static Object getNSApplication() {
		try {
			return PluginManager.getPluginManager().getExportedService("apple_getNSApplication").execute();
		} catch (final NoSuchProviderException nspe) {
			return null;
		}
	}
	
	/**
	 * Are we on OS X?
	 *
	 * @return true if we are running on OS X
	 */
	public static boolean isApple() {
		try {
			final Object obj = PluginManager.getPluginManager().getExportedService("apple_isApple").execute();
			if (obj instanceof Boolean) {
				return (Boolean)obj;
			} else {
				return false;
			}
		} catch (final NoSuchProviderException nspe) {
			return false;
		}
	}
	
	/**
	 * Are we using the OS X look and feel?
	 *
	 * @return true if we are using the OS X look and feel
	 */
	public static boolean isAppleUI() {
		try {
			final Object obj = PluginManager.getPluginManager().getExportedService("apple_isAppleUI").execute();
			if (obj instanceof Boolean) {
				return (Boolean)obj;
			} else {
				return false;
			}
		} catch (final NoSuchProviderException nspe) {
			return false;
		}
	}

	/**
	 * Set some OS X only UI settings.
	 */
	public void setUISettings() {
		try {
			PluginManager.getPluginManager().getExportedService("apple_setUISettings").execute();
		} catch (final NoSuchProviderException nspe) { }
	}
	
	/**
	 * Request user attention (Bounce the dock).
	 *
	 * @param isCritical If this is false, the dock icon only bounces once,
	 *                   otherwise it will bounce until clicked on.
	 */
	public void requestUserAttention(final boolean isCritical) {
		try {
			PluginManager.getPluginManager().getExportedService("apple_requestUserAttention").execute(isCritical);
		} catch (final NoSuchProviderException nspe) { }
	}
	
	/**
	 * Set this up as a listener for the Apple Events
	 *
	 * @return True if the listener was added, else false.
	 */
	public boolean setListener() {
		try {
			final Object obj = PluginManager.getPluginManager().getExportedService("apple_setListener").execute();
			if (obj instanceof Boolean) {
				return (Boolean)obj;
			} else {
				return false;
			}
		} catch (final NoSuchProviderException nspe) {
			return false;
		}
	}
	
	/**
	 * Set the MenuBar.
	 * This will unset all menu mnemonics aswell if on the OSX ui.
	 *
	 * @param menuBar MenuBar to use to send events to,
	 */
	public void setMenuBar(final MenuBar menuBar) {
		try {
			PluginManager.getPluginManager().getExportedService("apple_setMenuBar").execute(menuBar);
		} catch (final NoSuchProviderException nspe) { }
	}
}
