/*-
 * #%L
 * mastodon-deep-lineage
 * %%
 * Copyright (C) 2022 - 2024 Stefan Hahmann
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package org.mastodon.mamut.clustering.ui;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.Timer;

/**
 * Some shortcut methods to show notifications to the user.
 */
public class Notification
{

	private static final int DEFAULT_TIME = 2_000;

	private Notification()
	{
		// prevent instantiation
	}

	/**
	* Displays a success notification to the user.
	* <br>
	* This method creates a success notification with a green checkmark and the provided message, and displays it to the user.
	* The notification is displayed in a non-modal dialog, which means that the user can continue interacting with the application while the notification is displayed.
	* The notification is automatically dismissed after a default time period (2 seconds).
	*
	* @param title The title of the notification dialog.
	* @param message The message to display in the notification.
	*/
	public static void showSuccess( final String title, final String message )
	{
		String decoratedMessage = "<font size=+4 color=green>&#10003 </font>" + getColoredString( message, "green" );
		JOptionPane pane = new JOptionPane( getHtmlMessage( decoratedMessage ), JOptionPane.INFORMATION_MESSAGE );
		JDialog dialog = pane.createDialog( null, title );
		dialog.setModal( false );
		dialog.setVisible( true );
		new Timer( DEFAULT_TIME, ignore -> dialog.dispose() ).start();
	}

	/**
	 * Displays a warning notification to the user. The message is displayed in a non-modal dialog. It is show in orange color with a warning icon.
	 * @param title The title of the notification dialog.
	 * @param message The message to display in the notification.
	 */
	public static void showWarning( final String title, final String message )
	{
		JOptionPane.showMessageDialog( null, getHtmlMessage( getColoredString( message, "#e5521a" ) ), title, JOptionPane.WARNING_MESSAGE );
	}

	/**
	 * Displays an error notification to the user. The message is displayed in a non-modal dialog. It is show in red color with an error icon.
	 * @param title The title of the notification dialog.
	 * @param message The message to display in the notification.
	 */
	public static void showError( final String title, final String message )
	{
		JOptionPane.showMessageDialog( null, getHtmlMessage( getColoredString( message, "red" ) ), title, JOptionPane.ERROR_MESSAGE );
	}

	private static String getColoredString( String message, String color )
	{
		return "<font color=" + color + ">" + message + "</font>";
	}

	private static String getHtmlMessage( String message )
	{
		return "<html><body>" + message + "</body></html>";
	}
}
