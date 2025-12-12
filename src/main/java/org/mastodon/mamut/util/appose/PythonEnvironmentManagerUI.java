/*-
 * #%L
 * mastodon-deep-lineage
 * %%
 * Copyright (C) 2022 - 2025 Stefan Hahmann
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
package org.mastodon.mamut.util.appose;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.TitledBorder;

import net.miginfocom.swing.MigLayout;

import org.apposed.appose.util.Environments;
import org.mastodon.app.MastodonIcons;
import org.mastodon.mamut.detection.cellpose.Cellpose3;
import org.mastodon.mamut.detection.cellpose.Cellpose4;
import org.mastodon.mamut.detection.stardist.StarDist;
import org.mastodon.mamut.linking.trackastra.TrackastraUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PythonEnvironmentManagerUI extends JFrame
{
	private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	public static final Color GREEN = new Color( 0, 128, 0 );

	public static final Color RED = new Color( 180, 0, 0 );

	public static final String REFRESH_ALL = "Refresh All";

	private List< EnvironmentPanel > environmentPanels;

	private final JButton refreshButton;

	private final JButton closeButton;

	public PythonEnvironmentManagerUI( JFrame owner )
	{
		super( "Python Environment Manager" );
		setIconImage( MastodonIcons.MASTODON_ICON_MEDIUM.getImage() );
		refreshButton = new JButton( REFRESH_ALL );
		closeButton = new JButton( "Close" );

		initEnvironmentPanels();
		initLayout();
		initBehaviour();

		pack();
		setLocationRelativeTo( owner );
		setDefaultCloseOperation( DISPOSE_ON_CLOSE );
	}

	private void initEnvironmentPanels()
	{
		environmentPanels = new ArrayList<>();
		environmentPanels.add( new EnvironmentPanel( Cellpose3.ENV_NAME, Cellpose3.ENV_FILE_CONTENT ) );
		environmentPanels.add( new EnvironmentPanel( Cellpose4.ENV_NAME, Cellpose4.ENV_FILE_CONTENT ) );
		environmentPanels.add( new EnvironmentPanel( StarDist.ENV_NAME, StarDist.ENV_FILE_CONTENT ) );
		environmentPanels.add( new EnvironmentPanel( TrackastraUtils.ENV_NAME, TrackastraUtils.ENV_FILE_CONTENT ) );
	}

	private void initLayout()
	{
		JPanel mainPanel = new JPanel( new MigLayout( "fill, insets 10", "[grow]", "[]10[]10[]10[]10[grow][]" ) );
		JLabel titleLabel = new JLabel( "Python Environment Status" );
		titleLabel.setFont( titleLabel.getFont().deriveFont( Font.BOLD, 16f ) );
		mainPanel.add( titleLabel, "wrap, gapbottom 15" );

		environmentPanels.forEach( panel -> mainPanel.add( panel, "grow, wrap" ) );

		JPanel buttonPanel = new JPanel( new MigLayout( "fill", "[grow][]", "[]" ) );
		buttonPanel.add( refreshButton, "" );
		buttonPanel.add( closeButton, "" );
		mainPanel.add( buttonPanel, "grow" );

		setContentPane( mainPanel );
	}

	private void initBehaviour()
	{
		refreshButton.addActionListener( e -> refreshAllEnvironments() );
		closeButton.addActionListener( e -> dispose() );
	}

	private void refreshAllEnvironments()
	{
		refreshButton.setEnabled( false );
		refreshButton.setText( "Refreshing..." );

		SwingWorker< Void, Void > worker = new SwingWorker< Void, Void >()
		{
			@Override
			protected Void doInBackground()
			{
				environmentPanels.forEach( EnvironmentPanel::updateAsync );
				return null;
			}

			@Override
			protected void done()
			{
				refreshButton.setEnabled( true );
				refreshButton.setText( REFRESH_ALL );
			}
		};
		worker.execute();
	}

	private class EnvironmentPanel extends JPanel
	{
		private final String envName;

		private final String envContent;

		private final JLabel statusIcon;

		private final JLabel statusLabel;

		private final JButton installButton;

		private final JButton deleteButton;

		private final JLabel sizeValueLabel;

		private EnvironmentPanel( String envName, String envContent )
		{
			super( new MigLayout( "fill, insets 8", "[][80!][grow][]", "[]5[]5[]" ) );
			this.envName = envName;
			this.envContent = envContent;

			statusIcon = new JLabel();
			statusLabel = new JLabel();
			installButton = new JButton();
			deleteButton = new JButton( "Delete" );
			sizeValueLabel = new JLabel();

			initLayout();
			initBehaviour();
			updateAsync();
		}

		private void initBehaviour()
		{
			installButton.addActionListener( e -> installEnvironmentUI() );
			deleteButton.addActionListener( e -> deleteEnvironmentUI() );
		}

		private void initLayout()
		{
			TitledBorder border = BorderFactory.createTitledBorder( envName );
			border.setTitleFont( border.getTitleFont().deriveFont( Font.BOLD ) );
			setBorder( border );

			statusIcon.setPreferredSize( new Dimension( 24, 24 ) );
			add( statusIcon, "" );

			statusLabel.setFont( statusLabel.getFont().deriveFont( Font.BOLD ) );
			add( statusLabel, "gap 5" );

			add( new JLabel(), "grow" );
			add( installButton, "w 120!, gap 10" );
			add( deleteButton, "w 80!, wrap" );

			JLabel pathLabel = new JLabel( "Path:" );
			pathLabel.setFont( pathLabel.getFont().deriveFont( 10f ) );
			add( pathLabel, "span 2" );

			String installPath = new File( Environments.apposeEnvsDir(), envName ).getAbsoluteFile().toString();
			JTextField pathField = new JTextField( installPath );
			pathField.setEditable( false );
			pathField.setFont( pathField.getFont().deriveFont( 10f ) );
			pathField.setBackground( getBackground() );
			add( pathField, "grow, span 2, wrap" );

			JLabel sizeLabel = new JLabel( "Size:" );
			sizeLabel.setFont( sizeLabel.getFont().deriveFont( 10f ) );
			add( sizeLabel, "span 2" );

			sizeValueLabel.setFont( sizeValueLabel.getFont().deriveFont( 10f ) );
			add( sizeValueLabel, "grow, span 2" );
		}

		private void updateAsync()
		{
			statusLabel.setText( "Checking..." );
			statusLabel.setForeground( Color.GRAY );
			installButton.setEnabled( false );
			deleteButton.setEnabled( false );
			sizeValueLabel.setText( "..." );

			SwingWorker< UpdateResult, Void > worker = new SwingWorker< UpdateResult, Void >()
			{
				@Override
				protected UpdateResult doInBackground()
				{
					boolean isInstalled = ApposeUtils.checkEnvironmentInstalled( envName );
					String size = isInstalled ? ApposeUtils.calculateEnvironmentSize( envName ) : "N/A";
					return new UpdateResult( isInstalled, size );
				}

				@Override
				protected void done()
				{
					try
					{
						UpdateResult result = get();
						updateUIStatus( result.isInstalled, result.size );
					}
					catch ( InterruptedException e )
					{
						Thread.currentThread().interrupt();
						logger.warn( "Update interrupted for environment {}", envName, e );
					}
					catch ( ExecutionException e )
					{
						logger.error( "Error updating environment {}", envName, e.getCause() );
					}
				}
			};
			worker.execute();
		}

		private void updateUIStatus( boolean isInstalled, String size )
		{
			updateStatusIcon( isInstalled );
			statusLabel.setText( isInstalled ? "Installed" : "Not Installed" );
			statusLabel.setForeground( isInstalled ? GREEN : RED );
			installButton.setText( isInstalled ? "Update" : "Install" );
			installButton.setEnabled( true );
			deleteButton.setEnabled( isInstalled );
			sizeValueLabel.setText( size );
		}

		@SuppressWarnings( "MagicConstant" )
		private void runEnvironmentTask(
				String actionName,
				String confirmMessage,
				String progressMessage,
				String successMessage,
				int confirmMessageType,
				Runnable backgroundTask
		)
		{
			int result = JOptionPane.showConfirmDialog(
					PythonEnvironmentManagerUI.this,
					confirmMessage,
					"Confirm " + actionName,
					JOptionPane.YES_NO_OPTION,
					confirmMessageType
			);

			if ( result == JOptionPane.YES_OPTION )
			{
				JProgressBar progressBar = new JProgressBar();
				progressBar.setIndeterminate( true );
				JOptionPane progressPane = new JOptionPane(
						new Object[] { progressMessage, progressBar },
						JOptionPane.INFORMATION_MESSAGE,
						JOptionPane.DEFAULT_OPTION,
						null,
						new Object[] {},
						null
				);
				JDialog progressDialog = progressPane.createDialog( PythonEnvironmentManagerUI.this, actionName + "..." );

				SwingWorker< Void, Void > worker = new SwingWorker< Void, Void >()
				{
					@Override
					protected Void doInBackground() throws Exception
					{
						backgroundTask.run();
						Thread.sleep( 2000 );
						return null;
					}

					@Override
					protected void done()
					{
						progressDialog.dispose();
						updateAsync();
						JOptionPane.showMessageDialog(
								PythonEnvironmentManagerUI.this,
								successMessage,
								actionName + " Complete",
								JOptionPane.INFORMATION_MESSAGE
						);
					}
				};

				worker.execute();
				progressDialog.setVisible( true );
			}
		}

		private void installEnvironmentUI()
		{
			String actionName = ApposeUtils.checkEnvironmentInstalled( envName ) ? "Update" : "Installation";
			String message = ApposeUtils.checkEnvironmentInstalled( envName )
					? "Update Python environment '" + envName + "' to the latest version?"
					: "Install Python environment '" + envName + "'?";
			runEnvironmentTask(
					actionName,
					message + "\n\nThis requires internet connection (download size >1GB).\n\nThis may take several minutes.",
					"Installing python environment " + envName + "...",
					"Python environment '" + envName + "' installed successfully!",
					JOptionPane.QUESTION_MESSAGE,
					() -> {
						try
						{
							ApposeUtils.installEnvironment( envContent );
						}
						catch ( IOException e )
						{
							logger.error( "Installation failed for {}", envName, e );
							SwingUtilities.invokeLater( () -> JOptionPane.showMessageDialog(
									PythonEnvironmentManagerUI.this,
									"Installation failed: " + e.getMessage(),
									"Error",
									JOptionPane.ERROR_MESSAGE
							) );
						}
					}
			);
		}

		private void deleteEnvironmentUI()
		{
			runEnvironmentTask(
					"Deletion",
					"Delete Python environment '" + envName + "'?",
					"Deleting environment " + envName + "...",
					"Environment '" + envName + "' deleted successfully!",
					JOptionPane.WARNING_MESSAGE,
					() -> ApposeUtils.deleteEnvironment( envName, PythonEnvironmentManagerUI.this )
			);
		}

		private void updateStatusIcon( boolean isInstalled )
		{
			statusIcon.setIcon( isInstalled ? createTickIcon() : createCrossIcon() );
		}

		private Icon createTickIcon()
		{
			return createCircleIcon( GREEN, g2 -> {
				g2.drawLine( 5, 10, 8, 14 );
				g2.drawLine( 8, 14, 15, 6 );
			} );
		}

		private Icon createCrossIcon()
		{
			return createCircleIcon( RED, g2 -> {
				g2.drawLine( 6, 6, 14, 14 );
				g2.drawLine( 14, 6, 6, 14 );
			} );
		}

		private Icon createCircleIcon( Color color, Consumer< Graphics2D > draw )
		{
			return new Icon()
			{
				@Override
				public void paintIcon( Component c, Graphics g, int x, int y )
				{
					Graphics2D g2 = ( Graphics2D ) g.create();
					g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
					g2.setColor( color );
					g2.fillOval( x, y, 20, 20 );
					g2.setColor( Color.WHITE );
					g2.setStroke( new BasicStroke( 2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND ) );
					g2.translate( x, y );
					draw.accept( g2 );
					g2.dispose();
				}

				@Override
				public int getIconWidth()
				{
					return 20;
				}

				@Override
				public int getIconHeight()
				{
					return 20;
				}
			};
		}

		private class UpdateResult
		{
			final boolean isInstalled;

			final String size;

			UpdateResult( boolean isInstalled, String size )
			{
				this.isInstalled = isInstalled;
				this.size = size;
			}
		}
	}
}
