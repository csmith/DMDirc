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

package uk.org.ownage.dmdirc.identities;

import java.util.Properties;

/**
 * An identity is a group of settings that are applied to a connection, server,
 * network or channel. Identities may be automatically applied in certain
 * cases, or the user may manually apply them.
 * @author chris
 */
public class Identity implements ConfigSource {
    
    /** The name of this identity. */
    private String name;
    
    /** The autoapplies list for this identity. */
    private String[] autoapplies;
    
    /** The configuration details for this identity. */
    private Properties properties;
    
    /**
     * Creates a new instance of Identity.
     * @param file The file to load this identity from.
     */
    public Identity(final String file) {
    }
    
    /**
     * Determines whether this identity has a setting for the specified
     * option in the specified domain.
     * @param domain The domain of the option
     * @param option The name of the option
     * @return True iff this source has the option, false otherwise
     */
    public boolean hasOption(final String domain, final String option) {
        return false;
    }
    
    /**
     * Retrieves the specified option from this identity.
     * @param domain The domain of the option
     * @param option The name of the option
     * @return The value of the specified option
     */
    public String getOption(final String domain, final String option) {
        return "";
    }
    
    /**
     * Sets the specified option in this identity to the specified value.
     * @param domain The domain of the option
     * @param option The name of the option
     * @param value The new value for the option
     */
    public void setOption(final String domain, final String option, 
            final String value) {
        
    }
    
}
