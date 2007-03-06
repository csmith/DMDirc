/*
 * Copyright (c) 2006-2007 Chris Smith, Shane Mc Cormack
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
 *
 * SVN: $Id$
 */

package uk.org.ownage.dmdirc.parser;

import java.util.Enumeration;

/**
 * Contains information about known users.
 * 
 * @author            Shane Mc Cormack
 * @author            Chris Smith
 * @version           $Id$
 * @see IRCParser
 */
public class ClientInfo {
	/** Known nickname of client. */
	private String sNickname = "";
	/** Known ident of client. */
	private String sIdent = "";	
	/** Known host of client. */
	private String sHost = "";
	/** Known user modes of client. */
	private int nModes = 0;
	/** Known away state for client. */
	private boolean bIsAway = false;
	/** Reference to the parser object that owns this channel, Used for modes. */
	private IRCParser myParser; // Reference to parser object that owns this channel. Used for Modes

	/**
	 * Get a nickname of a user from a hostmask.
	 * Hostmask must match (?:)nick(?!ident)(?@host)
	 *
	 * @return nickname of user
	 */
	public static String parseHost(String sWho) {
		// Get the nickname from the string.
		String sTemp[] = null;
		sTemp = sWho.split("@",2);
		sTemp = sTemp[0].split("!",2);
		sTemp = sTemp[0].split(":",2);
		if (sTemp.length != 1) { sWho = sTemp[1]; } else { sWho = sTemp[0]; }
		return sWho;
	}
	
	/**
	 * Get a nick ident and host of a user from a hostmask.
	 * Hostmask must match (?:)nick(?!ident)(?@host)
	 *
	 * @return Array containing details. (result[0] -> Nick | result[1] -> Ident | result[2] -> Host)
	 */
	public static String[] parseHostFull(String sWho) {
		String sTemp[] = null;
		String result[] = new String[3];
		sTemp = sWho.split(":",2);
		if (sTemp.length != 1) { sWho = sTemp[1]; } else { sWho = sTemp[0]; }

		sTemp = sWho.split("@",2);
		if (sTemp.length != 1) { result[2] = sTemp[1]; } else { result[2] = ""; }
		sTemp = sTemp[0].split("!",2);
		if (sTemp.length != 1) { result[1] = sTemp[1]; } else { result[1] = ""; }
		result[0] = sTemp[0];
		
		return result;
	}

	/**
	 * Create a new client object from a hostmask.
	 *
 	 * @param tParser Refernce to parser that owns this channelclient (used for modes)	 
	 * @param sHostmask Hostmask parsed by parseHost to get nickname
	 * @see ClientInfo#parseHost
	 */
	public ClientInfo (IRCParser tParser, String sHostmask) { 
		setUserBits(sHostmask,true);
		myParser = tParser;
	}
	/**
	 * Get a string representation of the user
	 *
	 * @param sHostmask takes a host (?:)nick(?!ident)(?@host) and sets nick/host/ident variables
	 * @param bUpdateNick if this is false, only host/ident will be updated.
	 */	
	public void setUserBits (String sHostmask, boolean bUpdateNick) {
		String sTemp[] = null;
		sTemp = sHostmask.split(":",2);
		if (sTemp.length != 1) { sHostmask = sTemp[1]; } else { sHostmask = sTemp[0]; }

		sTemp = sHostmask.split("@",2);
		if (sTemp.length != 1) { sHost = sTemp[1]; }
		sTemp = sTemp[0].split("!",2);
		if (sTemp.length != 1) { sIdent = sTemp[1]; }
		if (bUpdateNick) { sNickname = sTemp[0]; }
	}

	/**
	 * Get a string representation of the user
	 *
	 * @return String representation of the user.
	 */
	public String toString() { return sNickname+"!"+sIdent+"@"+sHost; }
	
	/**
	 * Get the nickname for this user
	 *
	 * @return Known nickname for user.
	 */
	public String getNickname() { return sNickname; }
	/**
	 * Get the ident for this user
	 *
	 * @return Known ident for user. (May be "")
	 */		
	public String getIdent() { return sIdent; }
	/**
	 * Get the hostname for this user
	 *
	 * @return Known host for user. (May be "")
	 */		
	public String getHost() { return sHost; }
	
	/**
	 * Set the away state of a user.
	 *
	 * @param bNewState Boolean representing state. true = away, false = here
	 */	
	public void setAwayState(boolean bNewState) { bIsAway = bNewState; }
	/**
	 * Get the away state of a user.
	 *
	 * @return Boolean representing state. true = away, false = here
	 */	
	public boolean getAwayState() { return bIsAway; }
	
	/**
	 * Set the user modes (as an integer).
	 *
	 * @param nNewMode new integer representing channel modes. (Boolean only)
	 */	
	public void setUserMode(int nNewMode) { nModes = nNewMode; }
	/**
	 * Get the user modes (as an integer).
	 *
	 * @return integer representing channel modes. (Boolean only)
	 */	
	public int getUserMode() { return nModes; }	
	
	/**
	 * Get the user modes (as a string representation).
	 *
	 * @return string representing modes. (boolean and non-list)
	 */	
	public String getUserModeStr() { 
		String sModes = "+", sTemp = "";
		Character cTemp;
		int nTemp = 0, nModes = this.getUserMode();
		
		for (Enumeration e = myParser.hUserModes.keys(); e.hasMoreElements();) {
			cTemp = (Character)e.nextElement();
			nTemp = myParser.hUserModes.get(cTemp);
			if ((nModes & nTemp) == nTemp) { sModes = sModes+cTemp; }
		}
		
		return sModes;
	}
	
	/**
	 * Check to see if a client is still known on any of the channels we are on.
	 *
	 * @return Boolean to see if client is still visable.
	 */	
	public boolean checkVisability() {
		boolean bCanSee = false;
		ChannelInfo iChannel;
		ChannelClientInfo iChannelClient;
		
		for (Enumeration e = myParser.hChannelList.keys(); e.hasMoreElements();) {
			iChannel = myParser.hChannelList.get(e.nextElement());
			iChannelClient = iChannel.getUser(this);
			if (iChannelClient != null) {	bCanSee = true; break; }
		}

		return bCanSee;
	}
	
	/**
	 * Get SVN Version information
	 *
	 * @return SVN Version String
	 */
	public static String getSvnInfo () { return "$Id$"; }	
}
