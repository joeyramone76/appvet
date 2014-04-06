/* This software was developed by employees of the National Institute of
 * Standards and Technology (NIST), an agency of the Federal Government.
 * Pursuant to title 15 United States Code Section 105, works of NIST
 * employees are not subject to copyright protection in the United States
 * and are considered to be in the public domain.  As a result, a formal
 * license is not needed to use the software.
 * 
 * This software is provided by NIST as a service and is expressly
 * provided "AS IS".  NIST MAKES NO WARRANTY OF ANY KIND, EXPRESS, IMPLIED
 * OR STATUTORY, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTY OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NON-INFRINGEMENT
 * AND DATA ACCURACY.  NIST does not warrant or make any representations
 * regarding the use of the software or the results thereof including, but
 * not limited to, the correctness, accuracy, reliability or usefulness of
 * the software.
 * 
 * Permission to use this software is contingent upon your acceptance
 * of the terms of this agreement.
 */
package gov.nist.appvet.installer;

import gov.nist.appvet.installer.util.Authenticate;
import gov.nist.appvet.installer.util.Database;
import gov.nist.appvet.installer.util.FileUtil;
import gov.nist.appvet.installer.util.Validate;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

/**
 * $$Id: AppVetInstaller.java 38554 2014-03-30 16:06:12Z steveq $$
 */
public class AppVetInstaller implements ItemListener {

	private final String INSTALLER_FILES_DIR = "appvet_installer_files";
	private String APPVET_FILES_HOME = null;
	private String CATALINA_HOME = null;
	private String JAVA_HOME = null;
	private String os = null;

	// Host info
	private String ipAddr = null;
	private String hostname = null;
	private boolean keepApps = false;

	// AppVet info
	private String appVetUserName = null;
	private String appVetPassword = null;
	private String appVetLastName = null;
	private String appVetFirstName = null;
	private String appVetOrganization = null;
	private String appVetEmail = null;

	// Tomcat info
	private boolean tomcatSsl = false;
	private String tomcatPort = null;

	// MySQL info
	private String myqlUri = null;
	private String mysqlUsername = null;
	private String mysqlPassword = null;

	// Processing
	private JTextArea processingTextArea = null;
	private JButton doneButton = null;

	JPanel cards; // CardLayout panel
	final static String STARTPANEL = "STARTPANEL";
	final static String HOSTPANEL = "HOSTPANEL";
	final static String APPVETPANEL = "APPVETPANEL";
	final static String TOMCATMYSQLPANEL = "TOMCATMYSQLPANEL";
	final static String PROCESSPANEL = "PROCESSPANEL";

	private static JFrame frame;

	public AppVetInstaller() {

		String envVarExample = "";
		os = System.getProperty("os.name");
		if (!os.startsWith("Win") && !os.equals("Linux")) {
			System.out.println("AppVet Installer is not available for " + os);
			System.exit(0);
		} else if (os.startsWith("Win")) {
			envVarExample = "C:\\appvet_files";
		} else if (os.equals("Linux")) {
			envVarExample = "/home/appvet_files";
		}

		try {
			InetAddress addr = InetAddress.getLocalHost();
			ipAddr = addr.getHostAddress();

		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		JAVA_HOME = System.getenv("JAVA_HOME");
		if (JAVA_HOME == null || JAVA_HOME.isEmpty()) {
			JOptionPane.showMessageDialog(frame, 
					"Environment variable JAVA_HOME is undefined. Ensure\n"
							+ "that JAVA_HOME is set to a Java JDK and not a Java\n"
							+ "JRE before installing AppVet.",
							"AppVet Installer", JOptionPane.WARNING_MESSAGE);
			System.exit(0);
		}

		// Check if JAVA_HOME is set to JDK (which contains Jarsigner.exe)
		File file = new File(JAVA_HOME + "/bin/jarsigner.exe");
		if (!file.exists()) {
			JOptionPane.showMessageDialog(frame, 
					"JAVA_HOME does not seem to be set to a Java JDK.",
					"AppVet Installer", JOptionPane.WARNING_MESSAGE);
			System.exit(0);	
		}

		String PATH = System.getenv("PATH");
		if (os.startsWith("Win") && 
				(PATH == null || PATH.isEmpty() || PATH.indexOf(JAVA_HOME + "\\bin") < 0)) {
			JOptionPane.showMessageDialog(frame, 
					"AppVet requires that your PATH environment variable "
							+ "contain a path to " + JAVA_HOME + "\\bin directory.",
							"AppVet Installer", JOptionPane.WARNING_MESSAGE);
			System.exit(0);		
		} else if (os.startsWith("Linux") && 
				(PATH == null || PATH.isEmpty() || PATH.indexOf(JAVA_HOME + "/bin") < 0)) {
			JOptionPane.showMessageDialog(frame, 
					"AppVet requires that your PATH environment variable "
							+ "contain a path to " + JAVA_HOME + "/bin directory.",
							"AppVet Installer", JOptionPane.WARNING_MESSAGE);
			System.exit(0);
		}

		APPVET_FILES_HOME = System.getenv("APPVET_FILES_HOME");
		if (APPVET_FILES_HOME == null || APPVET_FILES_HOME.isEmpty()) {
			JOptionPane.showMessageDialog(frame, 
					"Environment variable APPVET_FILES_HOME must be defined\n"
							+ "before running the AppVet Installer (for example,\n"
							+ "APPVET_FILES_HOME=" + envVarExample + ") and must not\n"
							+ "contain any spaces or special characters.",
							"AppVet Installer", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		} else if (Validate.hasWhiteSpace(APPVET_FILES_HOME)) {
			JOptionPane.showMessageDialog(frame, 
					"The path defined by environment variable APPVET_FILES_HOME\n"
							+ "must not contain any spaces or special characters.",
							"AppVet Installer", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}

		CATALINA_HOME = System.getenv("CATALINA_HOME");
		if (CATALINA_HOME == null || CATALINA_HOME.isEmpty()) {
			JOptionPane.showMessageDialog(frame, 
					"Environment variable CATALINA_HOME is undefined. Ensure\n"
							+ "that Tomcat is installed and CATALINA_HOME is set\n"
							+ "before installing AppVet.",
							"AppVet Installer", JOptionPane.WARNING_MESSAGE);
			System.exit(0);
		}		
	}

	public void addComponentToPane(Container pane) {

		cards = new JPanel(new CardLayout());
		pane.add(cards, BorderLayout.CENTER);

		JPanel card1 = new JPanel();
		cards.add(card1, STARTPANEL);
		card1.setLayout(new BorderLayout(0, 0));

		JPanel logoPanel = new JPanel();
		logoPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		card1.add(logoPanel, BorderLayout.CENTER);
		logoPanel.setLayout(new BorderLayout(0, 0));

		JLabel nistLabel = new JLabel("");
		nistLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		logoPanel.add(nistLabel, BorderLayout.NORTH);
		String path = null;
		try {
			path = new File(".").getCanonicalPath();
		} catch (IOException e1) {
			showErrorMessage(e1.getMessage());
			e1.printStackTrace();
		}
		nistLabel.setIcon(new ImageIcon(path + "/" + INSTALLER_FILES_DIR + "/images/nist-gray.png"));

		JLabel appVetLogoLabel = new JLabel("");
		logoPanel.add(appVetLogoLabel);
		appVetLogoLabel.setHorizontalAlignment(SwingConstants.CENTER);
		try {
			path = new File(".").getCanonicalPath();
		} catch (IOException e1) {
			showErrorMessage(e1.getMessage());
			e1.printStackTrace();
		}
		appVetLogoLabel.setIcon(new ImageIcon(path + "/" + INSTALLER_FILES_DIR + "/images/appvet_logo.png"));

		JPanel startButtonPanel = new JPanel();
		card1.add(startButtonPanel, BorderLayout.SOUTH);
		startButtonPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null,
				null));

		JButton quitButton = new JButton("Quit");
		quitButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				System.exit(0);
			}
		});

		startButtonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		startButtonPanel.add(quitButton);

		JButton installButton = new JButton("Next");
		installButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
			}
		});
		installButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				CardLayout cl = (CardLayout) (cards.getLayout());
				cl.show(cards, HOSTPANEL);
			}
		});
		startButtonPanel.add(installButton);

		JPanel card2 = new JPanel();
		cards.add(card2, "HOSTPANEL");
		card2.setLayout(new BorderLayout(0, 0));

		JPanel panel_2 = new JPanel();
		card2.add(panel_2, BorderLayout.CENTER);
		panel_2.setLayout(new GridLayout(0, 1, 0, 0));

		final JPanel panel_4 = new JPanel();
		panel_4.setBorder(new TitledBorder(null, "Host", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_2.add(panel_4);
		GridBagLayout gbl_panel_4 = new GridBagLayout();
		gbl_panel_4.columnWidths = new int[]{0, 0, 0, 0};
		gbl_panel_4.rowHeights = new int[]{0, 0, 0, 0, 0};
		gbl_panel_4.columnWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_panel_4.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		panel_4.setLayout(gbl_panel_4);

		final JRadioButton useHostNameCheckBox = new JRadioButton("Use Hostname: ");
		useHostNameCheckBox.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
			}
		});
		useHostNameCheckBox.setSelected(true);
		GridBagConstraints gbc_useHostNameCheckBox = new GridBagConstraints();
		gbc_useHostNameCheckBox.insets = new Insets(0, 0, 5, 5);
		gbc_useHostNameCheckBox.gridx = 0;
		gbc_useHostNameCheckBox.gridy = 0;
		panel_4.add(useHostNameCheckBox, gbc_useHostNameCheckBox);

		final JTextField hostNameTextField = new JTextField();
		hostNameTextField.setText("host.example.com");
		GridBagConstraints gbc_hostNameTextField = new GridBagConstraints();
		gbc_hostNameTextField.insets = new Insets(0, 0, 5, 5);
		gbc_hostNameTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_hostNameTextField.gridx = 1;
		gbc_hostNameTextField.gridy = 0;
		panel_4.add(hostNameTextField, gbc_hostNameTextField);
		hostNameTextField.setColumns(10);

		final JRadioButton useStaticIp = new JRadioButton("Use Static IP: ");

		JButton useHostnameHelp = new JButton("?");
		useHostnameHelp.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				JOptionPane.showMessageDialog(frame, "Select hostname if "
						+ "AppVet is running in a production/operational\n"
						+ "environment. The selected hostname must be used\n"
						+ "to access the AppVet service.",
						"Help", JOptionPane.INFORMATION_MESSAGE);
			}
		});
		GridBagConstraints gbc_useHostnameHelp = new GridBagConstraints();
		gbc_useHostnameHelp.insets = new Insets(0, 0, 5, 0);
		gbc_useHostnameHelp.gridx = 2;
		gbc_useHostnameHelp.gridy = 0;
		panel_4.add(useHostnameHelp, gbc_useHostnameHelp);

		GridBagConstraints gbc_useStaticIp = new GridBagConstraints();
		gbc_useStaticIp.anchor = GridBagConstraints.WEST;
		gbc_useStaticIp.insets = new Insets(0, 0, 5, 5);
		gbc_useStaticIp.gridx = 0;
		gbc_useStaticIp.gridy = 1;
		panel_4.add(useStaticIp, gbc_useStaticIp);

		final JTextField ipTextField = new JTextField(ipAddr);
		ipTextField.setEditable(false);
		GridBagConstraints gbc_ipTextField = new GridBagConstraints();
		gbc_ipTextField.insets = new Insets(0, 0, 5, 5);
		gbc_ipTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_ipTextField.gridx = 1;
		gbc_ipTextField.gridy = 1;
		panel_4.add(ipTextField, gbc_ipTextField);
		ipTextField.setColumns(10);

		JButton useStaticIpHelp = new JButton("?");
		useStaticIpHelp.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				JOptionPane.showMessageDialog(frame, "Select static IP if "
						+ "AppVet is running in a development environment with\n"
						+ "a static IP address. This IP address must be used\n"
						+ "to access the AppVet service.",
						"Help", JOptionPane.INFORMATION_MESSAGE);
			}
		});
		GridBagConstraints gbc_useStaticIpHelp = new GridBagConstraints();
		gbc_useStaticIpHelp.insets = new Insets(0, 0, 5, 0);
		gbc_useStaticIpHelp.gridx = 2;
		gbc_useStaticIpHelp.gridy = 1;
		panel_4.add(useStaticIpHelp, gbc_useStaticIpHelp);

		final JRadioButton useDHCP = new JRadioButton("Use DHCP");
		GridBagConstraints gbc_useDHCP = new GridBagConstraints();
		gbc_useDHCP.anchor = GridBagConstraints.WEST;
		gbc_useDHCP.insets = new Insets(0, 0, 5, 5);
		gbc_useDHCP.gridx = 0;
		gbc_useDHCP.gridy = 2;
		panel_4.add(useDHCP, gbc_useDHCP);

		JButton useDHCPHelp = new JButton("?");
		useDHCPHelp.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				JOptionPane.showMessageDialog(frame, "Select DHCP if "
						+ "AppVet is running in a development environment with\n"
						+ "a dynamic IP address. This IP address will change\n"
						+ "over time.",
						"Help", JOptionPane.INFORMATION_MESSAGE);
			}
		});
		GridBagConstraints gbc_useDHCPHelp = new GridBagConstraints();
		gbc_useDHCPHelp.anchor = GridBagConstraints.WEST;
		gbc_useDHCPHelp.insets = new Insets(0, 0, 5, 5);
		gbc_useDHCPHelp.gridx = 1;
		gbc_useDHCPHelp.gridy = 2;
		panel_4.add(useDHCPHelp, gbc_useDHCPHelp);
		
        ButtonGroup group = new ButtonGroup();
        group.add(useHostNameCheckBox);
        group.add(useStaticIp);
        group.add(useDHCP);
        
		final JCheckBox keepAppsCheckBox = new JCheckBox("Keep Apps");
		keepAppsCheckBox.setHorizontalAlignment(SwingConstants.LEFT);
		GridBagConstraints gbc_keepAppsCheckBox = new GridBagConstraints();
		gbc_keepAppsCheckBox.insets = new Insets(0, 0, 0, 5);
		gbc_keepAppsCheckBox.anchor = GridBagConstraints.WEST;
		gbc_keepAppsCheckBox.gridx = 0;
		gbc_keepAppsCheckBox.gridy = 3;
		panel_4.add(keepAppsCheckBox, gbc_keepAppsCheckBox);

		JButton keepAppsHelp = new JButton("?");
		keepAppsHelp.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				JOptionPane.showMessageDialog(frame, "Select this option if "
						+ "you wish to keep received apps on the system. Note "
						+ "that keeping apps can use large amounts of disk "
						+ "space.",
						"Help", JOptionPane.INFORMATION_MESSAGE);
			}
		});
		GridBagConstraints gbc_keepAppsHelp = new GridBagConstraints();
		gbc_keepAppsHelp.insets = new Insets(0, 0, 0, 5);
		gbc_keepAppsHelp.anchor = GridBagConstraints.WEST;
		gbc_keepAppsHelp.gridx = 1;
		gbc_keepAppsHelp.gridy = 3;
		panel_4.add(keepAppsHelp, gbc_keepAppsHelp);

		JPanel panel_5 = new JPanel();
		panel_5.setBorder(new TitledBorder(null, "Java", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_2.add(panel_5);
		GridBagLayout gbl_panel_5 = new GridBagLayout();
		gbl_panel_5.columnWidths = new int[]{0, 0, 0};
		gbl_panel_5.rowHeights = new int[]{0, 0, 0};
		gbl_panel_5.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_panel_5.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		panel_5.setLayout(gbl_panel_5);

		JLabel lblNewLabel_6 = new JLabel("JAVA_HOME: ");
		GridBagConstraints gbc_lblNewLabel_6 = new GridBagConstraints();
		gbc_lblNewLabel_6.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel_6.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_6.gridx = 0;
		gbc_lblNewLabel_6.gridy = 1;
		panel_5.add(lblNewLabel_6, gbc_lblNewLabel_6);

		final JTextField javaHomeTextField = new JTextField();
		javaHomeTextField.setText(JAVA_HOME);
		javaHomeTextField.setEnabled(false);
		javaHomeTextField.setColumns(10);
		GridBagConstraints gbc_javaHomeTextField = new GridBagConstraints();
		gbc_javaHomeTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_javaHomeTextField.gridx = 1;
		gbc_javaHomeTextField.gridy = 1;
		panel_5.add(javaHomeTextField, gbc_javaHomeTextField);

		JPanel panel_3 = new JPanel();
		card2.add(panel_3, BorderLayout.SOUTH);

		JButton btnNewButton = new JButton("Cancel");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		btnNewButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				System.exit(0);
			}
		});
		panel_3.add(btnNewButton);

		JButton btnNewButton_1 = new JButton("Next");
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		btnNewButton_1.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				// Validate all fields
				String value = hostNameTextField.getText();
				if (useHostNameCheckBox.isSelected()) {
					if (value == null 
							|| value.isEmpty() 
							|| value.equals("host.example.com") 
							|| !Validate.isDomainName(value)) {
						showErrorMessage("Invalid hostname");
						return;
					} else {
						hostname = value;
					}
				} else if (useStaticIp.isSelected()) {
					hostname = ipTextField.getText();
				} else {
					hostname = "DHCP";
				}

				keepApps = keepAppsCheckBox.isSelected();

				// If fields are validated, go to next card
				CardLayout cl = (CardLayout) (cards.getLayout());
				cl.show(cards, APPVETPANEL);
			}
		});
		panel_3.add(btnNewButton_1);

		JPanel card3 = new JPanel();
		cards.add(card3, APPVETPANEL);
		card3.setLayout(new BorderLayout(0, 0));

		final JPanel appVetHomePanel = new JPanel();
		card3.add(appVetHomePanel, BorderLayout.CENTER);
		appVetHomePanel.setBorder(new TitledBorder(null, "AppVet",
				TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagLayout gbl_appVetHomePanel = new GridBagLayout();
		gbl_appVetHomePanel.columnWeights = new double[] { 0.0, 1.0 };
		gbl_appVetHomePanel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
				0.0, 0.0, 0.0, 0.0 };
		appVetHomePanel.setLayout(gbl_appVetHomePanel);

		JLabel appVetHomeLabel = new JLabel("  APPVET_FILES_HOME: ");
		GridBagConstraints gbc_appVetHomeLabel = new GridBagConstraints();
		gbc_appVetHomeLabel.anchor = GridBagConstraints.EAST;
		gbc_appVetHomeLabel.insets = new Insets(0, 0, 5, 5);
		gbc_appVetHomeLabel.gridx = 0;
		gbc_appVetHomeLabel.gridy = 0;
		appVetHomePanel.add(appVetHomeLabel, gbc_appVetHomeLabel);

		final JTextField appVetHomeTextField = new JTextField();
		appVetHomeTextField.setEnabled(false);
		appVetHomeTextField.setText(APPVET_FILES_HOME);

		GridBagConstraints gbc_appVetHomeTextField = new GridBagConstraints();
		gbc_appVetHomeTextField.insets = new Insets(0, 0, 5, 0);
		gbc_appVetHomeTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_appVetHomeTextField.gridx = 1;
		gbc_appVetHomeTextField.gridy = 0;
		appVetHomePanel.add(appVetHomeTextField, gbc_appVetHomeTextField);
		appVetHomeTextField.setColumns(10);

		JLabel label = new JLabel("");
		GridBagConstraints gbc_label = new GridBagConstraints();
		gbc_label.insets = new Insets(0, 0, 5, 5);
		gbc_label.gridx = 0;
		gbc_label.gridy = 1;
		appVetHomePanel.add(label, gbc_label);

		JLabel lblNewLabel_1 = new JLabel("  AppVet ADMIN ACCOUNT:");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_1.gridwidth = 2;
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 0);
		gbc_lblNewLabel_1.gridx = 0;
		gbc_lblNewLabel_1.gridy = 2;
		appVetHomePanel.add(lblNewLabel_1, gbc_lblNewLabel_1);

		JLabel adminUsernameLabel = new JLabel("  Username: ");
		GridBagConstraints gbc_adminUsernameLabel = new GridBagConstraints();
		gbc_adminUsernameLabel.anchor = GridBagConstraints.EAST;
		gbc_adminUsernameLabel.insets = new Insets(0, 0, 5, 5);
		gbc_adminUsernameLabel.gridx = 0;
		gbc_adminUsernameLabel.gridy = 3;
		appVetHomePanel.add(adminUsernameLabel, gbc_adminUsernameLabel);

		final JTextField adminUsernameTextField = new JTextField();
		GridBagConstraints gbc_adminUsernameTextField = new GridBagConstraints();
		gbc_adminUsernameTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_adminUsernameTextField.insets = new Insets(0, 0, 5, 0);
		gbc_adminUsernameTextField.gridx = 1;
		gbc_adminUsernameTextField.gridy = 3;
		appVetHomePanel.add(adminUsernameTextField, gbc_adminUsernameTextField);
		adminUsernameTextField.setColumns(10);

		JLabel adminPasswordLabel = new JLabel("  Password: ");
		GridBagConstraints gbc_adminPasswordLabel = new GridBagConstraints();
		gbc_adminPasswordLabel.anchor = GridBagConstraints.EAST;
		gbc_adminPasswordLabel.insets = new Insets(0, 0, 5, 5);
		gbc_adminPasswordLabel.gridx = 0;
		gbc_adminPasswordLabel.gridy = 4;
		appVetHomePanel.add(adminPasswordLabel, gbc_adminPasswordLabel);

		final JPasswordField adminPasswordField = new JPasswordField();
		GridBagConstraints gbc_adminPasswordField = new GridBagConstraints();
		gbc_adminPasswordField.fill = GridBagConstraints.HORIZONTAL;
		gbc_adminPasswordField.insets = new Insets(0, 0, 5, 0);
		gbc_adminPasswordField.gridx = 1;
		gbc_adminPasswordField.gridy = 4;
		appVetHomePanel.add(adminPasswordField, gbc_adminPasswordField);

		JLabel adminPasswordAgainLabel = new JLabel("  Password (again): ");
		GridBagConstraints gbc_adminPasswordAgainLabel = new GridBagConstraints();
		gbc_adminPasswordAgainLabel.anchor = GridBagConstraints.EAST;
		gbc_adminPasswordAgainLabel.insets = new Insets(0, 0, 5, 5);
		gbc_adminPasswordAgainLabel.gridx = 0;
		gbc_adminPasswordAgainLabel.gridy = 5;
		appVetHomePanel.add(adminPasswordAgainLabel, gbc_adminPasswordAgainLabel);

		final JPasswordField adminPasswordAgainTextField = new JPasswordField();
		GridBagConstraints gbc_adminPasswordAgainTextField = new GridBagConstraints();
		gbc_adminPasswordAgainTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_adminPasswordAgainTextField.insets = new Insets(0, 0, 5, 0);
		gbc_adminPasswordAgainTextField.gridx = 1;
		gbc_adminPasswordAgainTextField.gridy = 5;
		appVetHomePanel.add(adminPasswordAgainTextField,
				gbc_adminPasswordAgainTextField);
		adminPasswordAgainTextField.setColumns(2);

		JLabel lblNewLabel_3 = new JLabel("First Name: ");
		GridBagConstraints gbc_lblNewLabel_3 = new GridBagConstraints();
		gbc_lblNewLabel_3.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_3.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_3.gridx = 0;
		gbc_lblNewLabel_3.gridy = 6;
		appVetHomePanel.add(lblNewLabel_3, gbc_lblNewLabel_3);

		final JTextField firstNameTextField = new JTextField();
		GridBagConstraints gbc_firstNameTextField = new GridBagConstraints();
		gbc_firstNameTextField.insets = new Insets(0, 0, 5, 0);
		gbc_firstNameTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_firstNameTextField.gridx = 1;
		gbc_firstNameTextField.gridy = 6;
		appVetHomePanel.add(firstNameTextField, gbc_firstNameTextField);
		firstNameTextField.setColumns(10);

		JLabel lblNewLabel_2 = new JLabel("Last Name: ");
		GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
		gbc_lblNewLabel_2.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_2.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_2.gridx = 0;
		gbc_lblNewLabel_2.gridy = 7;
		appVetHomePanel.add(lblNewLabel_2, gbc_lblNewLabel_2);

		final JTextField lastNameTextField = new JTextField();
		GridBagConstraints gbc_lastNameTextField = new GridBagConstraints();
		gbc_lastNameTextField.insets = new Insets(0, 0, 5, 0);
		gbc_lastNameTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_lastNameTextField.gridx = 1;
		gbc_lastNameTextField.gridy = 7;
		appVetHomePanel.add(lastNameTextField, gbc_lastNameTextField);
		lastNameTextField.setColumns(10);

		JLabel lblOrganization = new JLabel("Organization: ");
		GridBagConstraints gbc_lblOrganization = new GridBagConstraints();
		gbc_lblOrganization.anchor = GridBagConstraints.EAST;
		gbc_lblOrganization.insets = new Insets(0, 0, 5, 5);
		gbc_lblOrganization.gridx = 0;
		gbc_lblOrganization.gridy = 8;
		appVetHomePanel.add(lblOrganization, gbc_lblOrganization);

		final JTextField organizationTextField = new JTextField();
		GridBagConstraints gbc_organizationTextField = new GridBagConstraints();
		gbc_organizationTextField.insets = new Insets(0, 0, 5, 0);
		gbc_organizationTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_organizationTextField.gridx = 1;
		gbc_organizationTextField.gridy = 8;
		appVetHomePanel.add(organizationTextField, gbc_organizationTextField);
		organizationTextField.setColumns(10);

		JLabel lblEmail = new JLabel("Email: ");
		GridBagConstraints gbc_lblEmail = new GridBagConstraints();
		gbc_lblEmail.anchor = GridBagConstraints.EAST;
		gbc_lblEmail.insets = new Insets(0, 0, 5, 5);
		gbc_lblEmail.gridx = 0;
		gbc_lblEmail.gridy = 9;
		appVetHomePanel.add(lblEmail, gbc_lblEmail);

		final JTextField emailTextField = new JTextField();
		GridBagConstraints gbc_emailTextField = new GridBagConstraints();
		gbc_emailTextField.insets = new Insets(0, 0, 5, 0);
		gbc_emailTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_emailTextField.gridx = 1;
		gbc_emailTextField.gridy = 9;
		appVetHomePanel.add(emailTextField, gbc_emailTextField);
		emailTextField.setColumns(10);

		JPanel appVetButtonPanel = new JPanel();
		card3.add(appVetButtonPanel, BorderLayout.SOUTH);

		JButton appVetCancelButton = new JButton("Cancel");
		appVetCancelButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				System.exit(0);
			}
		});
		appVetButtonPanel.add(appVetCancelButton);

		JButton appVetNextButton = new JButton("Next");
		appVetNextButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		appVetNextButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				// Validate all fields
				String value = adminUsernameTextField.getText();
				if (!Validate.isValidUserName(value)) {
					showErrorMessage("Invalid username");
					return;
				} else {
					appVetUserName = value;
				}

				value = adminPasswordField.getText();
				if (!Validate.isValidPassword(value)) {
					showErrorMessage("Invalid password");
					return;
				} else if (!value.equals(adminPasswordAgainTextField.getText())) {
					showErrorMessage("Passwords do not match");
					return;
				} else {
					appVetPassword = value;
				}
				
				value = firstNameTextField.getText();
				if (!Validate.isAlpha(value)) {
					showErrorMessage("Invalid first name");
					return;
				} else {
					appVetFirstName = value;
				}
				
				value = lastNameTextField.getText();
				if (!Validate.isAlpha(value)) {
					showErrorMessage("Invalid last name");
					return;
				} else {
					appVetLastName = value;
				}
				
				value = organizationTextField.getText();
				if (value == null || value.isEmpty()) {
					showErrorMessage("Invalid organization");
					return;
				} else {
					appVetOrganization = value;

				}

				value = emailTextField.getText();
				if (!Validate.isValidEmail(value)) {
					showErrorMessage("Invalid email");
					return;
				} else {
					appVetEmail = value;

				}
				// If fields are validated, go to next card
				CardLayout cl = (CardLayout) (cards.getLayout());
				cl.show(cards, TOMCATMYSQLPANEL);
			}
		});
		appVetButtonPanel.add(appVetNextButton);

		JPanel card4 = new JPanel();
		cards.add(card4, TOMCATMYSQLPANEL);
		card4.setLayout(new BorderLayout(0, 0));

		JPanel tomcatMysqlMainPanel = new JPanel();
		card4.add(tomcatMysqlMainPanel, BorderLayout.CENTER);
		GridBagLayout gbl_tomcatMysqlMainPanel = new GridBagLayout();
		gbl_tomcatMysqlMainPanel.columnWidths = new int[] { 0, 0 };
		gbl_tomcatMysqlMainPanel.rowHeights = new int[] { 0, 0, 0 };
		gbl_tomcatMysqlMainPanel.columnWeights = new double[] { 1.0,
				Double.MIN_VALUE };
		gbl_tomcatMysqlMainPanel.rowWeights = new double[] { 1.0, 1.0,
				Double.MIN_VALUE };
		tomcatMysqlMainPanel.setLayout(gbl_tomcatMysqlMainPanel);

		JPanel tomcatHomePanel = new JPanel();
		tomcatHomePanel.setBorder(new TitledBorder(null, "Apache Tomcat",
				TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_tomcatHomePanel = new GridBagConstraints();
		gbc_tomcatHomePanel.insets = new Insets(0, 0, 5, 0);
		gbc_tomcatHomePanel.fill = GridBagConstraints.BOTH;
		gbc_tomcatHomePanel.gridx = 0;
		gbc_tomcatHomePanel.gridy = 0;
		tomcatMysqlMainPanel.add(tomcatHomePanel, gbc_tomcatHomePanel);
		GridBagLayout gbl_tomcatHomePanel = new GridBagLayout();
		gbl_tomcatHomePanel.columnWeights = new double[] { 0.0, 1.0, 0.0 };
		gbl_tomcatHomePanel.rowWeights = new double[] { 0.0, 0.0, 0.0 };
		tomcatHomePanel.setLayout(gbl_tomcatHomePanel);

		JLabel tomcatHomeLabel = new JLabel("  CATALINA_HOME: ");
		GridBagConstraints gbc_tomcatHomeLabel = new GridBagConstraints();
		gbc_tomcatHomeLabel.anchor = GridBagConstraints.WEST;
		gbc_tomcatHomeLabel.insets = new Insets(0, 0, 5, 5);
		gbc_tomcatHomeLabel.gridx = 0;
		gbc_tomcatHomeLabel.gridy = 0;
		tomcatHomePanel.add(tomcatHomeLabel, gbc_tomcatHomeLabel);

		final JTextField tomcatHomeTextField = new JTextField(CATALINA_HOME);
		tomcatHomeTextField.setEnabled(false);
		GridBagConstraints gbc_tomcatHomeTextField = new GridBagConstraints();
		gbc_tomcatHomeTextField.insets = new Insets(0, 0, 5, 5);
		gbc_tomcatHomeTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_tomcatHomeTextField.gridx = 1;
		gbc_tomcatHomeTextField.gridy = 0;
		tomcatHomePanel.add(tomcatHomeTextField, gbc_tomcatHomeTextField);
		tomcatHomeTextField.setColumns(10);

		JLabel lblPortsAndKeystores = new JLabel(
				"  SSL and port must match Tomcat server.xml configuration");
		lblPortsAndKeystores.setHorizontalAlignment(SwingConstants.LEFT);
		GridBagConstraints gbc_lblPortsAndKeystores = new GridBagConstraints();
		gbc_lblPortsAndKeystores.anchor = GridBagConstraints.WEST;
		gbc_lblPortsAndKeystores.gridwidth = 3;
		gbc_lblPortsAndKeystores.insets = new Insets(0, 0, 5, 0);
		gbc_lblPortsAndKeystores.gridx = 0;
		gbc_lblPortsAndKeystores.gridy = 1;
		tomcatHomePanel.add(lblPortsAndKeystores, gbc_lblPortsAndKeystores);

		final JCheckBox sslCheckBox = new JCheckBox(" SSL        Port: ");
		GridBagConstraints gbc_sslCheckBox = new GridBagConstraints();
		gbc_sslCheckBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_sslCheckBox.insets = new Insets(0, 0, 5, 5);
		gbc_sslCheckBox.gridx = 0;
		gbc_sslCheckBox.gridy = 2;
		tomcatHomePanel.add(sslCheckBox, gbc_sslCheckBox);

		final JTextField portTextField = new JTextField();
		GridBagConstraints gbc_portTextField = new GridBagConstraints();
		gbc_portTextField.anchor = GridBagConstraints.WEST;
		gbc_portTextField.insets = new Insets(0, 0, 5, 5);
		gbc_portTextField.gridx = 1;
		gbc_portTextField.gridy = 2;
		tomcatHomePanel.add(portTextField, gbc_portTextField);
		portTextField.setColumns(10);

		JPanel mysqlAuthenticationPanel = new JPanel();
		mysqlAuthenticationPanel.setBorder(new TitledBorder(null,
				"MySQL Authentication", TitledBorder.LEADING, TitledBorder.TOP,
				null, null));
		GridBagConstraints gbc_mysqlAuthenticationPanel = new GridBagConstraints();
		gbc_mysqlAuthenticationPanel.fill = GridBagConstraints.BOTH;
		gbc_mysqlAuthenticationPanel.gridx = 0;
		gbc_mysqlAuthenticationPanel.gridy = 1;
		tomcatMysqlMainPanel.add(mysqlAuthenticationPanel,
				gbc_mysqlAuthenticationPanel);
		GridBagLayout gbl_mysqlAuthenticationPanel = new GridBagLayout();
		gbl_mysqlAuthenticationPanel.columnWeights = new double[] { 0.0, 1.0 };
		gbl_mysqlAuthenticationPanel.rowWeights = new double[] { 0.0, 0.0, 0.0 };
		mysqlAuthenticationPanel.setLayout(gbl_mysqlAuthenticationPanel);

		JLabel lblNewLabel = new JLabel("URI: ");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		mysqlAuthenticationPanel.add(lblNewLabel, gbc_lblNewLabel);

		final JTextField mysqlUriTextField = new JTextField(
				"jdbc:mysql://localhost");
		mysqlUriTextField.setEnabled(false);
		GridBagConstraints gbc_mysqlUrlTextBox = new GridBagConstraints();
		gbc_mysqlUrlTextBox.insets = new Insets(0, 0, 5, 0);
		gbc_mysqlUrlTextBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_mysqlUrlTextBox.gridx = 1;
		gbc_mysqlUrlTextBox.gridy = 0;
		mysqlAuthenticationPanel.add(mysqlUriTextField, gbc_mysqlUrlTextBox);
		mysqlUriTextField.setColumns(10);

		JLabel mysqlUsernameLabel = new JLabel("  Username: ");
		GridBagConstraints gbc_mysqlUsernameLabel = new GridBagConstraints();
		gbc_mysqlUsernameLabel.insets = new Insets(0, 0, 5, 5);
		gbc_mysqlUsernameLabel.anchor = GridBagConstraints.EAST;
		gbc_mysqlUsernameLabel.gridx = 0;
		gbc_mysqlUsernameLabel.gridy = 1;
		mysqlAuthenticationPanel
		.add(mysqlUsernameLabel, gbc_mysqlUsernameLabel);

		final JTextField mysqUsernameTextBox = new JTextField("root");
		GridBagConstraints gbc_mysqUsernameTextBox = new GridBagConstraints();
		gbc_mysqUsernameTextBox.insets = new Insets(0, 0, 5, 0);
		gbc_mysqUsernameTextBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_mysqUsernameTextBox.gridx = 1;
		gbc_mysqUsernameTextBox.gridy = 1;
		mysqlAuthenticationPanel.add(mysqUsernameTextBox,
				gbc_mysqUsernameTextBox);
		mysqUsernameTextBox.setColumns(10);

		JLabel mysqlPasswordLabel = new JLabel("  Password: ");
		GridBagConstraints gbc_mysqlPasswordLabel = new GridBagConstraints();
		gbc_mysqlPasswordLabel.anchor = GridBagConstraints.EAST;
		gbc_mysqlPasswordLabel.insets = new Insets(0, 0, 0, 5);
		gbc_mysqlPasswordLabel.gridx = 0;
		gbc_mysqlPasswordLabel.gridy = 2;
		mysqlAuthenticationPanel
		.add(mysqlPasswordLabel, gbc_mysqlPasswordLabel);

		final JPasswordField mysqlPasswordField = new JPasswordField();

		GridBagConstraints gbc_mysqlPasswordField = new GridBagConstraints();
		gbc_mysqlPasswordField.fill = GridBagConstraints.HORIZONTAL;
		gbc_mysqlPasswordField.gridx = 1;
		gbc_mysqlPasswordField.gridy = 2;
		mysqlAuthenticationPanel
		.add(mysqlPasswordField, gbc_mysqlPasswordField);

		JPanel tomcatMysqlButtonPanel = new JPanel();
		card4.add(tomcatMysqlButtonPanel, BorderLayout.SOUTH);

		JButton tomcatMysqlCancelButton = new JButton("Cancel");
		tomcatMysqlCancelButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				JOptionPane.showMessageDialog(frame, "AppVet was not installed",
						"AppVet Installer", JOptionPane.INFORMATION_MESSAGE);
				System.exit(0);
			}
		});

		tomcatMysqlCancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
			}
		});
		tomcatMysqlButtonPanel.add(tomcatMysqlCancelButton);

		JButton appVetInstallButton = new JButton("Install");
		appVetInstallButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				// Validate all fields
				tomcatSsl = sslCheckBox.isSelected();

				String value = portTextField.getText();
				if (!Validate.isNumeric(value)) {
					showErrorMessage("Port number must be numeric");
					return;
				} else {
					tomcatPort = value;
				}

				value = mysqlUriTextField.getText();
				if (!Validate.isUrl(value)) {
					showErrorMessage("MySQL URL is invalid");
					return;
				} else {
					myqlUri = value;
				}

				value = mysqUsernameTextBox.getText();
				if (!Validate.isValidUserName(value)) {
					showErrorMessage("Invalid MySQL username");
					return;
				} else {
					mysqlUsername = value;
				}

				value = mysqlPasswordField.getText();
				if (!Validate.isValidPassword(value)) {
					showErrorMessage("Invalid MySQL password");
					return;
				} else {
					mysqlPassword = value;
				}

				CardLayout cl = (CardLayout) (cards.getLayout());
				cl.show(cards, PROCESSPANEL);

				Installer installer = new Installer();
				Thread thread = new Thread(installer);
				thread.start();
			}
		});
		tomcatMysqlButtonPanel.add(appVetInstallButton);

		JPanel card5 = new JPanel();
		cards.add(card5, PROCESSPANEL);
		card5.setLayout(new BorderLayout(0, 0));

		JPanel panel = new JPanel();
		card5.add(panel, BorderLayout.CENTER);
		panel.setLayout(new BorderLayout(0, 0));

		processingTextArea = new JTextArea();
		panel.add(processingTextArea, BorderLayout.CENTER);

		JPanel panel_1 = new JPanel();
		card5.add(panel_1, BorderLayout.SOUTH);
		panel_1.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		doneButton = new JButton("Done");
		doneButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				System.exit(0);
			}
		});
		doneButton.setEnabled(false);
		panel_1.add(doneButton);

	}

	class Installer implements Runnable {

		public void run() {
			// Create MySQL Tables
			System.out.println("Creating database tables...");
			processingTextArea.append("Creating database tables...\n");

			Database db = new Database(myqlUri, mysqlUsername, mysqlPassword);
			String sql = "create database appvet";
			if (!db.update(sql)) {
				showErrorMessage("Could not create database. Shutting down.");
				System.exit(0);
			}

			db.setDatabase("appvet");

			// Create users table
			sql = "CREATE TABLE users (username VARCHAR(32), PRIMARY KEY (username), password VARCHAR(102), org VARCHAR(120), email VARCHAR(120), role VARCHAR(48), lastlogon TIMESTAMP, fromhost VARCHAR(120), lastName VARCHAR(32), firstName VARCHAR(32));";
			if (!db.update(sql)) {
				showErrorMessage("Could not create users table. Shutting down.");
				System.exit(0);
			}

			// Create sessions table
			sql = "CREATE TABLE sessions (sessionid VARCHAR(32), PRIMARY KEY (sessionid), username VARCHAR(120), clientaddress VARCHAR(120), expiretime BIGINT);";
			if (!db.update(sql)) {
				showErrorMessage("Could not create sessions table. Shutting down.");
				System.exit(0);
			}

			// Create apps table
			sql = "CREATE TABLE apps (appid VARCHAR(32), PRIMARY KEY (appid), appname VARCHAR(120), packagename VARCHAR(120), versioncode VARCHAR(120), versionname VARCHAR(120), filename VARCHAR(120), submittime TIMESTAMP NULL, appstatus VARCHAR(120), statustime TIMESTAMP NULL, username VARCHAR(120), clienthost VARCHAR(120));";
			if (!db.update(sql)) {
				showErrorMessage("Could not create apps table. Shutting down.");
				System.exit(0);
			}

			// Create toolstatus table
			sql = "CREATE TABLE toolstatus (appid VARCHAR(32), PRIMARY KEY (appid), registration VARCHAR(120), appinfo VARCHAR(120));";
			if (!db.update(sql)) {
				showErrorMessage("Could not create toolstatus table. Shutting down.");
				System.exit(0);
			}

			// Add AppVet admin
			try {
				System.out.println("Adding admin account...");
				processingTextArea.append("Adding admin account...\n");

				final String passwordHash = Authenticate.createHash(appVetPassword);
				sql = "INSERT INTO users VALUES ('" + appVetUserName + "','"
						+ passwordHash + "','" + appVetOrganization + "','" + appVetEmail
						+ "','ADMIN', 0, null, '" + appVetLastName + "','"
						+ appVetFirstName + "');";
				if (!db.update(sql)) {
					showErrorMessage("Could not add admin account. Shutting down.");
					System.exit(0);
				}

			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			} catch (InvalidKeySpecException e) {
				e.printStackTrace();
			}

			System.out.println("Creating directories...");
			processingTextArea.append("Creating directories...\n");

			// Install AppVet files
			String dirPath = APPVET_FILES_HOME + "/apps";
			File f = new File(dirPath);
			if (!f.exists()) {
				new File(dirPath).mkdirs();
			}
			dirPath = APPVET_FILES_HOME + "/conf";
			f = new File(dirPath);
			if (!f.exists()) {
				new File(dirPath).mkdirs();
			}
			dirPath = APPVET_FILES_HOME + "/conf/tool_adapters";
			f = new File(dirPath);
			if (!f.exists()) {
				new File(dirPath).mkdirs();
			}
			dirPath = APPVET_FILES_HOME + "/logs";
			f = new File(dirPath);
			if (!f.exists()) {
				new File(dirPath).mkdirs();
			}
			dirPath = CATALINA_HOME + "/webapps/appvet_images";
			f = new File(dirPath);
			if (!f.exists()) {
				new File(dirPath).mkdirs();
			}

			// Modify AppVetProperties.xml with given user input data
			System.out.println("Creating properties file...");
			processingTextArea.append("Creating properties file...\n");

			String appVetProperties = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
					+ "<appvet:AppVet xmlns:appvet=\"http://csrc.nist.gov/groups/SNS/appvet\" \n"
					+ "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \n"
					+ "xsi:schemaLocation=\"http://csrc.nist.gov/groups/SNS/appvet AppVetProperties.xsd\">\n"					
					+ " <appvet:Host>\n" 
					+ "    <appvet:Hostname></appvet:Hostname>\n"
					+ "    <appvet:SSL></appvet:SSL>\n"
					+ "    <appvet:Port></appvet:Port>\n" 
					+ " </appvet:Host>\n"
					+ " <appvet:Logging>\n" 
					+ "    <appvet:Level>INFO</appvet:Level>\n"
					+ "    <appvet:ToConsole>false</appvet:ToConsole>\n"
					+ "</appvet:Logging>\n" 
					+ "<appvet:Sessions>\n"
					+ "    <appvet:Timeout>1800000</appvet:Timeout>\n"
					+ "    <appvet:GetUpdatesDelay>5000</appvet:GetUpdatesDelay> \n"
					+ "</appvet:Sessions>\n" 
					+ "<appvet:Database>\n"
					+ "    <appvet:URL></appvet:URL>\n"
					+ "    <appvet:UserName></appvet:UserName>\n"
					+ "    <appvet:Password></appvet:Password>\n" 
					+ "</appvet:Database>\n"
					+ "<appvet:ToolServices>\n"
					+ "    <appvet:PollingInterval>2000</appvet:PollingInterval>\n"
					+ "    <appvet:StaggerInterval>1000</appvet:StaggerInterval>\n"
					+ "    <appvet:ConnectionTimeout>30000</appvet:ConnectionTimeout>\n"
					+ "    <appvet:SocketTimeout>1200000</appvet:SocketTimeout>\n"
					+ "    <appvet:Timeout>1500000</appvet:Timeout>\n"					
					+ "</appvet:ToolServices>  \n" 
					+ "<appvet:Apps>\n"
					+ "    <appvet:KeepApps>false</appvet:KeepApps>\n"					
					+ "</appvet:Apps>\n" 
					+ "</appvet:AppVet>";

			// Copy appvet_images directory and default.png file
			try {
				String currentDirectory = System.getProperty("user.dir");
				FileUtil.copyFile(new File(currentDirectory
						+ "/" + INSTALLER_FILES_DIR + "/deploy/tomcat/webapps/appvet_images/default.png"), new File(
								CATALINA_HOME + "/webapps/appvet_images/default.png"));
			} catch (IOException e) {
				e.printStackTrace();
			}
			// Copy registration and appinfo tool adapters
			try {
				String currentDirectory = System.getProperty("user.dir");
				FileUtil.copyFile(new File(currentDirectory
						+ "/" + INSTALLER_FILES_DIR + "/deploy/conf/tool_adapters/registration.xml"), new File(
								APPVET_FILES_HOME + "/conf/tool_adapters/registration.xml"));
				FileUtil.copyFile(new File(currentDirectory
						+ "/" + INSTALLER_FILES_DIR + "/deploy/conf/tool_adapters/appinfo.xml"), new File(
								APPVET_FILES_HOME + "/conf/tool_adapters/appinfo.xml"));		
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
			// Add complementary tools. These tools may be unavailable at time of deployment.
			try {
				String currentDirectory = System.getProperty("user.dir");
				FileUtil.copyFile(new File(currentDirectory
						+ "/" + INSTALLER_FILES_DIR + "/deploy/conf/unused_tool_adapters/androidcert.xml"), new File(
								APPVET_FILES_HOME + "/conf/tool_adapters/androidcert.xml"));		
			} catch (IOException e1) {
				e1.printStackTrace();
			}

			// Set Hostname
			String str = appVetProperties.replaceAll("<appvet:Hostname></appvet:Hostname>",
					"<appvet:Hostname>" + hostname + "</appvet:Hostname>");

			// Set keep apps
			String str0 = str.replaceAll("<appvet:KeepApps></appvet:KeepApps>",
					"<appvet:KeepApps>" + keepApps + "</appvet:KeepApps>");

			// Set SSL
			String str1 = str0.replaceAll("<appvet:SSL></appvet:SSL>",
					"<appvet:SSL>" + tomcatSsl + "</appvet:SSL>");
			// Set Port
			String str2 = str1.replaceAll("<appvet:Port></appvet:Port>", "<appvet:Port>"
					+ tomcatPort + "</appvet:Port>");
			// Set database URL
			String str3 = str2.replaceAll("<appvet:URL></appvet:URL>", "<appvet:URL>"
					+ myqlUri + "</appvet:URL>");
			// Set database username
			String str4 = str3.replaceAll("<appvet:UserName></appvet:UserName>",
					"<appvet:UserName>" + mysqlUsername + "</appvet:UserName>");
			// Set database password
			String str5 = str4.replaceAll("<appvet:Password></appvet:Password>",
					"<appvet:Password>" + mysqlPassword + "</appvet:Password>");

			// Write AppVetProperties.xml file
			f = new File(APPVET_FILES_HOME + "/conf/AppVetProperties.xml");
			try {
				f.createNewFile();
				PrintWriter out = new PrintWriter(APPVET_FILES_HOME
						+ "/conf/AppVetProperties.xml");
				out.println(str5);
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

			// Create AppVet log file
			f = new File(APPVET_FILES_HOME + "/logs/appvet_log.txt");
			try {
				f.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}

			// TODO: Install WAR file into $TOMCAT/webapps
			System.out.println("Loading appvet.war file... (TBD)");
			processingTextArea.append("Loading appvet.war file (TBD)...\n");

			// Display completion
			System.out.println("AppVet Installed!");
			processingTextArea.append("AppVet Installed!\n");
			doneButton.setEnabled(true);
		}

	}

	public void showErrorMessage(String errorMessage) {
		JOptionPane.showMessageDialog(frame, errorMessage,
				"Input Validation Error", JOptionPane.ERROR_MESSAGE);
		return;
	}

	public void itemStateChanged(ItemEvent evt) {
		CardLayout cl = (CardLayout) (cards.getLayout());
		cl.show(cards, (String) evt.getItem());
	}

	/**
	 * Create the GUI and show it. For thread safety, this method should be
	 * invoked from the event dispatch thread.
	 */
	private void createAndShowGUI() {
		String path = null;
		try {
			path = new File(".").getCanonicalPath();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		// Create and set up the window.
		frame = new JFrame("AppVet Installer");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setIconImage(Toolkit.getDefaultToolkit().getImage(
				path + "/" + INSTALLER_FILES_DIR + "/images/appvet_icon.png"));
		frame.setTitle("AppVet Installer");
		frame.setBounds(100, 100, 432, 332);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setVisible(true);
		frame.setLocationRelativeTo(null);

		// Create and set up the content pane.
		AppVetInstaller demo = new AppVetInstaller();
		demo.addComponentToPane(frame.getContentPane());

		// Display the window.
		frame.setVisible(true);
	}

	public static void main(String[] args) {

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (UnsupportedLookAndFeelException ex) {
			ex.printStackTrace();
		} catch (IllegalAccessException ex) {
			ex.printStackTrace();
		} catch (InstantiationException ex) {
			ex.printStackTrace();
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		}

		final AppVetInstaller appVetInstaller = new AppVetInstaller();

		// Schedule a job for the event dispatch thread:
		// creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				appVetInstaller.createAndShowGUI();
			}
		});
	}
}
