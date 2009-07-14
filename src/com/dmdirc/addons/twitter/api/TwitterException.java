/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.dmdirc.addons.twitter.api;

/**
 * Exception in the twitter API!
 *
 * @author shane
 */
class TwitterException extends RuntimeException {
    /**
     * Create a new Twitter Exception!
     *
     * @param reason Reason for this exception.
     */
    public TwitterException(final String reason) {
        super(reason);
    }

    /**
     * Create a new Twitter Exception with a cause.
     *
     * @param reason Reason for this exception.
     * @param cause Cause of this exception
     */
    public TwitterException(final String reason, final Throwable cause) {
        super(reason, cause);
    }

}
