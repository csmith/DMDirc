/*
 * Copyright (c) 2006-2017 DMDirc Developers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.dmdirc.ui.messages;

import com.dmdirc.events.DisplayProperty;
import com.dmdirc.events.DisplayPropertyMap;
import java.util.Arrays;
import java.util.Optional;

/**
 * Represents a line of text in IRC.
 */
public class IRCLine implements Line {

    private final String timestamp;
    private final String text;
    private final Styliser styliser;
    private final StyledMessageUtils styleUtils = new StyledMessageUtils(); // TODO: Inject
    private final DisplayPropertyMap displayProperties;
    private int fontSize;
    private String fontName;

    /**
     * Creates a new line with a specified height.
     *
     * @param styliser  The styliser to use to style this line
     * @param timestamp The textual timestamp to use for the line
     * @param text      The textual content of the line
     * @param displayProperties The properties to use when displaying the line.
     * @param fontSize  The height for this line
     * @param fontName  The name of the font to use for this line
     */
    public IRCLine(final Styliser styliser, final String timestamp, final String text,
            final DisplayPropertyMap displayProperties, final int fontSize, final String fontName) {
        this.styliser = styliser;
        this.timestamp = timestamp; // TODO: Make this a long and convert further down the line
        this.text = text;
        this.displayProperties = displayProperties;
        this.fontName = fontName;
        this.fontSize = fontSize;
    }

    /**
     * Returns the line parts of this line.
     *
     * @return Lines parts
     */
    private String[] getLineParts() {
        if (displayProperties.get(DisplayProperty.NO_TIMESTAMPS).orElse(false)) {
            return new String[] { text };
        } else {
            return new String[] { timestamp, text };
        }
    }

    @Override
    public int getLength() {
        return timestamp.length() + text.length();
    }

    @Override
    public int getFontSize() {
        return fontSize;
    }

    @Override
    public void setFontSize(final int fontSize) {
        this.fontSize = fontSize;
    }

    @Override
    public void setFontName(final String fontName) {
        this.fontName = fontName;
    }

    @Override
    public String getText() {
        return styleUtils.stripControlCodes(timestamp + text);
    }

    @Override
    public String getStyledText() {
        return timestamp + text;
    }

    @Override
    public <T> T getStyled(final StyledMessageMaker<T> maker) {
        maker.setDefaultFont(fontName, fontSize);
        styliser.addStyledString(maker, getLineParts());
        final T styledString = maker.getStyledMessage();
        fontSize = maker.getMaximumFontSize();
        maker.clear();
        return styledString;
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof Line && Arrays.equals(((IRCLine) obj).getLineParts(), getLineParts());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(getLineParts());
    }

    @Override
    public <T> Optional<T> getDisplayableProperty(final DisplayProperty<T> property) {
        return displayProperties.get(property);
    }

}
