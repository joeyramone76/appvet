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
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class AboutDialogBox extends DialogBox {

    public final PushButton closeButton;
    public Label messageLabel = null;

    public AboutDialogBox(String version) {
	super(false, true);

	setSize("400px", "209px");
	setAnimationEnabled(false);

	final VerticalPanel dialogVPanel = new VerticalPanel();
	dialogVPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
	dialogVPanel.addStyleName("dialogVPanel");
	dialogVPanel
	.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

	final VerticalPanel verticalPanel = new VerticalPanel();
	verticalPanel
	.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
	verticalPanel.setStyleName("insetPanel");
	dialogVPanel.add(verticalPanel);
	dialogVPanel.setCellVerticalAlignment(verticalPanel,
		HasVerticalAlignment.ALIGN_MIDDLE);
	dialogVPanel.setCellHorizontalAlignment(verticalPanel,
		HasHorizontalAlignment.ALIGN_CENTER);
	dialogVPanel.setCellWidth(verticalPanel, "100%");
	verticalPanel.setWidth("350px");

	final Image image = new Image("images/appvet_logo.png");
	verticalPanel.add(image);
	verticalPanel.setCellHorizontalAlignment(image,
		HasHorizontalAlignment.ALIGN_CENTER);
	image.setSize("192px", "73px");

	final String message = "Version " + version;

	final HorizontalPanel horizontalPanel = new HorizontalPanel();
	verticalPanel.add(horizontalPanel);
	verticalPanel.setCellWidth(horizontalPanel, "100%");
	horizontalPanel.setWidth("350px");
	messageLabel = new Label(message);
	horizontalPanel.add(messageLabel);
	horizontalPanel.setCellHorizontalAlignment(messageLabel,
		HasHorizontalAlignment.ALIGN_CENTER);
	horizontalPanel.setCellVerticalAlignment(messageLabel,
		HasVerticalAlignment.ALIGN_MIDDLE);
	messageLabel.setStyleName("infoDialogBox");
	verticalPanel.setCellVerticalAlignment(messageLabel,
		HasVerticalAlignment.ALIGN_MIDDLE);
	verticalPanel.setCellHorizontalAlignment(messageLabel,
		HasHorizontalAlignment.ALIGN_CENTER);
	messageLabel
	.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
	dialogVPanel.setCellHorizontalAlignment(messageLabel,
		HasHorizontalAlignment.ALIGN_CENTER);
	dialogVPanel.setCellVerticalAlignment(messageLabel,
		HasVerticalAlignment.ALIGN_MIDDLE);
	messageLabel.setSize("350px", "18px");

	final DockPanel dockPanel = new DockPanel();
	dockPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
	dockPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
	dockPanel.add(dialogVPanel, DockPanel.CENTER);
	dockPanel.setCellVerticalAlignment(dialogVPanel,
		HasVerticalAlignment.ALIGN_MIDDLE);
	dockPanel.setCellHorizontalAlignment(dialogVPanel,
		HasHorizontalAlignment.ALIGN_CENTER);

	this.setWidget(dockPanel);
	dockPanel.setSize("372px", "181px");
	dialogVPanel.setSize("360px", "125px");
	
		final SimplePanel simplePanel = new SimplePanel();
		dockPanel.add(simplePanel, DockPanel.SOUTH);
		dockPanel.setCellVerticalAlignment(simplePanel, HasVerticalAlignment.ALIGN_MIDDLE);
		simplePanel.setStyleName("aboutDialogButtonPanel");
		dialogVPanel.setCellWidth(simplePanel, "100%");
		simplePanel.setWidth("340px");
		closeButton = new PushButton("Close");
		simplePanel.setWidget(closeButton);
		dialogVPanel.setCellVerticalAlignment(closeButton,
			HasVerticalAlignment.ALIGN_MIDDLE);
		closeButton.setSize("70px", "18px");
		dialogVPanel.setCellHorizontalAlignment(closeButton,
			HasHorizontalAlignment.ALIGN_CENTER);
    }
}
