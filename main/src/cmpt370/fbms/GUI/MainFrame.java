/*
	FBMS: File Backup and Management System
	Copyright (C) 2013 Group 06

	This program is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package cmpt370.fbms.GUI;

import java.awt.BorderLayout;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;

import cmpt370.fbms.Control;
import cmpt370.fbms.Data;

public class MainFrame extends JFrame
{
	private JPanel contentPane;

	/**
	 * Create the frame.
	 */
	public MainFrame()
	{
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setVisible(true);

		// Set size and position
		setSize(900, 400);
		setLocationRelativeTo(null);

		// Create main panel
		contentPane = new JPanel();
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		// Set the icon
		setIconImage(new ImageIcon("res/icon.png").getImage());

		// Create menu
		MainMenu topMenu = new MainMenu();
		setJMenuBar(topMenu);

		// Create toolbar
		MainToolBar topTool = new MainToolBar();
		add(topTool, BorderLayout.NORTH);

		// Create table columns
		Vector<String> columns = new Vector<>();
		columns.add("Name");
		columns.add("Size");
		columns.add("Created date");
		columns.add("Accessed date");
		columns.add("Modified date");
		columns.add("Revisions");
		columns.add("Revision sizes");

		// Create table
		JTable table = new JTable(Data.getTableData(Control.backupDirectory), columns);

		// Create scrollpane for the table
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		table.setFillsViewportHeight(true);
		add(scrollPane, BorderLayout.CENTER);

		// Necessary to revalidate the frame so that we can see the table
		revalidate();
	}
}