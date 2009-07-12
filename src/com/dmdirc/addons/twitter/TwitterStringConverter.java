/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.dmdirc.addons.twitter;

import com.dmdirc.parser.interfaces.StringConverter;

/**
 *
 * @author shane
 */
class TwitterStringConverter implements StringConverter {

    @Override
    public String toLowerCase(String input) {
        return input.toLowerCase();
    }

    @Override
    public String toUpperCase(String input) {
        return input.toUpperCase();
    }

    @Override
    public boolean equalsIgnoreCase(String first, String second) {
        return first.equalsIgnoreCase(second);
    }
    
}
