/*
 * Copyright (c) 2006-2008 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.ui.core;

/**
 * Represents a colour as a combination of red, green and blue values.
 *
 * @author chris
 */
public class Colour {

    /** Pure white. */
    public static Colour WHITE = new Colour(255, 255, 255);
    /** Light gray. */
    public static Colour LIGHT_GRAY = new Colour(192, 192, 192);
    /** Gray. */
    public static Colour GRAY = new Colour(128, 128, 128);
    /** Dark gray. */
    public static Colour DARK_GRAY = new Colour(64, 64, 64);
    /** Pure black. */
    public static Colour BLACK = new Colour(0, 0, 0);
    /** Pure red. */
    public static Colour RED = new Colour(255, 0, 0);
    /** Pure blue. */
    public static Colour BLUE = new Colour(0, 0, 255);
    /** Yellow. */
    public static Colour YELLOW = new Colour(255, 255, 0);

    /** The value of the red channel of this colour. */
    private final int red;
    /** The value of the green channel of this colour. */
    private final int green;
    /** The value of the blue channel of this colour. */
    private final int blue;

    /**
     * Creates a new colour with the specified values of red, green and blue
     * (in the range 0-255).
     *
     * @param red The value of the red channel
     * @param green The value of the green channel
     * @param blue The value of the blue channel
     */
    public Colour(final int red, final int green, final int blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    /**
     * Retrieves the value of the blue channel of this colour.
     * 
     * @return This colour's blue channel
     */
    public int getBlue() {
        return blue;
    }

    /**
     * Retrieves the value of the green channel of this colour.
     *
     * @return This colour's green channel
     */
    public int getGreen() {
        return green;
    }

    /**
     * Retrieves the value of the red channel of this colour.
     *
     * @return This colour's red channel
     */
    public int getRed() {
        return red;
    }

    /**
     * Determines if this colour is equal to the specified object. Colours are
     * equal if and only if their red, green and blue values are equal.
     *
     * @param obj The object to compare this one to
     * @return True if the colours are equal, false otherwise.
     */
    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof Colour) {
            final Colour c = (Colour) obj;
            return c.getRed() == red && c.getGreen() == green && c.getBlue() == blue;
        } else {
            return false;
        }
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 53 * hash + this.red;
        hash = 53 * hash + this.green;
        hash = 53 * hash + this.blue;
        return hash;
    }

}
