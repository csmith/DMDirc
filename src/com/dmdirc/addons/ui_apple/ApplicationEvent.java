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

package com.dmdirc.addons.ui_apple;

/**
 * Dummy interface for ApplicationEvent from the Apple UI on non-Apple platforms.
 * http://developer.apple.com/documentation/Java/Reference/1.5.0/appledoc/api/com/apple/eawt/ApplicationEvent.html
 */
public interface ApplicationEvent {
	/**
	 * Provides the filename associated with a particular AppleEvent.
	 *
	 * @return The filename associated with a particular AppleEvent.
	 */
	String getFilename();

	/**
	 * Whether or not this event is handled.
	 *
	 * @return True if the event is handled, false otherwise
	 */
	boolean isHandled();

	/**
	 * Sets the handled state of this event.
	 *
	 * @param handled The new 'handled' state for this event.
	 */
	void setHandled(boolean handled);

	/**
	 * Retrieves the source of this event.
	 *
	 * @return This event's source
	 */
	Object getSource();
	
	/**
	 * Get a string representation of this object.
	 *
	 * @return A string representation of this object.
	 */
	@Override
	String toString();
}