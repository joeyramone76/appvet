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

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * $$Id: AppUploadDialogBox.java 38562 2014-03-30 16:20:25Z steveq $$
 */
@SuppressWarnings("deprecation")
public class AppUploadDialogBox extends DialogBox {

    public PushButton cancelButton = null;
    public FormPanel uploadAppForm = null;
    public Label submitAppStatusLabel = null;
    public FileUpload fileUpload = null;
    public PushButton submitButton = null;

    public AppUploadDialogBox(String sessionId, String servletURL) {
	super(false, true);

	setSize("", "");
	setAnimationEnabled(false);

	final VerticalPanel dialogVPanel = new VerticalPanel();
	dialogVPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
	dialogVPanel.addStyleName("dialogVPanel");
	dialogVPanel
	.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
	this.setWidget(dialogVPanel);
	dialogVPanel.setSize("", "");

	final SimplePanel simplePanel = new SimplePanel();
	simplePanel.setStyleName("appUploadPanel");
	dialogVPanel.add(simplePanel);
	dialogVPanel.setCellVerticalAlignment(simplePanel,
		HasVerticalAlignment.ALIGN_MIDDLE);
	dialogVPanel.setCellHorizontalAlignment(simplePanel,
		HasHorizontalAlignment.ALIGN_CENTER);
	simplePanel.setSize("", "");
	uploadAppForm = new FormPanel();
	simplePanel.setWidget(uploadAppForm);
	uploadAppForm.setAction(servletURL);
	uploadAppForm.setMethod(FormPanel.METHOD_POST);
	uploadAppForm.setEncoding(FormPanel.ENCODING_MULTIPART);
	uploadAppForm.setSize("", "");

	final VerticalPanel verticalPanel = new VerticalPanel();
	verticalPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
	verticalPanel
	.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
	uploadAppForm.setWidget(verticalPanel);
	verticalPanel.setSize("", "");

	final Hidden hiddenCommand = new Hidden();
	hiddenCommand.setValue("SUBMIT_APP");
	hiddenCommand.setName("command");
	verticalPanel.add(hiddenCommand);
	hiddenCommand.setWidth("");

	final Hidden hiddenSessionId = new Hidden();
	hiddenSessionId.setName("sessionid");
	hiddenSessionId.setValue(sessionId);
	verticalPanel.add(hiddenSessionId);
	hiddenSessionId.setWidth("");

//	final Hidden hiddenTaRelease = new Hidden();
//	hiddenTaRelease.setValue("");
//	hiddenTaRelease.setName("tarelease");
//	verticalPanel.add(hiddenTaRelease);
//	hiddenTaRelease.setSize("", "");

	final HorizontalPanel horizontalButtonPanel = new HorizontalPanel();
	horizontalButtonPanel.setStyleName("buttonPanel");
	dialogVPanel.add(horizontalButtonPanel);
	horizontalButtonPanel
	.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
	dialogVPanel.setCellVerticalAlignment(horizontalButtonPanel,
		HasVerticalAlignment.ALIGN_MIDDLE);
	dialogVPanel.setCellHorizontalAlignment(horizontalButtonPanel,
		HasHorizontalAlignment.ALIGN_CENTER);
	horizontalButtonPanel.setSpacing(10);
	horizontalButtonPanel
	.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
	horizontalButtonPanel.setWidth("300px");
	verticalPanel.setCellWidth(horizontalButtonPanel, "100%");

	final Grid grid = new Grid(2, 2);
	grid.setStyleName("grid");
	verticalPanel.add(grid);
	verticalPanel.setCellHorizontalAlignment(grid,
		HasHorizontalAlignment.ALIGN_CENTER);
	verticalPanel.setCellVerticalAlignment(grid,
		HasVerticalAlignment.ALIGN_MIDDLE);
	grid.setSize("", "");

	final Label uploadLabel = new Label("App file: ");
	uploadLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
	grid.setWidget(0, 0, uploadLabel);
	uploadLabel.setStyleName("uploadLabel");
	uploadLabel.setSize("", "");
	fileUpload = new FileUpload();
	grid.setWidget(0, 1, fileUpload);
	grid.getCellFormatter().setWidth(0, 1, "");
	grid.getCellFormatter().setHeight(0, 1, "");
	fileUpload.setStyleName("appUpload");
	fileUpload.setTitle("Select app file to upload");
	fileUpload.setName("fileupload");
	fileUpload.setSize("240px", "");

	grid.getCellFormatter().setVerticalAlignment(0, 0,
		HasVerticalAlignment.ALIGN_MIDDLE);
	grid.getCellFormatter().setVerticalAlignment(1, 0,
		HasVerticalAlignment.ALIGN_MIDDLE);
	grid.getCellFormatter().setVerticalAlignment(0, 1,
		HasVerticalAlignment.ALIGN_MIDDLE);
	grid.getCellFormatter().setHorizontalAlignment(1, 1,
		HasHorizontalAlignment.ALIGN_LEFT);
	grid.getCellFormatter().setVerticalAlignment(1, 1,
		HasVerticalAlignment.ALIGN_MIDDLE);
	grid.getCellFormatter().setHorizontalAlignment(1, 0,
		HasHorizontalAlignment.ALIGN_LEFT);
	grid.getCellFormatter().setHorizontalAlignment(0, 1,
		HasHorizontalAlignment.ALIGN_LEFT);
	grid.getCellFormatter().setHorizontalAlignment(0, 0,
		HasHorizontalAlignment.ALIGN_CENTER);
	submitAppStatusLabel = new Label("");
	submitAppStatusLabel.setStyleName("submissionRequirementsLabel");
	submitAppStatusLabel
	.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
	verticalPanel.add(submitAppStatusLabel);
	verticalPanel.setCellHorizontalAlignment(submitAppStatusLabel,
		HasHorizontalAlignment.ALIGN_CENTER);
	verticalPanel.setCellVerticalAlignment(submitAppStatusLabel,
		HasVerticalAlignment.ALIGN_MIDDLE);
	submitAppStatusLabel.setSize("", "18px");
	cancelButton = new PushButton("Cancel");
	cancelButton.setHTML("Cancel");
	horizontalButtonPanel.add(cancelButton);
	horizontalButtonPanel.setCellHorizontalAlignment(cancelButton,
		HasHorizontalAlignment.ALIGN_CENTER);
	cancelButton.setSize("70px", "18px");
	submitButton = new PushButton("Submit");
	horizontalButtonPanel.add(submitButton);
	horizontalButtonPanel.setCellHorizontalAlignment(submitButton,
		HasHorizontalAlignment.ALIGN_CENTER);
	submitButton.setSize("70px", "18px");
	submitButton.addClickHandler(new ClickHandler() {

	    @Override
	    public void onClick(ClickEvent event) {
//		if (!taReleaseTextBox.getText().isEmpty()) {
//		    hiddenTaRelease.setValue(taReleaseTextBox.getText());
//		}
		uploadAppForm.submit();
	    }

	});
    }
}
