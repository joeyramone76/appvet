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
package gov.nist.appvet.gwt.client.gui;

import gov.nist.appvet.gwt.client.GWTService;
import gov.nist.appvet.gwt.client.GWTServiceAsync;
import gov.nist.appvet.gwt.client.gui.dialog.MessageDialogBox;
import gov.nist.appvet.gwt.shared.AppInfoGwt;
import gov.nist.appvet.gwt.shared.ConfigInfoGwt;
import gov.nist.appvet.properties.AppVetProperties;
import gov.nist.appvet.shared.validate.ValidateBase;

import java.util.List;
import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * $$Id: LoginPanel.java 38554 2014-03-30 16:06:12Z steveq $$
 */
public class LoginPanel extends DockLayoutPanel {

	// See appvet.gwt.xml
	private static Logger log = Logger.getLogger("LoginPanel");
	private final Button loginButton = new Button("LOGIN");
	private final TextBox userNameTextBox = new TextBox();
	private final PasswordTextBox passwordTextBox = new PasswordTextBox();
	private final Label loginStatusLabel = new Label("");
	private final GWTServiceAsync appVetService = GWT.create(GWTService.class);
	private static MessageDialogBox messageDialogBox = null;

	public LoginPanel(Unit unit) {
		super(Unit.PX);

		final String version = AppVetProperties.VERSION;
		log.info("Starting AppVet v" + version + "...");

		setSize("100%", "");
		
		DockPanel dockPanel_1 = new DockPanel();
		dockPanel_1.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
		addNorth(dockPanel_1, 32.0);
		dockPanel_1.setWidth("100%");
		
		Image image = new Image("images/nist-gray.png");
		image.setStyleName("nistLoginLogo");
		dockPanel_1.add(image, DockPanel.EAST);
		dockPanel_1.setCellVerticalAlignment(image, HasVerticalAlignment.ALIGN_MIDDLE);
		dockPanel_1.setCellHorizontalAlignment(image, HasHorizontalAlignment.ALIGN_RIGHT);

		final VerticalPanel centerVerticalPanel = new VerticalPanel();
		centerVerticalPanel
				.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		centerVerticalPanel
				.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		add(centerVerticalPanel);
		centerVerticalPanel.setSize("100%", "90%");

		final DecoratorPanel decoratorPanel = new DecoratorPanel();
		centerVerticalPanel.add(decoratorPanel);
		centerVerticalPanel.setCellWidth(decoratorPanel, "100%");
		decoratorPanel.setSize("", "");
		centerVerticalPanel.setCellHorizontalAlignment(decoratorPanel,
				HasHorizontalAlignment.ALIGN_CENTER);
		centerVerticalPanel.setCellVerticalAlignment(decoratorPanel,
				HasVerticalAlignment.ALIGN_MIDDLE);

		final DockPanel dockPanel = new DockPanel();
		dockPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		dockPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		decoratorPanel.setWidget(dockPanel);
		dockPanel.setSize("100%", "200px");

		final VerticalPanel verticalPanel = new VerticalPanel();
		verticalPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		verticalPanel
				.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		dockPanel.add(verticalPanel, DockPanel.NORTH);
		dockPanel.setCellWidth(verticalPanel, "100%");
		dockPanel.setCellVerticalAlignment(verticalPanel,
				HasVerticalAlignment.ALIGN_MIDDLE);
		dockPanel.setCellHorizontalAlignment(verticalPanel,
				HasHorizontalAlignment.ALIGN_CENTER);
		verticalPanel.setSize("300px", "90px");

		final Image appVetImage = new Image("images/appvet_logo.png");
		appVetImage.setStyleName("loginPanelLogo");
		verticalPanel.add(appVetImage);
		verticalPanel.setCellHorizontalAlignment(appVetImage,
				HasHorizontalAlignment.ALIGN_CENTER);
		verticalPanel.setCellVerticalAlignment(appVetImage,
				HasVerticalAlignment.ALIGN_MIDDLE);
		appVetImage.setSize("192px", "73px");

		loginStatusLabel.setStyleName("submissionRequirementsLabel");
		verticalPanel.add(loginStatusLabel);
		loginStatusLabel.setVisible(true);
		loginStatusLabel.setSize("200px", "20px");
		verticalPanel.setCellHorizontalAlignment(loginStatusLabel,
				HasHorizontalAlignment.ALIGN_CENTER);

		final Grid grid = new Grid(2, 2);
		grid.setStyleName("loginGrid");
		dockPanel.add(grid, DockPanel.CENTER);
		dockPanel.setCellWidth(grid, "100%");
		grid.setHeight("");
		dockPanel.setCellVerticalAlignment(grid,
				HasVerticalAlignment.ALIGN_MIDDLE);
		dockPanel.setCellHorizontalAlignment(grid,
				HasHorizontalAlignment.ALIGN_CENTER);

		final Label usernameLabel = new Label("USERNAME");
		usernameLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
		grid.setWidget(0, 0, usernameLabel);
		usernameLabel.setSize("100px", "20px");
		grid.getCellFormatter().setHorizontalAlignment(0, 0,
				HasHorizontalAlignment.ALIGN_LEFT);
		grid.getCellFormatter().setVerticalAlignment(0, 0,
				HasVerticalAlignment.ALIGN_MIDDLE);

		grid.setWidget(0, 1, userNameTextBox);
		userNameTextBox.setSize("180px", "15px");
		grid.getCellFormatter().setHorizontalAlignment(0, 1,
				HasHorizontalAlignment.ALIGN_RIGHT);
		grid.getCellFormatter().setVerticalAlignment(0, 1,
				HasVerticalAlignment.ALIGN_MIDDLE);

		final Label passwordLabel = new Label("PASSWORD");
		passwordLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
		grid.setWidget(1, 0, passwordLabel);
		passwordLabel.setSize("100px", "20px");
		grid.getCellFormatter().setHorizontalAlignment(1, 0,
				HasHorizontalAlignment.ALIGN_LEFT);
		grid.getCellFormatter().setVerticalAlignment(1, 0,
				HasVerticalAlignment.ALIGN_MIDDLE);

		grid.setWidget(1, 1, passwordTextBox);
		passwordTextBox.setSize("180px", "15px");
		grid.getCellFormatter().setHorizontalAlignment(1, 1,
				HasHorizontalAlignment.ALIGN_RIGHT);
		grid.getCellFormatter().setVerticalAlignment(1, 1,
				HasVerticalAlignment.ALIGN_MIDDLE);
		passwordTextBox.addKeyPressHandler(new KeyPressHandler() {

			@Override
			public void onKeyPress(KeyPressEvent event_) {
				final boolean enterPressed = KeyCodes.KEY_ENTER == event_
						.getNativeEvent().getKeyCode();
				if (enterPressed) {
					getUserInput();
				}
			}

		});
		final SimplePanel simplePanel_3 = new SimplePanel();
		simplePanel_3.setStyleName("buttonPanel");
		dockPanel.add(simplePanel_3, DockPanel.SOUTH);
		simplePanel_3.setHeight("28px");
		dockPanel.setCellVerticalAlignment(simplePanel_3,
				HasVerticalAlignment.ALIGN_MIDDLE);
		dockPanel.setCellHorizontalAlignment(simplePanel_3,
				HasHorizontalAlignment.ALIGN_CENTER);
		loginButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				getUserInput();
			}

		});
		simplePanel_3.setWidget(loginButton);
		loginButton.setText("LOGIN");
		loginButton.setSize("78px", "26px");
	}

	public void authenticate(final String username, final String password) {
		appVetService.authenticate(username, password,
				new AsyncCallback<ConfigInfoGwt>() {

					@Override
					public void onFailure(Throwable caught) {
						showMessageDialog("AppVet Error",
								"Authentication system error", true);
						return;
					}

					@Override
					public void onSuccess(final ConfigInfoGwt result) {
						if (result == null) {
							showMessageDialog("AppVet Login Error",
									"Unknown username or password", true);
							return;
						} else {
							loginStatusLabel.setText("Retrieving data...");
							startAppVet(result);
						}
					}

				});
	}

	public void getUserInput() {
		loginButton.setEnabled(false);
		final String userName = userNameTextBox.getText();
		final String password = passwordTextBox.getText();
		if (!ValidateBase.isValidUserName(userName)
				|| !ValidateBase.isValidPassword(password)) {
			showMessageDialog("AppVet Login Error",
					"Invalid Username or Password", true);
			return;
		}
		authenticate(userName, password);
	}

	private void showMessageDialog(String windowTitle, String message,
			boolean isError) {
		messageDialogBox = new MessageDialogBox(message, isError);
		messageDialogBox.setText(windowTitle);
		messageDialogBox.center();
		messageDialogBox.closeButton.setFocus(true);
		messageDialogBox.closeButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				userNameTextBox.setText("");
				passwordTextBox.setText("");
				messageDialogBox.hide();
				messageDialogBox = null;
				loginStatusLabel.setText("");
				loginButton.setEnabled(true);
			}

		});
	}

	public void startAppVet(final ConfigInfoGwt configInfo) {
		final String userName = configInfo.getUserInfo().getUserName();
		if ((userName == null) || userName.isEmpty()) {
			log.warning("Error retrieving apps list: "
					+ "username is null or empty");
			return;
		}

		appVetService.getAllApps(userName, new AsyncCallback<List<AppInfoGwt>>() {

			@Override
			public void onFailure(Throwable caught) {
				caught.printStackTrace();
				showMessageDialog("AppVet Error", "Apps list retrieval error",
						true);
				return;
			}

			@Override
			public void onSuccess(List<AppInfoGwt> appsList) {
				if (appsList == null) {
					showMessageDialog("AppVet Error", "No apps are available",
							true);
					return;
				} else {
					final AppVetPanel appVetPanel = new AppVetPanel(Unit.PX, configInfo,
							appsList);
					final RootLayoutPanel rootLayoutPanel = RootLayoutPanel
							.get();
					rootLayoutPanel.clear();
					rootLayoutPanel.add(appVetPanel);
				}
			}

		});
	}
}
