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

package com.dmdirc.addons.ui_swing.textpane2;

import java.util.ArrayList;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.StyleContext;

/** Stylised document. */
public class IRCDocument extends DefaultStyledDocument {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 2;
    /** EOL tag that we re-use when creating ElementSpecs. */
    private static final char[] EOL_ARRAY = {'\n'};
    /** Batched ElementSpecs. */
    private ArrayList<ElementSpec> batch = null;

    /** Instantiates a new IRCDocument. */
    public IRCDocument() {
        this(new StyleContext());
    }

    /**
     * Instantiates a new IRCDocument using the specified StyleContext.
     *
     * @param styles StyleContext to use
     */
    public IRCDocument(final StyleContext styles) {
        super(new DocumentContent(), styles);
        batch = new ArrayList<ElementSpec>();
    }

    public IRCDocument(Content c, StyleContext styles) {
        super(c, styles);
        batch = new ArrayList<ElementSpec>();
    }

    /**
     * Adds a String (assumed to not contain linefeeds) for
     * later batch insertion.
     */
    public void appendBatchString(String str,
            AttributeSet a) {
        // We could synchronize this if multiple threads
        // would be in here. Since we're trying to boost speed,
        // we'll leave it off for now.

        // Make a copy of the attributes, since we will hang onto
        // them indefinitely and the caller might change them
        // before they are processed.
        a = a.copyAttributes();
        char[] chars = str.toCharArray();
        batch.add(new ElementSpec(
                a, ElementSpec.ContentType, chars, 0, str.length()));
    }

    /**
     * Adds a linefeed for later batch processing
     */
    public void appendBatchLineFeed(AttributeSet a) {
        // See sync notes above. In the interest of speed, this
        // isn't synchronized.

        // Add a spec with the linefeed characters
        batch.add(new ElementSpec(
                a, ElementSpec.ContentType, EOL_ARRAY, 0, 1));

        // Then add attributes for element start/end tags. Ideally
        // we'd get the attributes for the current position, but we
        // don't know what those are yet if we have unprocessed
        // batch inserts. Alternatives would be to get the last
        // paragraph element (instead of the first), or to process
        // any batch changes when a linefeed is inserted.
        Element paragraph = getParagraphElement(0);
        AttributeSet pattr = paragraph.getAttributes();
        batch.add(new ElementSpec(null, ElementSpec.EndTagType));
        batch.add(new ElementSpec(pattr, ElementSpec.StartTagType));
    }

    public void processBatchUpdates(int offs) throws
            BadLocationException {
        // As with insertBatchString, this could be synchronized if
        // there was a chance multiple threads would be in here.
        ElementSpec[] inserts = new ElementSpec[batch.size()];
        batch.toArray(inserts);

        // Process all of the inserts in bulk
        super.insert(offs, inserts);
    }
}
