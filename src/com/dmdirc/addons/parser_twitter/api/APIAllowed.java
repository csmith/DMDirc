/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.dmdirc.addons.parser_twitter.api;

/**
 *
 * @author shane
 */
enum APIAllowed {
    /** It is not known if we are allowed or not. */
    UNKNOWN(false),
    /** We are not allowed. */
    FALSE(false),
    /** We are allowed. */
    TRUE(true);

    /** Boolean value of this APIAllowed */
    final boolean value;

    /**
     * Create an APIAllowed
     *
     * @param booleanValue boolean value for this if needed.
     */
    private APIAllowed(final boolean booleanValue) {
        value = booleanValue;
    }

    /**
     * Get the boolean value of this object.
     *
     * @return the boolean value of this object.
     */
    public boolean getBooleanValue() {
        return value;
    }
}
