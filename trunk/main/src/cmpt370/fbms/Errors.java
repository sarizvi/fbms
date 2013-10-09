package cmpt370.fbms;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

public class Errors
{
	/**
	 * Used for alerting the user to fatal errors. Should *only* be used for errors that cannot be
	 * recovered from and require the program to terminate. Will log the error (along with the stack
	 * trace) as well as prompt the user.
	 * 
	 * @param message
	 *            The error message to print
	 * @param error
	 *            An exception object
	 */
	public static void fatalError(String message, Throwable error)
	{
		Control.logger.fatal(message, error);

		// Convert stack trace into a string
		StackTraceElement stackTrace[] = error.getStackTrace();
		String stackTraceContent = "See log file.\n\nStack trace:\n";
		for(StackTraceElement line : stackTrace)
		{
			stackTraceContent += line + "\n";
		}

		JOptionPane.showMessageDialog(null, message + "\n\n" + stackTraceContent, "Fatal error",
				JOptionPane.ERROR_MESSAGE);
		System.exit(1);
	}

	/**
	 * Works the same way as fatalError(String, Throwable), but only requires a message.
	 * 
	 * @param message
	 *            The error message to print
	 */
	public static void fatalError(String message)
	{
		Control.logger.fatal(message);

		JOptionPane.showMessageDialog(null, message, "Fatal error", JOptionPane.ERROR_MESSAGE);
		System.exit(1);
	}

	/**
	 * Displays a non-obtrusive notification at the bottom corner of the screen. For use with simple
	 * errors.
	 * 
	 * @param message
	 *            The message to display
	 * @param header
	 *            The header (title) of the notification
	 */
	public static void nonfatalError(String message, String header)
	{
		// The frame
		final JDialog frame = new JDialog();
		frame.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		frame.setSize(300, 100);
		frame.setUndecorated(true);

		frame.setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();

		// The heading
		JLabel headingLabel = new JLabel("FBMS: " + header);
		headingLabel.setFont(new Font("Sans Serif", Font.BOLD, 18));
		headingLabel.setOpaque(false);

		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.weightx = 1;
		constraints.weighty = -1;
		constraints.insets = new Insets(5, 5, 5, 5);
		constraints.fill = GridBagConstraints.BOTH;
		frame.add(headingLabel, constraints);

		// The close button
		// Make the button close the window on click
		JButton closeButton = new JButton(new AbstractAction("X")
		{
			// So Eclipse will shut the hell up
			private static final long serialVersionUID = -3379092798847301811L;

			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				frame.dispose();
			}
		});

		closeButton.setMargin(new Insets(1, 4, 1, 4));
		closeButton.setFocusable(false);

		constraints.gridx = 1;
		constraints.weightx = 0;
		constraints.weighty = 0;
		constraints.fill = GridBagConstraints.NONE;
		constraints.anchor = GridBagConstraints.NORTH;
		frame.add(closeButton, constraints);

		// The message
		JLabel messageLabel = new JLabel("<html>" + message);
		messageLabel.setFont(new Font("Sans Serif", Font.PLAIN, 12));
		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.weightx = 1;
		constraints.weighty = 1;
		constraints.insets = new Insets(5, 5, 5, 5);
		constraints.fill = GridBagConstraints.HORIZONTAL;
		frame.add(messageLabel, constraints);

		// Get dimension of screen and tool bar(s)
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Insets toolBarOffset = Toolkit.getDefaultToolkit().getScreenInsets(
				frame.getGraphicsConfiguration());

		// Offset from the left is the width of the screen minus the size of the frame minus and any
		// toolbars (so it works with, say, a taskbar that is on the right side of the screen)
		// Offset from the top is similar
		frame.setLocation(screenSize.width - frame.getWidth() - toolBarOffset.right,
				screenSize.height - toolBarOffset.bottom - frame.getHeight());

		frame.setAlwaysOnTop(true);
		frame.setVisible(true);

		// Use a thread to make the frame disappear after a period of time
		new Thread()
		{
			@Override
			public void run()
			{
				try
				{
					Thread.sleep(7500);
					frame.dispose();
				}
				catch(InterruptedException e)
				{
					Errors.fatalError("Thread was interupted", e);
				}
			};
		}.start();
	}
}
