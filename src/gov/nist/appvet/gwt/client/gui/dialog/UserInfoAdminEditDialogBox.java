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
package gov.nist.appvet.gwt.client.gui.dialog;

import gov.nist.appvet.gwt.client.gui.table.appslist.UsersListPagingDataGrid;
import gov.nist.appvet.gwt.shared.UserInfoGwt;

import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.SimpleCheckBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.TextBoxBase;
import com.google.gwt.user.client.ui.ValueBoxBase.TextAlignment;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * $$Id: UserInfoAdminEditDialogBox.java 38554 2014-03-30 16:06:12Z steveq $$
 */
public class UserInfoAdminEditDialogBox extends DialogBox {

    public PushButton cancelButton = null;
    public PushButton okButton = null;
    public TextBox lastNameTextBox = null;
    public TextBox firstNameTextBox = null;
    public TextBox userIdTextBox = null;
    public PasswordTextBox password1TextBox = null;
    public PasswordTextBox password2TextBox = null;
    public TextBox organizationTextBox = null;
    public TextBox emailTextBox = null;
    public ListBox roleComboBox = null;
    public List<UserInfoGwt> allUsers = null;
    public boolean newUser = false;
    public UsersListPagingDataGrid<UserInfoGwt> usersListTable = null;
    public SimpleCheckBox changePasswordCheckBox = null;
    public Label passwordLabel = null;
    public Label passwordAgainLabel = null;

    @SuppressWarnings("deprecation")
    public UserInfoAdminEditDialogBox(UserInfoGwt userInfo,
	    UsersListPagingDataGrid<UserInfoGwt> usersListTable,
	    List<UserInfoGwt> allUsers) {
	setWidth("386px");

	this.usersListTable = usersListTable;
	this.allUsers = allUsers;
	if (userInfo == null) {
	    newUser = true;
	}
	changePasswordCheckBox = new SimpleCheckBox();
	if (newUser) {
	    passwordLabel = new Label("Password: ");
	} else {
	    passwordLabel = new Label("Password Reset: ");
	}
	passwordLabel
	.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
	if (newUser) {
	    passwordAgainLabel = new Label("Password (again): ");
	} else {
	    passwordAgainLabel = new Label("Password Reset (again): ");
	}
	passwordAgainLabel
	.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);

	final VerticalPanel verticalPanel_1 = new VerticalPanel();
	verticalPanel_1.setSize("100%", "100%");
	verticalPanel_1.setSpacing(5);
	verticalPanel_1.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
	verticalPanel_1
	.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

	final HorizontalPanel horizontalPanel_1 = new HorizontalPanel();
	verticalPanel_1.add(horizontalPanel_1);
	horizontalPanel_1
	.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
	horizontalPanel_1
	.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);

	final Label lblNewLabel = new Label("Last Name: ");
	horizontalPanel_1.add(lblNewLabel);
	horizontalPanel_1.setCellHorizontalAlignment(lblNewLabel,
		HasHorizontalAlignment.ALIGN_CENTER);
	horizontalPanel_1.setCellVerticalAlignment(lblNewLabel,
		HasVerticalAlignment.ALIGN_MIDDLE);
	lblNewLabel.setWidth("170px");
	horizontalPanel_1.setCellWidth(lblNewLabel, "50%");
	lblNewLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);

	lastNameTextBox = new TextBox();
	lastNameTextBox.setTextAlignment(TextBoxBase.ALIGN_LEFT);
	lastNameTextBox.setAlignment(TextAlignment.LEFT);
	horizontalPanel_1.add(lastNameTextBox);
	horizontalPanel_1.setCellHorizontalAlignment(lastNameTextBox,
		HasHorizontalAlignment.ALIGN_CENTER);
	horizontalPanel_1.setCellVerticalAlignment(lastNameTextBox,
		HasVerticalAlignment.ALIGN_MIDDLE);
	horizontalPanel_1.setCellWidth(lastNameTextBox, "50%");
	lastNameTextBox.setWidth("180px");

	final HorizontalPanel horizontalPanel_2 = new HorizontalPanel();
	verticalPanel_1.add(horizontalPanel_2);
	horizontalPanel_2
	.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
	horizontalPanel_2
	.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);

	final Label lblNewLabel_1 = new Label("First Name: ");
	horizontalPanel_2.add(lblNewLabel_1);
	lblNewLabel_1.setWidth("170px");
	horizontalPanel_2.setCellWidth(lblNewLabel_1, "50%");
	horizontalPanel_2.setCellVerticalAlignment(lblNewLabel_1,
		HasVerticalAlignment.ALIGN_MIDDLE);
	horizontalPanel_2.setCellHorizontalAlignment(lblNewLabel_1,
		HasHorizontalAlignment.ALIGN_CENTER);
	lblNewLabel_1.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);

	firstNameTextBox = new TextBox();
	firstNameTextBox.setTextAlignment(TextBoxBase.ALIGN_LEFT);
	firstNameTextBox.setAlignment(TextAlignment.LEFT);
	horizontalPanel_2.add(firstNameTextBox);
	horizontalPanel_2.setCellWidth(firstNameTextBox, "50%");
	horizontalPanel_2.setCellVerticalAlignment(firstNameTextBox,
		HasVerticalAlignment.ALIGN_MIDDLE);
	horizontalPanel_2.setCellHorizontalAlignment(firstNameTextBox,
		HasHorizontalAlignment.ALIGN_CENTER);
	firstNameTextBox.setWidth("180px");

	final HorizontalPanel horizontalPanel_3 = new HorizontalPanel();
	verticalPanel_1.add(horizontalPanel_3);
	horizontalPanel_3
	.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
	horizontalPanel_3
	.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);

	final Label lblUserId = new Label("User ID:");
	horizontalPanel_3.add(lblUserId);
	lblUserId.setWidth("170px");
	horizontalPanel_3.setCellVerticalAlignment(lblUserId,
		HasVerticalAlignment.ALIGN_MIDDLE);
	horizontalPanel_3.setCellHorizontalAlignment(lblUserId,
		HasHorizontalAlignment.ALIGN_CENTER);
	horizontalPanel_3.setCellWidth(lblUserId, "50%");
	lblUserId.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);

	userIdTextBox = new TextBox();
	userIdTextBox.setTextAlignment(TextBoxBase.ALIGN_LEFT);
	userIdTextBox.setAlignment(TextAlignment.LEFT);
	horizontalPanel_3.add(userIdTextBox);
	horizontalPanel_3.setCellWidth(userIdTextBox, "50%");
	horizontalPanel_3.setCellVerticalAlignment(userIdTextBox,
		HasVerticalAlignment.ALIGN_MIDDLE);
	horizontalPanel_3.setCellHorizontalAlignment(userIdTextBox,
		HasHorizontalAlignment.ALIGN_CENTER);
	userIdTextBox.setWidth("180px");

	final HorizontalPanel horizontalPanel_6 = new HorizontalPanel();
	verticalPanel_1.add(horizontalPanel_6);
	horizontalPanel_6
	.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
	horizontalPanel_6
	.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);

	final Label lblOrganization = new Label("Organization: ");
	horizontalPanel_6.add(lblOrganization);
	lblOrganization.setWidth("170px");
	horizontalPanel_6.setCellVerticalAlignment(lblOrganization,
		HasVerticalAlignment.ALIGN_MIDDLE);
	horizontalPanel_6.setCellHorizontalAlignment(lblOrganization,
		HasHorizontalAlignment.ALIGN_CENTER);
	horizontalPanel_6.setCellWidth(lblOrganization, "50%");
	lblOrganization
	.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);

	organizationTextBox = new TextBox();
	organizationTextBox.setTextAlignment(TextBoxBase.ALIGN_LEFT);
	organizationTextBox.setAlignment(TextAlignment.LEFT);
	horizontalPanel_6.add(organizationTextBox);
	horizontalPanel_6.setCellVerticalAlignment(organizationTextBox,
		HasVerticalAlignment.ALIGN_MIDDLE);
	horizontalPanel_6.setCellHorizontalAlignment(organizationTextBox,
		HasHorizontalAlignment.ALIGN_CENTER);
	horizontalPanel_6.setCellWidth(organizationTextBox, "50%");
	organizationTextBox.setWidth("180px");

	final HorizontalPanel horizontalPanel_7 = new HorizontalPanel();
	verticalPanel_1.add(horizontalPanel_7);
	horizontalPanel_7
	.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
	horizontalPanel_7
	.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);

	final Label lblEmail = new Label("Email: ");
	horizontalPanel_7.add(lblEmail);
	lblEmail.setWidth("170px");
	horizontalPanel_7.setCellVerticalAlignment(lblEmail,
		HasVerticalAlignment.ALIGN_MIDDLE);
	horizontalPanel_7.setCellHorizontalAlignment(lblEmail,
		HasHorizontalAlignment.ALIGN_CENTER);
	horizontalPanel_7.setCellWidth(lblEmail, "50%");
	lblEmail.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
	emailTextBox = new TextBox();
	emailTextBox.setTextAlignment(TextBoxBase.ALIGN_LEFT);
	emailTextBox.setAlignment(TextAlignment.LEFT);
	horizontalPanel_7.add(emailTextBox);
	horizontalPanel_7.setCellVerticalAlignment(emailTextBox,
		HasVerticalAlignment.ALIGN_MIDDLE);
	horizontalPanel_7.setCellHorizontalAlignment(emailTextBox,
		HasHorizontalAlignment.ALIGN_CENTER);
	horizontalPanel_7.setCellWidth(emailTextBox, "50%");
	emailTextBox.setWidth("180px");

	final HorizontalPanel horizontalPanel_8 = new HorizontalPanel();
	verticalPanel_1.add(horizontalPanel_8);
	horizontalPanel_8
	.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
	horizontalPanel_8
	.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);

	final Label lblRole = new Label("Role: ");
	horizontalPanel_8.add(lblRole);
	horizontalPanel_8.setCellHorizontalAlignment(lblRole,
		HasHorizontalAlignment.ALIGN_CENTER);
	lblRole.setWidth("170px");
	horizontalPanel_8.setCellVerticalAlignment(lblRole,
		HasVerticalAlignment.ALIGN_MIDDLE);
	horizontalPanel_8.setCellWidth(lblRole, "50%");
	lblRole.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);

	roleComboBox = new ListBox();
	horizontalPanel_8.add(roleComboBox);
	horizontalPanel_8.setCellWidth(roleComboBox, "50%");
	horizontalPanel_8.setCellVerticalAlignment(roleComboBox,
		HasVerticalAlignment.ALIGN_MIDDLE);
	horizontalPanel_8.setCellHorizontalAlignment(roleComboBox,
		HasHorizontalAlignment.ALIGN_CENTER);
	roleComboBox.addItem("DEV");
	roleComboBox.addItem("ANALYST");
	roleComboBox.addItem("ADMIN");
	roleComboBox.addItem("APPSTORE");
	roleComboBox.addItem("TOOL_SERVICE_PROVIDER");
	roleComboBox.addItem("OTHER_CLIENT");

	roleComboBox.setWidth("190px");

	final HorizontalPanel horizontalPanel_13 = new HorizontalPanel();
	verticalPanel_1.add(horizontalPanel_13);
	horizontalPanel_13
	.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
	horizontalPanel_13
	.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
	horizontalPanel_13.setWidth("366px");

	changePasswordCheckBox.addClickHandler(new ClickHandler() {

	    @Override
	    public void onClick(ClickEvent event) {
		if (changePasswordCheckBox.isChecked()) {
		    password1TextBox.setEnabled(true);
		    password2TextBox.setEnabled(true);
		} else {
		    password1TextBox.setText(null);
		    password1TextBox.setEnabled(false);
		    password2TextBox.setText(null);
		    password2TextBox.setEnabled(false);
		}
	    }

	});
	horizontalPanel_13.add(changePasswordCheckBox);
	horizontalPanel_13.setCellVerticalAlignment(changePasswordCheckBox,
		HasVerticalAlignment.ALIGN_MIDDLE);

	final Label lblNewLabel_2 = new Label("Change Password");
	lblNewLabel_2.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
	horizontalPanel_13.add(lblNewLabel_2);
	horizontalPanel_13.setCellVerticalAlignment(lblNewLabel_2,
		HasVerticalAlignment.ALIGN_MIDDLE);
	lblNewLabel_2.setWidth("340px");

	final HorizontalPanel horizontalPanel_4 = new HorizontalPanel();
	verticalPanel_1.add(horizontalPanel_4);
	horizontalPanel_4
	.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
	horizontalPanel_4
	.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);

	horizontalPanel_4.add(passwordLabel);
	passwordLabel.setWidth("170px");
	horizontalPanel_4.setCellWidth(passwordLabel, "50%");
	horizontalPanel_4.setCellVerticalAlignment(passwordLabel,
		HasVerticalAlignment.ALIGN_MIDDLE);
	horizontalPanel_4.setCellHorizontalAlignment(passwordLabel,
		HasHorizontalAlignment.ALIGN_CENTER);
	passwordLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);

	password1TextBox = new PasswordTextBox();
	password1TextBox.setEnabled(false);
	password1TextBox.setTextAlignment(TextBoxBase.ALIGN_LEFT);
	password1TextBox.setAlignment(TextAlignment.LEFT);
	horizontalPanel_4.add(password1TextBox);
	horizontalPanel_4.setCellVerticalAlignment(password1TextBox,
		HasVerticalAlignment.ALIGN_MIDDLE);
	horizontalPanel_4.setCellHorizontalAlignment(password1TextBox,
		HasHorizontalAlignment.ALIGN_CENTER);
	horizontalPanel_4.setCellWidth(password1TextBox, "50%");
	password1TextBox.setSize("180px", "");

	final HorizontalPanel horizontalPanel_5 = new HorizontalPanel();
	verticalPanel_1.add(horizontalPanel_5);
	horizontalPanel_5
	.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
	horizontalPanel_5
	.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);

	horizontalPanel_5.add(passwordAgainLabel);
	passwordAgainLabel.setWidth("170px");
	horizontalPanel_5.setCellVerticalAlignment(passwordAgainLabel,
		HasVerticalAlignment.ALIGN_MIDDLE);
	horizontalPanel_5.setCellHorizontalAlignment(passwordAgainLabel,
		HasHorizontalAlignment.ALIGN_CENTER);
	horizontalPanel_5.setCellWidth(passwordAgainLabel, "50%");
	passwordAgainLabel
	.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
	password2TextBox = new PasswordTextBox();
	password2TextBox.setEnabled(false);
	password2TextBox.setTextAlignment(TextBoxBase.ALIGN_LEFT);
	password2TextBox.setAlignment(TextAlignment.LEFT);
	horizontalPanel_5.add(password2TextBox);
	horizontalPanel_5.setCellVerticalAlignment(password2TextBox,
		HasVerticalAlignment.ALIGN_MIDDLE);
	horizontalPanel_5.setCellHorizontalAlignment(password2TextBox,
		HasHorizontalAlignment.ALIGN_CENTER);
	horizontalPanel_5.setCellWidth(password2TextBox, "50%");
	password2TextBox.setSize("180px", "");

	final HorizontalPanel horizontalPanel_9 = new HorizontalPanel();
	verticalPanel_1.add(horizontalPanel_9);
	horizontalPanel_9
	.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
	horizontalPanel_9
	.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);

	final HorizontalPanel horizontalPanel_10 = new HorizontalPanel();
	verticalPanel_1.add(horizontalPanel_10);
	horizontalPanel_10
	.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
	horizontalPanel_10
	.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);

	final Label lblLastLogon = new Label("Last Logon: ");
	horizontalPanel_10.add(lblLastLogon);
	lblLastLogon.setWidth("170px");
	horizontalPanel_10.setCellVerticalAlignment(lblLastLogon,
		HasVerticalAlignment.ALIGN_MIDDLE);
	horizontalPanel_10.setCellHorizontalAlignment(lblLastLogon,
		HasHorizontalAlignment.ALIGN_CENTER);
	horizontalPanel_10.setCellWidth(lblLastLogon, "50%");
	lblLastLogon.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);

	final TextBox lastLogonTextBox = new TextBox();
	lastLogonTextBox.setEnabled(false);
	lastLogonTextBox.setTextAlignment(TextBoxBase.ALIGN_LEFT);
	lastLogonTextBox.setAlignment(TextAlignment.LEFT);
	horizontalPanel_10.add(lastLogonTextBox);
	horizontalPanel_10.setCellVerticalAlignment(lastLogonTextBox,
		HasVerticalAlignment.ALIGN_MIDDLE);
	horizontalPanel_10.setCellHorizontalAlignment(lastLogonTextBox,
		HasHorizontalAlignment.ALIGN_CENTER);
	horizontalPanel_10.setCellWidth(lastLogonTextBox, "50%");
	lastLogonTextBox.setReadOnly(true);
	lastLogonTextBox.setWidth("180px");

	final HorizontalPanel horizontalPanel_11 = new HorizontalPanel();
	verticalPanel_1.add(horizontalPanel_11);
	horizontalPanel_11
	.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
	horizontalPanel_11
	.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);

	final Label lblFromHost = new Label("From Host: ");
	horizontalPanel_11.add(lblFromHost);
	lblFromHost.setWidth("170px");
	horizontalPanel_11.setCellWidth(lblFromHost, "50%");
	horizontalPanel_11.setCellVerticalAlignment(lblFromHost,
		HasVerticalAlignment.ALIGN_MIDDLE);
	horizontalPanel_11.setCellHorizontalAlignment(lblFromHost,
		HasHorizontalAlignment.ALIGN_CENTER);
	lblFromHost.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);

	final TextBox fromHostTextBox = new TextBox();
	fromHostTextBox.setEnabled(false);
	fromHostTextBox.setTextAlignment(TextBoxBase.ALIGN_LEFT);
	fromHostTextBox.setAlignment(TextAlignment.LEFT);
	horizontalPanel_11.add(fromHostTextBox);
	horizontalPanel_11.setCellVerticalAlignment(fromHostTextBox,
		HasVerticalAlignment.ALIGN_MIDDLE);
	horizontalPanel_11.setCellHorizontalAlignment(fromHostTextBox,
		HasHorizontalAlignment.ALIGN_CENTER);
	horizontalPanel_11.setCellWidth(fromHostTextBox, "50%");
	fromHostTextBox.setReadOnly(true);
	fromHostTextBox.setWidth("180px");

	final HorizontalPanel horizontalPanel_12 = new HorizontalPanel();
	verticalPanel_1.add(horizontalPanel_12);
	horizontalPanel_12
	.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
	horizontalPanel_12
	.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);

	final HorizontalPanel horizontalPanel = new HorizontalPanel();

	horizontalPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
	horizontalPanel
	.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
	horizontalPanel.setSize("200px", "50px");
	horizontalPanel.setStyleName("buttonPanelStyle");
	cancelButton = new PushButton("Cancel");
	cancelButton.setHTML("Cancel");
	horizontalPanel.add(cancelButton);

	final Label buttonSpacerLabel = new Label("");

	horizontalPanel.add(buttonSpacerLabel);
	horizontalPanel.setCellVerticalAlignment(buttonSpacerLabel,
		HasVerticalAlignment.ALIGN_MIDDLE);
	horizontalPanel.setCellHorizontalAlignment(buttonSpacerLabel,
		HasHorizontalAlignment.ALIGN_CENTER);
	buttonSpacerLabel.setSize("60px", "18px");
	horizontalPanel.setCellHorizontalAlignment(cancelButton,
		HasHorizontalAlignment.ALIGN_CENTER);
	horizontalPanel.setCellVerticalAlignment(cancelButton,
		HasVerticalAlignment.ALIGN_MIDDLE);
	cancelButton.setSize("70px", "18px");
	okButton = new PushButton("Submit");

	horizontalPanel.add(okButton);
	horizontalPanel.setCellHorizontalAlignment(okButton,
		HasHorizontalAlignment.ALIGN_CENTER);
	horizontalPanel.setCellVerticalAlignment(okButton,
		HasVerticalAlignment.ALIGN_MIDDLE);
	okButton.setSize("70px", "18px");
	if (!newUser) {
	    lastNameTextBox.setText(userInfo.getLastName());
	    firstNameTextBox.setText(userInfo.getFirstName());
	    userIdTextBox.setText(userInfo.getUserName());
	    userIdTextBox.setReadOnly(true);
	    lastLogonTextBox.setText(userInfo.getLastLogon());
	    fromHostTextBox.setText(userInfo.getFromHost());
	    organizationTextBox.setText(userInfo.getOrganization());
	    emailTextBox.setText(userInfo.getEmail());
	    if (userInfo.getRole().equals("DEV")) {
		roleComboBox.setSelectedIndex(0);
	    } else if (userInfo.getRole().equals("ANALYST")) {
		roleComboBox.setSelectedIndex(1);
	    } else if (userInfo.getRole().equals("ADMIN")) {
		roleComboBox.setSelectedIndex(2);
	    } else if (userInfo.getRole().equals("APPSTORE")) {
		roleComboBox.setSelectedIndex(3);
	    } else if (userInfo.getRole().equals("TOOL_SERVICE_PROVIDER")) {
		roleComboBox.setSelectedIndex(4);
	    } else if (userInfo.getRole().equals("OTHER_CLIENT")) {
		roleComboBox.setSelectedIndex(5);
	    }
	}

	final SimplePanel simplePanel = new SimplePanel();
	simplePanel.setStyleName("userFormPanel");
	simplePanel.setWidget(verticalPanel_1);

	final DockPanel dockPanel = new DockPanel();
	dockPanel.setStyleName("gwt-DialogBox");
	setWidget(dockPanel);
	dockPanel.setSize("386px", "");
	dockPanel.add(horizontalPanel, DockPanel.SOUTH);
	dockPanel.setCellVerticalAlignment(horizontalPanel,
		HasVerticalAlignment.ALIGN_MIDDLE);
	dockPanel.setCellHorizontalAlignment(horizontalPanel,
		HasHorizontalAlignment.ALIGN_CENTER);
	dockPanel.add(simplePanel, DockPanel.CENTER);
    }
}
