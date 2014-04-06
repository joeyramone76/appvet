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

import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;


public class MessageDialogBox extends DialogBox {

    public PushButton closeButton;
    public Label messageLabel = null;

    public MessageDialogBox(String message, boolean isErrorMessage) {
	super(false, true);

	messageLabel = new Label(message);
	if (isErrorMessage) {
	    messageLabel.setStyleName("errorDialogBox");
	} else {
	    messageLabel.setStyleName("infoDialogBox");
	}
	setWidth("");
	setAnimationEnabled(false);

	final VerticalPanel dialogVPanel = new VerticalPanel();
	dialogVPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
	dialogVPanel.addStyleName("dialogVPanel");
	dialogVPanel
	.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

	final VerticalPanel verticalPanel = new VerticalPanel();
	verticalPanel.setStyleName("messagePanel");
	verticalPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
	verticalPanel
	.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
	dialogVPanel.add(verticalPanel);
	verticalPanel.setSize("364px", "");
	dialogVPanel.setCellVerticalAlignment(verticalPanel,
		HasVerticalAlignment.ALIGN_MIDDLE);
	dialogVPanel.setCellHorizontalAlignment(verticalPanel,
		HasHorizontalAlignment.ALIGN_CENTER);
	dialogVPanel.setCellWidth(verticalPanel, "100%");

	final SimplePanel emptyPanel = new SimplePanel();
	verticalPanel.add(emptyPanel);
	verticalPanel.setCellVerticalAlignment(emptyPanel,
		HasVerticalAlignment.ALIGN_MIDDLE);
	verticalPanel.setCellWidth(emptyPanel, "100%");
	emptyPanel.setHeight("40px");

	final SimplePanel simplePanel = new SimplePanel();
	verticalPanel.add(simplePanel);
	verticalPanel.setCellHorizontalAlignment(simplePanel,
		HasHorizontalAlignment.ALIGN_CENTER);
	verticalPanel.setCellWidth(simplePanel, "100%");
	dialogVPanel.setCellWidth(simplePanel, "100%");
	dialogVPanel.setCellVerticalAlignment(simplePanel,
		HasVerticalAlignment.ALIGN_MIDDLE);
	dialogVPanel.setCellHorizontalAlignment(simplePanel,
		HasHorizontalAlignment.ALIGN_CENTER);
	simplePanel.setSize("300px", "60px");

	simplePanel.setWidget(messageLabel);
	messageLabel
	.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
	dialogVPanel.setCellHorizontalAlignment(messageLabel,
		HasHorizontalAlignment.ALIGN_CENTER);
	messageLabel.setSize("", "");

	final SimplePanel emptyPanel2 = new SimplePanel();
	verticalPanel.add(emptyPanel2);
	verticalPanel.setCellVerticalAlignment(emptyPanel2,
		HasVerticalAlignment.ALIGN_MIDDLE);
	verticalPanel.setCellHorizontalAlignment(emptyPanel2,
		HasHorizontalAlignment.ALIGN_CENTER);
	verticalPanel.setCellWidth(emptyPanel2, "100%");
	emptyPanel2.setHeight("40px");
	this.setWidget(dialogVPanel);
	dialogVPanel.setSize("", "");

	final HorizontalPanel horizontalPanel = new HorizontalPanel();
	horizontalPanel.setStyleName("buttonPanel");
	horizontalPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
	horizontalPanel
	.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
	dialogVPanel.add(horizontalPanel);
	dialogVPanel.setCellHorizontalAlignment(horizontalPanel,
		HasHorizontalAlignment.ALIGN_CENTER);
	horizontalPanel.setWidth("320px");
	closeButton = new PushButton("Close");
	horizontalPanel.add(closeButton);
	dialogVPanel.setCellWidth(closeButton, "100%");

	closeButton.setSize("70px", "18px");
	dialogVPanel.setCellVerticalAlignment(closeButton,
		HasVerticalAlignment.ALIGN_MIDDLE);
	dialogVPanel.setCellHorizontalAlignment(closeButton,
		HasHorizontalAlignment.ALIGN_CENTER);
    }
}
