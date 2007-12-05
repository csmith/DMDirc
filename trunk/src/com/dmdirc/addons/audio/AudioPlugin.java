/*
 * Copyright (c) 2006-2007 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.addons.audio;

import com.dmdirc.plugins.Plugin;
import com.dmdirc.commandparser.CommandManager;
/**
 * Adds Audio playing facility to client.
 *
 * @author Shane 'Dataforce' McCormack
 * @version $Id: AudioPlugin.java 969 2007-04-30 18:38:20Z ShaneMcC $
 */
public final class AudioPlugin extends Plugin {
	/** The AudioCommand we created */
	private AudioCommand command = null;

	/**
	 * Creates a new instance of the Audio Plugin.
	 */
	public AudioPlugin() { super(); }
	
	/**
	 * Called when the plugin is loaded.
	 */
	public void onLoad() {
		command = new AudioCommand();
	}
	
	/**
	 * Called when this plugin is Unloaded
	 */
	public void onUnload() {
		CommandManager.unregisterCommand(command);
	}
	
	/**
	 * Get SVN Version information.
	 *
	 * @return SVN Version String
	 */
	public static String getSvnInfo() { return "$Id: IRCParser.java 969 2007-04-30 18:38:20Z ShaneMcC $"; }	
}

