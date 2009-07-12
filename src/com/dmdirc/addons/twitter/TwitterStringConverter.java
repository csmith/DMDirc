/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.dmdirc.addons.twitter;

import com.dmdirc.parser.interfaces.StringConverter;

/**
 * StringConverter that just uses the standard string conversion.
 *
 * @author shane
 */
class TwitterStringConverter implements StringConverter {

    /** {@inheritDoc} */
    @Override
    public String toLowerCase(final String input) {
        return input.toLowerCase();
    }

    /** {@inheritDoc} */
    @Override
    public String toUpperCase(final String input) {
        return input.toUpperCase();
    }

    /** {@inheritDoc} */
    @Override
    public boolean equalsIgnoreCase(final String first, final String second) {
        return first.equalsIgnoreCase(second);
    }
    
}
