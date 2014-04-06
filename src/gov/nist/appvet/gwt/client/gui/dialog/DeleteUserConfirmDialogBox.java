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
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class DeleteUserConfirmDialogBox extends DialogBox {

    public PushButton okButton = null;
    public PushButton cancelButton = null;

    public DeleteUserConfirmDialogBox(String userName) {
	super(false, true);

	setSize("", "");
	setAnimationEnabled(false);

	final VerticalPanel verticalPanel = new VerticalPanel();
	verticalPanel.addStyleName("dialogVPanel");
	verticalPanel
	.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
	this.setWidget(verticalPanel);
	verticalPanel.setSize("", "");

	final VerticalPanel verticalPanel_1 = new VerticalPanel();
	verticalPanel_1.setStyleName("messagePanel");
	verticalPanel.add(verticalPanel_1);
	verticalPanel_1.setWidth("320px");
	verticalPanel.setCellWidth(verticalPanel_1, "100%");

	final SimplePanel simplePanel = new SimplePanel();
	verticalPanel_1.add(simplePanel);
	verticalPanel_1.setCellWidth(simplePanel, "100%");
	simplePanel.setHeight("40px");

	final SimplePanel simplePanel_1 = new SimplePanel();
	verticalPanel_1.add(simplePanel_1);
	verticalPanel_1.setCellWidth(simplePanel_1, "100%");
	verticalPanel_1.setCellVerticalAlignment(simplePanel_1,
		HasVerticalAlignment.ALIGN_MIDDLE);
	verticalPanel_1.setCellHorizontalAlignment(simplePanel_1,
		HasHorizontalAlignment.ALIGN_CENTER);
	simplePanel_1.setSize("300px", "60px");

	final HTML htmlNewHtml = new HTML(
		"<p align=\"center\">\r\nAre you sure you want to delete user"
			+ " '" + userName + "'?\r\n</p>", true);
	htmlNewHtml.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
	simplePanel_1.setWidget(htmlNewHtml);
	htmlNewHtml.setSize("", "");
	htmlNewHtml.setStyleName("errorDialogBox");

	final SimplePanel simplePanel_2 = new SimplePanel();
	verticalPanel_1.add(simplePanel_2);
	verticalPanel_1.setCellWidth(simplePanel_2, "100%");
	simplePanel_2.setHeight("40px");

	final HorizontalPanel horizontalButtonPanel = new HorizontalPanel();
	horizontalButtonPanel.setStyleName("buttonPanel");
	horizontalButtonPanel
	.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
	horizontalButtonPanel.setSpacing(5);
	horizontalButtonPanel
	.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
	verticalPanel.add(horizontalButtonPanel);
	verticalPanel.setCellVerticalAlignment(horizontalButtonPanel,
		HasVerticalAlignment.ALIGN_MIDDLE);
	verticalPanel.setCellHorizontalAlignment(horizontalButtonPanel,
		HasHorizontalAlignment.ALIGN_CENTER);
	horizontalButtonPanel.setWidth("320px");
	verticalPanel.setCellWidth(horizontalButtonPanel, "100%");
	cancelButton = new PushButton("No");
	cancelButton.setHTML("Cancel");
	horizontalButtonPanel.add(cancelButton);
	cancelButton.setSize("70px", "18px");
	horizontalButtonPanel.setCellVerticalAlignment(cancelButton,
		HasVerticalAlignment.ALIGN_MIDDLE);
	horizontalButtonPanel.setCellHorizontalAlignment(cancelButton,
		HasHorizontalAlignment.ALIGN_CENTER);
	okButton = new PushButton("Yes");
	okButton.setHTML("Ok");
	horizontalButtonPanel.add(okButton);
	horizontalButtonPanel.setCellHorizontalAlignment(okButton,
		HasHorizontalAlignment.ALIGN_CENTER);
	horizontalButtonPanel.setCellVerticalAlignment(okButton,
		HasVerticalAlignment.ALIGN_MIDDLE);
	okButton.setSize("70px", "18px");
    }
}
