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
import gov.nist.appvet.gwt.client.gui.dialog.AboutDialogBox;
import gov.nist.appvet.gwt.client.gui.dialog.AppUploadDialogBox;
import gov.nist.appvet.gwt.client.gui.dialog.DeleteAppConfirmDialogBox;
import gov.nist.appvet.gwt.client.gui.dialog.MessageDialogBox;
import gov.nist.appvet.gwt.client.gui.dialog.ReportUploadDialogBox;
import gov.nist.appvet.gwt.client.gui.dialog.UserAcctDialogBox;
import gov.nist.appvet.gwt.client.gui.dialog.UsersDialogBox;
import gov.nist.appvet.gwt.client.gui.table.appslist.AppsListPagingDataGrid;
import gov.nist.appvet.gwt.shared.AppInfoGwt;
import gov.nist.appvet.gwt.shared.ConfigInfoGwt;
import gov.nist.appvet.gwt.shared.ToolStatusGwt;
import gov.nist.appvet.gwt.shared.UserInfoGwt;
import gov.nist.appvet.shared.analysis.AnalysisType;
import gov.nist.appvet.shared.appvetparameters.AppVetParameter;
import gov.nist.appvet.shared.os.DeviceOS;
import gov.nist.appvet.shared.servletcommands.AppVetServletCommand;
import gov.nist.appvet.shared.status.AppStatus;
import gov.nist.appvet.shared.validate.ValidateBase;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.FormHandler;
import com.google.gwt.user.client.ui.FormSubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormSubmitEvent;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.HorizontalSplitPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.MenuItemSeparator;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;

@SuppressWarnings("deprecation")
public class AppVetPanel extends DockLayoutPanel {

	private static Logger log = Logger.getLogger("AppVetPanel"); // See appvet.gwt.xml
	private SingleSelectionModel<AppInfoGwt> appSelectionModel = null;
	private static long MAX_SESSION_IDLE_DURATION = 0;
	private static int POLLING_INTERVAL = 0;
	private final static GWTServiceAsync appVetServiceAsync = GWT
			.create(GWTService.class);
	private HTML appInfoName = null;
	private HTML appInfoVersion = null;
	private Image appInfoIcon = null;
	private HTML toolResultsHtml = null;
	private AppsListPagingDataGrid<AppInfoGwt> appsListTable = null;
	private long lastAppsListUpdate = -1;
	private UserInfoGwt userInfo = null;
	private String userName = null;
	private PushButton deleteButton = null;
	private PushButton downloadButton = null;
	private PushButton addReportButton = null;
	private PushButton logButton = null;
	private List<AppInfoGwt> allApps = null;
	private TextBox searchTextBox = null;
	private String sessionId = null;
	private long sessionExpirationLong = 0;
	private Timer pollingTimer = null;
	private HorizontalPanel appsListButtonPanel = null;
	private SimplePanel rightCenterPanel = null;
	private static AppUploadDialogBox appUploadDialogBox = null;
	private static MessageDialogBox errorDialogBox = null;
	private static MessageDialogBox messageDialogBox = null;
	private static AboutDialogBox aboutDialogBox = null;
	private static UsersDialogBox usersDialogBox = null;
	private static DeleteAppConfirmDialogBox deleteConfirmDialogBox = null;
	private static ReportUploadDialogBox reportUploadDialogBox = null;
	private UserAcctDialogBox userInfoEditDialogBox = null;
	public final Label statusMessageLabel = new Label("");
	private String SERVLET_URL = null;
	private String HOST_URL = null;
	private String[] androidToolNames = null;
	private String[] androidToolIDs = null;
	private String[] androidToolTypes = null;
	private String[] iosToolNames = null;
	private String[] iosToolIDs = null;
	private String[] iosToolTypes = null;
	
	private InlineLabel appsLabel = null;
	private int iconVersion = 0;
	private static double NORTH_PANEL_HEIGHT = 130.0;
	private static double SOUTH_PANEL_HEIGHT = 47.0;
	private static boolean searchMode = false;
	private MenuItem accountMenuItem = null;
	private boolean newBrowserEvent = true;

	class AppListHandler implements SelectionChangeEvent.Handler {

		ConfigInfoGwt configInfo = null;
		AppVetPanel appVetPanel = null;

		public AppListHandler(AppVetPanel appVetPanel, ConfigInfoGwt configInfo) {
			this.appVetPanel = appVetPanel;
			this.configInfo = configInfo;
		}

		@Override
		public void onSelectionChange(SelectionChangeEvent event) {
			final AppInfoGwt selectedApp = appSelectionModel.getSelectedObject();
			if (selectedApp != null) {
				appVetServiceAsync.getToolsResults(selectedApp.os, sessionId, selectedApp.appId,
						new AsyncCallback<List<ToolStatusGwt>>() {

					@Override
					public void onFailure(Throwable caught) {
						showMessageDialog("AppVet Error",
								"System error retrieving app info",
								true);
					}

					@Override
					public void onSuccess(List<ToolStatusGwt> toolsResults) {
						if ((toolsResults == null) || toolsResults.isEmpty()) {
							showMessageDialog("AppVet Error",
									"Could not retrieve app info", true);
						} else {
							String appNameHtml = null;
							
							/* Set app icon */
							appInfoIcon.setVisible(true);

							if (selectedApp.appStatus == AppStatus.REGISTERING) {
								iconVersion++;
								final String iconPath = HOST_URL
										+ "/appvet_images/default.png?v"
										+ iconVersion;
								appInfoIcon.setUrl(iconPath);
								appNameHtml = "<div id=\"endorsed\" style='color: darkslategray; size:18; weight: bold'>"
										+ "Unknown" + "</div>";
								appInfoName.setHTML(appNameHtml);
								toolResultsHtml
								.setHTML("Waiting for data...");
								appInfoVersion
								.setHTML("<b>Version: </b>N/A");
								return;
							} else if (selectedApp.appStatus == AppStatus.PENDING) {
								final String iconPath = HOST_URL
										+ "/appvet_images/default.png";
								appInfoIcon.setUrl(iconPath);
							} else if (selectedApp.appStatus == AppStatus.PROCESSING) {
								iconVersion++;
								final String iconPath = HOST_URL
										+ "/appvet_images/"
										+ selectedApp.appId + ".png?v"
										+ iconVersion;
								appInfoIcon.setUrl(iconPath);
							} else {
								final String iconPath = HOST_URL
										+ "/appvet_images/"
										+ selectedApp.appId + ".png";
								appInfoIcon.setUrl(iconPath);
							}

							/* Set app status */
							if ((selectedApp.appStatus == AppStatus.ERROR)
									|| (selectedApp.appStatus == AppStatus.FAIL)
									|| (selectedApp.appStatus == AppStatus.WARNING)
									|| (selectedApp.appStatus == AppStatus.PASS)) {
								appNameHtml = "<div id=\"endorsed\" style='color: darkslategray; size:18; weight: bold'>"
										+ selectedApp.appName + "</div>";
								addReportButton.setEnabled(true);
								deleteButton.setEnabled(true);
								downloadButton.setEnabled(true);
							} else {
								appNameHtml = "<div id=\"endorsed\" style='color: dimgray; size:18; weight: bold'>"
										+ selectedApp.appName + "</div>";
								//addReportButton.setEnabled(true);
								//deleteButton.setEnabled(false);
								downloadButton.setEnabled(false);
							}
							appInfoName.setHTML(appNameHtml);

							if ((selectedApp.versionName == null)
									|| selectedApp.versionName.equals("")) {
								appInfoVersion
								.setHTML("<b>Version: </b>N/A");
							} else {
								appInfoVersion
								.setHTML("<b>Version: </b>"
										+ selectedApp.versionName);
							}

							/* Get tool results */
							final String htmlToolResults = getHtmlToolResults(
									selectedApp.appId, toolsResults);
							toolResultsHtml.setHTML(htmlToolResults);
							logButton.setEnabled(true);
						}
					}

					public String getHtmlToolResults(String appId,
							List<ToolStatusGwt> toolResults) {
						
						/* Get pre-processing analysis results */
						String statuses = "<hr><div id=\"appInfoSectionHeader\">PreProcessing</div>\n";
						for (int i = 0; i < toolResults.size(); i++) {
							AnalysisType analysisType = toolResults.get(i).getAnalysisType();
							if (analysisType == AnalysisType.PREPROCESSOR) {
								statuses += getToolStatusHtmlDisplay(toolResults.get(i));
							}
						}
						
						/* Get analysis tool results. */
						statuses += "<hr><div id=\"appInfoSectionHeader\">Tools</div>\n";
						for (int i = 0; i < toolResults.size(); i++) {
							AnalysisType analysisType = toolResults.get(i).getAnalysisType();
							if (analysisType == AnalysisType.ANALYSISTOOL) {
								statuses += getToolStatusHtmlDisplay(toolResults.get(i));
							}
						}
						
						/* Get audit results */
						statuses += "<hr><div id=\"appInfoSectionHeader\">Audit</div>\n";
						for (int i = 0; i < toolResults.size(); i++) {
							AnalysisType analysisType = toolResults.get(i).getAnalysisType();
							if (analysisType == AnalysisType.AUDIT) {
								statuses += getToolStatusHtmlDisplay(toolResults.get(i));
							}
						}
						return statuses;
					}
					
					public String getToolStatusHtmlDisplay(ToolStatusGwt toolStatus) {
						return "<table>"
								+ "<tr>\n"
								+ "<td align=\"left\" width=\"185\">"
								+ toolStatus.getToolDisplayName()
								+ "</td>\n"
								+ "<td align=\"left\" width=\"120\">"
								+ toolStatus.getStatusDescription()
								+ "</td>\n"
								+"<td align=\"left\" width=\"45\">"
								+ toolStatus.getReport()
								+ "</td>\n"
								+ "</tr>\n"
								+ "</table>";
					}
				});
			}
		}
	}
	

	class AppUploadFormHandler implements FormHandler {
		AppUploadDialogBox submitAppDialogBox = null;
		String apkFileName = null;

		public AppUploadFormHandler(AppUploadDialogBox submitAppDialogBox) {
			this.submitAppDialogBox = submitAppDialogBox;
		}

		@Override
		@Deprecated
		public void onSubmit(FormSubmitEvent event) {
			submitAppDialogBox.submitAppStatusLabel
			.setText("Validating File...");
			apkFileName = submitAppDialogBox.fileUpload.getFilename();

			if (apkFileName.length() == 0) {
				submitAppDialogBox.submitAppStatusLabel.setText("");
				showMessageDialog("App Submission Error", "No file selected",
						true);
				event.setCancelled(true);
			} else if (!ValidateBase.isPrintable(apkFileName)) {
				submitAppDialogBox.submitAppStatusLabel.setText("");
				showMessageDialog("App Submission Error", "File \""
						+ apkFileName + "\" contains an illegal character.",
						true);
				event.setCancelled(true);
			} else if (!ValidateBase.hasValidAppFileExtension(apkFileName)) {
				submitAppDialogBox.submitAppStatusLabel.setText("");
				showMessageDialog("App Submission Error", "File \""
						+ apkFileName + "\" is not an APK file.", true);
				event.setCancelled(true);
			} else {
				submitAppDialogBox.cancelButton.setEnabled(false);
				submitAppDialogBox.submitButton.setEnabled(false);
				submitAppDialogBox.submitAppStatusLabel.setText("Uploading "
						+ apkFileName + "...");
			}
		}

		@Override
		@Deprecated
		public void onSubmitComplete(FormSubmitCompleteEvent event) {
			submitAppDialogBox.submitAppStatusLabel.setText("");
			killDialogBox(submitAppDialogBox);
			showMessageDialog("Submit App", "File \""
					+ apkFileName + "\" was uploaded successfully.", false);
		}
	}

	class ReportUploadFormHandler implements FormHandler {

		ReportUploadDialogBox reportUploadDialogBox = null;
		String username = null;
		String appid = null;
		AppInfoGwt selected = null;

		public ReportUploadFormHandler(
				ReportUploadDialogBox reportUploadDialogBox, String username,
				AppInfoGwt selected) {
			this.reportUploadDialogBox = reportUploadDialogBox;
			this.selected = selected;
			this.username = username;
			this.appid = selected.appId;
		}

		@Override
		@Deprecated
		public void onSubmit(FormSubmitEvent event) {
			String reportFileName = reportUploadDialogBox.fileUpload
					.getFilename();
			
			String[] availableToolNames = null;
			String[] availableToolTypes = null;
			if (selected.os == DeviceOS.ANDROID) {
				availableToolNames = androidToolNames;
				availableToolTypes = androidToolTypes;
			} else if (selected.os == DeviceOS.IOS) {
				availableToolNames = iosToolNames;
				availableToolTypes = iosToolTypes;
			}
			
			if (reportFileName.length() == 0) {
				showMessageDialog("Report Submission Error",
						"No file selected", true);
				event.setCancelled(true);
			} else if (!ValidateBase.isLegalFileName(reportFileName)) {
				showMessageDialog("App Submission Error", "File \""
						+ reportFileName + "\" contains an illegal character.",
						true);
				event.setCancelled(true);
			} else if (!validReportFileName(reportFileName,
					reportUploadDialogBox.toolNamesComboBox, availableToolNames,
					availableToolTypes)) {
				event.setCancelled(true);
			} else {
				reportUploadDialogBox.cancelButton.setEnabled(false);
				reportUploadDialogBox.submitButton.setEnabled(false);
				reportUploadDialogBox.statusLabel.setText("Uploading "
						+ reportFileName + "...");
			}
		}

		@Override
		@Deprecated
		public void onSubmitComplete(FormSubmitCompleteEvent event) {
			reportUploadDialogBox.statusLabel.setText("");
			String reportFileName = reportUploadDialogBox.fileUpload
					.getFilename();
			showMessageDialog("Report Submission Error", "Report '"
					+ reportFileName + "' submitted successfully. ", false);
			killDialogBox(reportUploadDialogBox);
		}
	}

	public static int[] getCenterPosition(
			com.google.gwt.user.client.ui.UIObject object) {
		final int windowWidth = Window.getClientWidth();
		final int windowHeight = Window.getClientHeight();
		final int xposition = (windowWidth / 2)
				- (object.getOffsetHeight() / 2);
		final int yposition = (windowHeight / 2)
				- (object.getOffsetWidth() / 2);
		final int[] position = { xposition, yposition };
		return position;
	}

	public static void killDialogBox(DialogBox dialogBox) {
		if (dialogBox != null) {
			log.fine("Closing dialog box");
			dialogBox.hide();
			dialogBox = null;
		} else {
			log.fine("Can't close dialog box. dialogBox is null");
		}
	}

	public void showExpiredSessionMessage() {
		killDialogBox(appUploadDialogBox);
		killDialogBox(errorDialogBox);
		killDialogBox(messageDialogBox);
		killDialogBox(aboutDialogBox);
		killDialogBox(usersDialogBox);
		killDialogBox(deleteConfirmDialogBox);
		killDialogBox(reportUploadDialogBox);
		killDialogBox(userInfoEditDialogBox);
		AppVetPanel.showMessageDialog("AppVet Session", "AppVet session has expired",
				true);
		messageDialogBox.closeButton.setFocus(true);
		messageDialogBox.closeButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				killDialogBox(messageDialogBox);
				final LoginPanel loginPanel = new LoginPanel(Unit.PX);
				final RootLayoutPanel rootLayoutPanel = RootLayoutPanel.get();
				rootLayoutPanel.clear();
				rootLayoutPanel.add(loginPanel);
			}

		});
	}

	public static void showMessageDialog(String windowTitle, String message,
			boolean isError) {
		messageDialogBox = new MessageDialogBox(message, isError);
		messageDialogBox.setText(windowTitle);
		messageDialogBox.center();
		messageDialogBox.closeButton.setFocus(true);
		messageDialogBox.closeButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				killDialogBox(messageDialogBox);
			}

		});
	}

	public static boolean validReportFileName(String reportFileName,
			ListBox comboBox, String[] availableToolNames,
			String[] selectedReportFileTypes) {

		final int selectedToolNameReport = comboBox.getSelectedIndex();
		final String selectedTool = availableToolNames[selectedToolNameReport];
		final String selectedReportFileType = selectedReportFileTypes[selectedToolNameReport];
		final String selectedReportFileTypeLowercase = selectedReportFileType
				.toLowerCase();
		final String reportFileLowerCase = reportFileName.toLowerCase();

		if (selectedReportFileTypeLowercase.endsWith("html")) {
			if (!reportFileLowerCase.endsWith("html")) {
				showMessageDialog("Report Submission Error", selectedTool
						+ " reports must be HTML files.", true);
				return false;
			}
		} else if (selectedReportFileTypeLowercase.endsWith("pdf")) {
			if (!reportFileLowerCase.endsWith("pdf")) {
				showMessageDialog("Report Submission Error", selectedTool
						+ " reports must be PDF files.", true);
				return false;
			}
		} else if (selectedReportFileTypeLowercase.endsWith("txt")) {
			if (!reportFileLowerCase.endsWith("txt")) {
				showMessageDialog("Report Submission Error", selectedTool
						+ " reports must be TXT files.", true);
				return false;
			}
		} else if (selectedReportFileTypeLowercase.endsWith("rtf")) {
			if (!reportFileLowerCase.endsWith("rtf")) {
				showMessageDialog("Report Submission Error", selectedTool
						+ " reports must be RTF files.", true);
				return false;
			}
		} else if (selectedReportFileTypeLowercase.endsWith("xml")) {
			if (!reportFileLowerCase.endsWith("xml")) {
				showMessageDialog("Report Submission Error", selectedTool
						+ " reports must be XML files.", true);
				return false;
			}
		}
		return true;
	}


	public AppVetPanel(Unit unit, final ConfigInfoGwt configInfo,
			List<AppInfoGwt> initialApps) {
		super(Unit.PX);

		Window.addResizeHandler(new ResizeHandler() {
			Timer resizeTimer = new Timer() {

				@Override
				public void run() {
					resizeComponents();
				}

			};

			@Override
			public void onResize(ResizeEvent event) {
				resizeTimer.cancel();
				resizeTimer.schedule(250);
			}

		});

		userInfo = configInfo.getUserInfo();
		userName = userInfo.getUserName();
		allApps = initialApps;

		sinkEvents(Event.ONCLICK);
		sessionId = configInfo.getSessionId();
		sessionExpirationLong = configInfo.getSessionExpirationLong();
		MAX_SESSION_IDLE_DURATION = configInfo.getMaxIdleTime();
		POLLING_INTERVAL = configInfo.getUpdatesDelay();

		setSize("100%", "");
		setStyleName("mainDockPanel");
		SERVLET_URL = configInfo.getAppVetServletUrl();
		HOST_URL = configInfo.getAppVetHostUrl();
		appSelectionModel = new SingleSelectionModel<AppInfoGwt>();
		appSelectionModel.addSelectionChangeHandler(new AppListHandler(this,
				configInfo));
		if (configInfo.getAndroidToolNames() == null) {
			log.warning("Available tools is null");
		}
		androidToolNames = configInfo.getAndroidToolNames();
		androidToolIDs = configInfo.getAndroidToolIDs();
		androidToolTypes = configInfo.getAndroidToolTypes();
		iosToolNames = configInfo.getiOSToolNames();
		iosToolIDs = configInfo.getiOSToolIDs();
		iosToolTypes = configInfo.getiOSToolTypes();

		final VerticalPanel northAppVetPanel = new VerticalPanel();
		northAppVetPanel
		.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		northAppVetPanel.setStyleName("northAppVetPanel");
		northAppVetPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		addNorth(northAppVetPanel, 125.0);
		northAppVetPanel.setSize("100%", "");

		final HorizontalPanel horizontalPanel_5 = new HorizontalPanel();
		horizontalPanel_5
		.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		horizontalPanel_5.setStyleName("appVetHeaderPanel");
		northAppVetPanel.add(horizontalPanel_5);
		northAppVetPanel.setCellVerticalAlignment(horizontalPanel_5, HasVerticalAlignment.ALIGN_MIDDLE);
		horizontalPanel_5.setWidth("100%");
		northAppVetPanel.setCellWidth(horizontalPanel_5, "100%");

		final InlineHTML nlnhtmlNewInlinehtml_1 = new InlineHTML(
				"<img border=\"0\" width=\"192px\" src=\"images/appvet_logo.png\" alt=\"appvet\" />");
		nlnhtmlNewInlinehtml_1.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
		nlnhtmlNewInlinehtml_1.setStyleName("");
		horizontalPanel_5.add(nlnhtmlNewInlinehtml_1);
		horizontalPanel_5.setCellWidth(nlnhtmlNewInlinehtml_1, "33%");
		horizontalPanel_5.setCellVerticalAlignment(nlnhtmlNewInlinehtml_1,
				HasVerticalAlignment.ALIGN_MIDDLE);

		final HorizontalPanel horizontalPanel_6 = new HorizontalPanel();
		horizontalPanel_6
		.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		horizontalPanel_6
		.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		horizontalPanel_5.add(horizontalPanel_6);
		horizontalPanel_6.setWidth("");
		horizontalPanel_5.setCellWidth(horizontalPanel_6, "34%");
		horizontalPanel_5.setCellHorizontalAlignment(horizontalPanel_6,
				HasHorizontalAlignment.ALIGN_CENTER);
		horizontalPanel_5.setCellVerticalAlignment(horizontalPanel_6,
				HasVerticalAlignment.ALIGN_MIDDLE);

		searchTextBox = new TextBox();
		searchTextBox.setText("Search");
		searchTextBox.setStyleName("searchTextBox");
		searchTextBox.setTitle("Search by app ID, name, release kit, etc.");
		searchTextBox.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				searchTextBox.setText("");
			}

		});

		searchTextBox.addKeyPressHandler(new KeyPressHandler() {

			@Override
			public void onKeyPress(KeyPressEvent event_) {
				final boolean enterPressed = KeyCodes.KEY_ENTER == event_
						.getNativeEvent().getKeyCode();
				final String searchString = searchTextBox.getText();

				if (enterPressed) {
					final int numFound = search();
					if (numFound > 0) {
						appsLabel.setText("Search Results for \""
								+ searchString + "\"");
					}
				}
			}

		});

		searchTextBox.setSize("300px", "22px");

		horizontalPanel_6.add(searchTextBox);
		horizontalPanel_6.setCellVerticalAlignment(searchTextBox,
				HasVerticalAlignment.ALIGN_MIDDLE);

		final PushButton searchButton = new PushButton("Search");
		searchButton.setTitle("Search by app ID, name, release kit, etc.");
		searchButton.getUpFace().setHTML("");
		searchButton.setSize("18px", "18px");
		searchButton
		.setHTML("<img width=\"18px\" src=\"images/icon-search.png\" alt=\"search\" />");
		searchButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				final String searchString = searchTextBox.getText();
				final int numFound = search();
				if (numFound > 0) {
					appsLabel.setText("Search Results for \"" + searchString
							+ "\"");
				}
			}

		});

		horizontalPanel_6.add(searchButton);
		horizontalPanel_6.setCellHorizontalAlignment(searchButton,
				HasHorizontalAlignment.ALIGN_CENTER);
		horizontalPanel_6.setCellVerticalAlignment(searchButton,
				HasVerticalAlignment.ALIGN_MIDDLE);
		
		Image image = new Image("images/nist-gray.png");
		horizontalPanel_5.add(image);
		horizontalPanel_5.setCellHorizontalAlignment(image, HasHorizontalAlignment.ALIGN_RIGHT);
		horizontalPanel_5.setCellWidth(image, "33%");

		final HorizontalPanel horizontalPanel_3 = new HorizontalPanel();
		northAppVetPanel.add(horizontalPanel_3);
		northAppVetPanel.setCellHorizontalAlignment(horizontalPanel_3,
				HasHorizontalAlignment.ALIGN_CENTER);
		horizontalPanel_3.setWidth("100%");
		northAppVetPanel.setCellWidth(horizontalPanel_3, "100%");
		final MenuBar appVetMenuBar = new MenuBar(false);
		horizontalPanel_3.add(appVetMenuBar);

		appVetMenuBar.setStyleName("appVetMenuBar");
		appVetMenuBar.setAutoOpen(true);
		appVetMenuBar.setWidth("250px");
		appVetMenuBar.setAnimationEnabled(false);
		final MenuBar userMenuBar = new MenuBar(true);
		accountMenuItem = new MenuItem(userInfo.getNameWithLastNameInitial(),
				true, userMenuBar);
		accountMenuItem.setStyleName("AccountMenuItem");

		final MenuItem accountSettingsMenuItem = new MenuItem(
				"Account Settings", false, new Command() {
					@Override
					public void execute() {
						updateUserInfo();
					}
				});

		userMenuBar.addItem(accountSettingsMenuItem);

		final MenuItem myAppsMenuItem = new MenuItem("My Apps", false,
				new Command() {

			@Override
			public void execute() {
				searchTextBox.setText(userInfo.getUserName());
				final int numFound = search();
				if (numFound > 0) {
					appsLabel.setText("My Apps");
				}
			}

		});
		userMenuBar.addItem(myAppsMenuItem);

		final MenuItemSeparator separator = new MenuItemSeparator();
		userMenuBar.addSeparator(separator);
		final MenuItem logoutMenuItem = new MenuItem("Logout", false,
				new Command() {

			@Override
			public void execute() {
				appVetServiceAsync.removeSession(sessionId,
						new AsyncCallback<Boolean>() {

					@Override
					public void onFailure(Throwable caught) {
						AppVetPanel.showMessageDialog("AppVet Error",
								"App list retrieval error",
								true);
						errorDialogBox.closeButton
						.setFocus(true);
						errorDialogBox.closeButton
						.addClickHandler(new ClickHandler() {

							@Override
							public void onClick(
									ClickEvent event) {
								killDialogBox(errorDialogBox);
							}

						});
					}

					@Override
					public void onSuccess(Boolean result) {
						if (result == false) {
							AppVetPanel.showMessageDialog(
									"AppVet Error",
									"Could not remove session",
									true);
							errorDialogBox.closeButton
							.setFocus(true);
							errorDialogBox.closeButton
							.addClickHandler(new ClickHandler() {

								@Override
								public void onClick(
										ClickEvent event) {
									killDialogBox(errorDialogBox);
								}

							});
						} else {
							pollingTimer.cancel();
							final LoginPanel loginPanel = new LoginPanel(
									Unit.PX);
							final RootLayoutPanel rootLayoutPanel = RootLayoutPanel
									.get();
							rootLayoutPanel.clear();
							rootLayoutPanel.add(loginPanel);

							System.gc();
						}
					}

				});
			}

		});

		userMenuBar.addItem(logoutMenuItem);
		appVetMenuBar.addItem(accountMenuItem);

		final MenuBar helpMenuBar = new MenuBar(true);
		final MenuItem helpMenuItem = new MenuItem("Help", true, helpMenuBar);
		final MenuItem aboutMenuItem = new MenuItem("About", false,
				new Command() {

			@Override
			public void execute() {
				aboutDialogBox = new AboutDialogBox(configInfo
						.getAppVetVersion());
				aboutDialogBox.setText("About");
				aboutDialogBox.center();
				aboutDialogBox.closeButton.setFocus(true);
				aboutDialogBox.closeButton
				.addClickHandler(new ClickHandler() {

					@Override
					public void onClick(ClickEvent event) {
						killDialogBox(aboutDialogBox);
					}

				});
			}

		});

		
		final MenuItem documentationMenuItem = new MenuItem("Documentation", false,
				new Command() {

			@Override
			public void execute() {
	            Window.open("http://csrc.nist.gov/projects/appvet/", "_blank", null);
			}

		});
		helpMenuBar.addItem(documentationMenuItem);

		appVetMenuBar.addItem(helpMenuItem);
		helpMenuBar.addItem(aboutMenuItem);

		
		horizontalPanel_3.add(statusMessageLabel);
		horizontalPanel_3.setCellVerticalAlignment(statusMessageLabel,
				HasVerticalAlignment.ALIGN_MIDDLE);
		horizontalPanel_3.setCellHorizontalAlignment(statusMessageLabel,
				HasHorizontalAlignment.ALIGN_RIGHT);
		horizontalPanel_3.setCellWidth(statusMessageLabel, "100%");

		statusMessageLabel.setStyleName("devModeIndicator");
		statusMessageLabel
		.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
		statusMessageLabel.setSize("420px", "18");

		final MenuBar adminMenuBar = new MenuBar(true);
		final MenuItem adminMenuItem = new MenuItem("Admin", true, adminMenuBar);

		final MenuItem mntmAppVetLog = new MenuItem("AppVet Log", false,
				new Command() {

			@Override
			public void execute() {
				final String dateString = "?nocache"
						+ new Date().getTime();
				final String url = SERVLET_URL + dateString + 
						"&" + AppVetParameter.COMMAND.value + "=" + AppVetServletCommand.GET_APPVET_LOG.name() + 
						"&" + AppVetParameter.SESSIONID.value + "=" + sessionId;
				Window.open(url, "_blank", "");
			}

		});

		adminMenuBar.addItem(mntmAppVetLog);

		final MenuItem usersMenuItem = new MenuItem("Users", false,
				new Command() {

			@Override
			public void execute() {
				usersDialogBox = new UsersDialogBox();
				usersDialogBox.setText("Users");
				usersDialogBox.center();
				usersDialogBox.doneButton.setFocus(true);
				usersDialogBox.doneButton
				.addClickHandler(new ClickHandler() {

					@Override
					public void onClick(ClickEvent event) {
						killDialogBox(usersDialogBox);
					}

				});
			}

		});

		adminMenuBar.addItem(usersMenuItem);

		if (userInfo.getRole().equals("ADMIN")) {
			appVetMenuBar.addItem(adminMenuItem);
		}

		// Remove first element containing the lastUpdate timestamp
		AppInfoGwt timeStampObject = null;
		if (initialApps != null && initialApps.size() > 0) {
			timeStampObject = initialApps.remove(0);
			lastAppsListUpdate = timeStampObject.getLastAppUpdate();
		}

		final HorizontalPanel horizontalPanel_2 = new HorizontalPanel();
		horizontalPanel_2
		.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		horizontalPanel_2.setStyleName("footerPanel");
		addSouth(horizontalPanel_2, 35.0);
		horizontalPanel_2.setSize("100%", "");

//		final Label lastUpdatedLabel = new Label("Last updated: "
//				+ configInfo.getLastUpdated());
//		lastUpdatedLabel
//		.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
//		lastUpdatedLabel.setStyleName("lastUpdated");
//		horizontalPanel_2.add(lastUpdatedLabel);
//		lastUpdatedLabel.setWidth("200px");
//		horizontalPanel_2.setCellWidth(lastUpdatedLabel, "100%");
//		horizontalPanel_2.setCellVerticalAlignment(lastUpdatedLabel,
//				HasVerticalAlignment.ALIGN_MIDDLE);

		final HorizontalSplitPanel centerAppVetSplitPanel = new HorizontalSplitPanel();
		centerAppVetSplitPanel.setSplitPosition("64%");
		centerAppVetSplitPanel.setSize("", "");

		final SimplePanel leftCenterPanel = new SimplePanel();
		centerAppVetSplitPanel.setLeftWidget(leftCenterPanel);
		leftCenterPanel.setSize("", "95%");

		final DockPanel dockPanel_1 = new DockPanel();
		dockPanel_1.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		leftCenterPanel.setWidget(dockPanel_1);
		dockPanel_1.setSize("100%", "");
		rightCenterPanel = new SimplePanel();
		centerAppVetSplitPanel.setRightWidget(rightCenterPanel);
		rightCenterPanel.setSize("", "630px");

		final VerticalPanel appInfoVerticalPanel = new VerticalPanel();
		appInfoVerticalPanel
		.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		rightCenterPanel.setWidget(appInfoVerticalPanel);
		appInfoVerticalPanel.setSize("99%", "");

		final HorizontalPanel horizontalPanel_1 = new HorizontalPanel();
		horizontalPanel_1
		.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		horizontalPanel_1.setStyleName("iconPanel");
		appInfoVerticalPanel.add(horizontalPanel_1);
		appInfoVerticalPanel.setCellWidth(horizontalPanel_1, "100%");
		horizontalPanel_1.setSize("", "");

		appInfoIcon = new Image("");
		appInfoIcon.setVisible(false);
		appInfoIcon.setAltText("");
		horizontalPanel_1.add(appInfoIcon);
		horizontalPanel_1.setCellVerticalAlignment(appInfoIcon,
				HasVerticalAlignment.ALIGN_MIDDLE);
		appInfoIcon.setSize("70px", "70px");

		final VerticalPanel verticalPanel = new VerticalPanel();
		horizontalPanel_1.add(verticalPanel);
		appInfoName = new HTML("", true);
		appInfoName.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
		verticalPanel.add(appInfoName);
		appInfoName.setStyleName("appInfoName");
		appInfoName.setWidth("");
		horizontalPanel_1.setCellVerticalAlignment(appInfoName,
				HasVerticalAlignment.ALIGN_MIDDLE);
		appInfoVersion = new HTML("", true);
		appInfoVersion
		.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
		appInfoVersion.setStyleName("appInfoVersion");
		verticalPanel.add(appInfoVersion);
		appsListButtonPanel = new HorizontalPanel();
		appsListButtonPanel
		.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		dockPanel_1.add(appsListButtonPanel, DockPanel.NORTH);
		dockPanel_1.setCellHorizontalAlignment(appsListButtonPanel,
				HasHorizontalAlignment.ALIGN_CENTER);
		dockPanel_1.setCellWidth(appsListButtonPanel, "100%");
		dockPanel_1.setCellVerticalAlignment(appsListButtonPanel,
				HasVerticalAlignment.ALIGN_MIDDLE);
		appsListButtonPanel.setStyleName("appListButtonPanel");
		appsListButtonPanel.setSize("100%", "");

		appsLabel = new InlineLabel("Apps");
		appsLabel.setStyleName("AppsLabel");
		appsListButtonPanel.add(appsLabel);
		appsListButtonPanel.setCellWidth(appsLabel, "50%");
		appsListButtonPanel.setCellVerticalAlignment(appsLabel,
				HasVerticalAlignment.ALIGN_MIDDLE);
		appsLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
		appsLabel.setWidth("60px");

		final HorizontalPanel horizontalPanel = new HorizontalPanel();
		horizontalPanel
		.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
		horizontalPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		horizontalPanel.setStyleName("appFunctionButtonPanel");
		appsListButtonPanel.add(horizontalPanel);
		appsListButtonPanel.setCellWidth(horizontalPanel, "50%");
		appsListButtonPanel.setCellVerticalAlignment(horizontalPanel,
				HasVerticalAlignment.ALIGN_MIDDLE);
		appsListButtonPanel.setCellHorizontalAlignment(horizontalPanel,
				HasHorizontalAlignment.ALIGN_RIGHT);
		horizontalPanel.setWidth("");

		final PushButton submitButton = new PushButton("Submit");
		submitButton.setTitle("Submit App");
		submitButton
		.setHTML("<img width=\"18px\" src=\"images/icon-submit.png\" alt=\"Submit\" />");
		submitButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				appUploadDialogBox = new AppUploadDialogBox(sessionId,
						SERVLET_URL);
				appUploadDialogBox.setText("Submit App");
				appUploadDialogBox.center();
				appUploadDialogBox.cancelButton.setFocus(true);
				appUploadDialogBox.cancelButton
				.addClickHandler(new ClickHandler() {

					@Override
					public void onClick(ClickEvent event) {
						killDialogBox(appUploadDialogBox);
					}

				});

				appUploadDialogBox.uploadAppForm
				.addFormHandler(new AppUploadFormHandler(
						appUploadDialogBox));
			}
		});

		final PushButton viewAllButton = new PushButton("View All");
		viewAllButton.setTitle("View All");
		viewAllButton
		.setHTML("<img width=\"18px\" src=\"images/icon-view-all.png\" alt=\"view-all\" />");
		viewAllButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				searchMode = false;
				setAllApps();
			}

		});

		horizontalPanel.add(viewAllButton);
		horizontalPanel.setCellHorizontalAlignment(viewAllButton,
				HasHorizontalAlignment.ALIGN_CENTER);
		horizontalPanel.setCellVerticalAlignment(viewAllButton,
				HasVerticalAlignment.ALIGN_MIDDLE);
		viewAllButton.setSize("18px", "18px");
		horizontalPanel.add(submitButton);
		horizontalPanel.setCellVerticalAlignment(submitButton,
				HasVerticalAlignment.ALIGN_MIDDLE);
		horizontalPanel.setCellHorizontalAlignment(submitButton,
				HasHorizontalAlignment.ALIGN_CENTER);
		submitButton.setSize("18px", "18px");
		downloadButton = new PushButton("Download");
		downloadButton.setTitle("Download Reports");
		downloadButton
		.setHTML("<img width=\"18px\" src=\"images/icon-download.png\" alt=\"Download\" />");
		horizontalPanel.add(downloadButton);
		downloadButton.setEnabled(true);
		downloadButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				final AppInfoGwt selected = appSelectionModel
						.getSelectedObject();
				if (selected == null) {
					showMessageDialog("AppVet Error", "No app is selected", true);
				} else {
					
					final String appId = selected.appId;
					final String dateString = "?nocache"
							+ new Date().getTime();
					final String url = SERVLET_URL
							+ dateString + 
							"&" + AppVetParameter.COMMAND.value + "=" + AppVetServletCommand.DOWNLOAD_REPORTS.name() + 
							"&" + AppVetParameter.APPID.value + "=" + appId +
							"&" + AppVetParameter.SESSIONID.value + "=" + sessionId;
					Window.open(url, "_self", "");
					
				}
			}
		});

		horizontalPanel.setCellHorizontalAlignment(downloadButton,
				HasHorizontalAlignment.ALIGN_CENTER);
		horizontalPanel.setCellVerticalAlignment(downloadButton,
				HasVerticalAlignment.ALIGN_MIDDLE);
		appsListButtonPanel.setCellHorizontalAlignment(downloadButton,
				HasHorizontalAlignment.ALIGN_CENTER);
		downloadButton.setSize("18px", "18px");
		addReportButton = new PushButton("Add Report");
		horizontalPanel.add(addReportButton);
		horizontalPanel.setCellVerticalAlignment(addReportButton,
				HasVerticalAlignment.ALIGN_MIDDLE);
		addReportButton.setTitle("Override Report");
		addReportButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				final AppInfoGwt selected = appSelectionModel
						.getSelectedObject();
				
				String[] availableToolNames = null;
				String[] availableToolIDs = null;
				if (selected.os == DeviceOS.ANDROID) {
					availableToolNames = androidToolNames;
					availableToolIDs = androidToolIDs;
				} else if (selected.os == DeviceOS.IOS) {
					availableToolNames = iosToolNames;
					availableToolIDs = iosToolIDs;
				}
				
				if (selected == null) {
					showMessageDialog("AppVet Error", "No app is selected", true);
				} else {
					reportUploadDialogBox = new ReportUploadDialogBox(userName,
							sessionId, selected.appId, SERVLET_URL,
							availableToolNames, availableToolIDs);
					reportUploadDialogBox.setText("Override Report");
					reportUploadDialogBox.center();
					reportUploadDialogBox.cancelButton.setFocus(true);
					reportUploadDialogBox.cancelButton
					.addClickHandler(new ClickHandler() {

						@Override
						public void onClick(ClickEvent event) {
							killDialogBox(reportUploadDialogBox);
						}

					});
					reportUploadDialogBox.uploadReportForm
					.addFormHandler(new ReportUploadFormHandler(
							reportUploadDialogBox, userName,
							selected));
				}
			}
		});

		addReportButton.setSize("18px", "18px");
		addReportButton
		.setHTML("<img width=\"18px\" src=\"images/icon-submit-report.png\" alt=\"Add Report\" />");
		deleteButton = new PushButton("Delete");
		horizontalPanel.add(deleteButton);
		horizontalPanel.setCellVerticalAlignment(deleteButton,
				HasVerticalAlignment.ALIGN_MIDDLE);
		deleteButton
		.setHTML("<img width=\"18px\" src=\"images/icon-delete.png\" alt=\"delete\" />");
		deleteButton.setTitle("Delete App");
		deleteButton.setVisible(true);
		deleteButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				final AppInfoGwt selected = appSelectionModel
						.getSelectedObject();
				deleteConfirmDialogBox = new DeleteAppConfirmDialogBox(
						selected.appId, selected.appName);
				deleteConfirmDialogBox.setText("Confirm Delete");
				deleteConfirmDialogBox.center();
				deleteConfirmDialogBox.cancelButton.setFocus(true);
				deleteConfirmDialogBox.cancelButton
				.addClickHandler(new ClickHandler() {

					@Override
					public void onClick(ClickEvent event) {
						killDialogBox(deleteConfirmDialogBox);
						return;
					}

				});
				deleteConfirmDialogBox.okButton
				.addClickHandler(new ClickHandler() {

					@Override
					public void onClick(ClickEvent event) {
						killDialogBox(deleteConfirmDialogBox);
						if (selected != null) {
							deleteApp(selected.os, selected.appId, userName);
						}
					}

				});
			}
		});
		deleteButton.setSize("18px", "18px");
		logButton = new PushButton("Log");
		horizontalPanel.add(logButton);
		horizontalPanel.setCellVerticalAlignment(logButton,
				HasVerticalAlignment.ALIGN_MIDDLE);
		logButton.setTitle("View Log");
		logButton
		.setHTML("<img width=\"18px\" src=\"images/icon-log.png\" alt=\"log\" />");
		logButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				final AppInfoGwt selected = appSelectionModel
						.getSelectedObject();
				if (selected != null) {
					final String appId = selected.appId;
					final String dateString = "?nocache" + new Date().getTime();
					final String url = SERVLET_URL + dateString +
							"&" + AppVetParameter.COMMAND.value + "=" + AppVetServletCommand.GET_APP_LOG.name() +
							"&" + AppVetParameter.APPID.value + "=" + appId + 
							"&" + AppVetParameter.SESSIONID.value + "=" + sessionId;
					Window.open(url, "_blank", "");
				}
			}

		});
		logButton.setSize("18px", "18px");

		appsListTable = new AppsListPagingDataGrid<AppInfoGwt>();
		appsListTable.dataGrid.setStyleName("dataGrid");
		dockPanel_1.add(appsListTable, DockPanel.CENTER);
		dockPanel_1.setCellHorizontalAlignment(appsListTable,
				HasHorizontalAlignment.ALIGN_CENTER);
		dockPanel_1.setCellVerticalAlignment(appsListTable,
				HasVerticalAlignment.ALIGN_MIDDLE);
		appsListTable.setAppVetHostUrl(HOST_URL);
		appsListTable.dataGrid.setSize("99%", "");
		appsListTable.setDataList(initialApps);
		appsListTable.setSize("", "");
		appsListTable.dataGrid
		.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.DISABLED);
		appsListTable.dataGrid.setSelectionModel(appSelectionModel);
		addReportButton.setVisible(true);
		logButton.setVisible(true);

//		final Label lblNewLabel_1 = new Label("*See log for system errors");
//		lblNewLabel_1.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
//		appInfoVerticalPanel.add(lblNewLabel_1);
//		lblNewLabel_1.setWidth("200px");
//		appInfoVerticalPanel.setCellWidth(lblNewLabel_1, "100%");
		toolResultsHtml = new HTML("", true);
		appInfoVerticalPanel.add(toolResultsHtml);
		appInfoVerticalPanel.setCellWidth(toolResultsHtml, "100%");
		toolResultsHtml.setWidth("100%");
		toolResultsHtml
		.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
		toolResultsHtml.setStyleName("toolResultsHtml");

		add(centerAppVetSplitPanel);

		/*
	 // Add logo in bottom-right corner 
	 final InlineHTML nlnhtmlNewInlinehtml = new InlineHTML(
	 "<a href=\"http://www.example.com\"><img border=\"0\" width=\"75px\"  src=\"exampleImage.png\" alt=\"example\" /></a>"
	 ); nlnhtmlNewInlinehtml
	 .setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
	 nlnhtmlNewInlinehtml.setStyleName("mainTaLogo");
	 horizontalPanel_2.add(nlnhtmlNewInlinehtml);
	 nlnhtmlNewInlinehtml.setWidth("");
	 horizontalPanel_2.setCellHorizontalAlignment(nlnhtmlNewInlinehtml,
	 HasHorizontalAlignment.ALIGN_RIGHT);
	 horizontalPanel_2.setCellVerticalAlignment(nlnhtmlNewInlinehtml,
	 HasVerticalAlignment.ALIGN_MIDDLE);
		 */
		if ((initialApps != null) && (initialApps.size() > 0)) {
			appSelectionModel.setSelected(initialApps.get(0), true);
		} else {
			logButton.setEnabled(false);
			addReportButton.setEnabled(false);
			deleteButton.setEnabled(false);
			downloadButton.setEnabled(false);
		}
		pollServer(userName);
		scheduleResize();
	}

	public void deleteApp(final DeviceOS os, final String appid, final String username) {
		appVetServiceAsync.deleteApp(os, appid, username,
				new AsyncCallback<Boolean>() {

			@Override
			public void onFailure(Throwable caught) {
				showMessageDialog("AppVet Error",
						"App list retrieval error", true);
			}

			@Override
			public void onSuccess(Boolean deleted) {
				if (deleted == false) {
					showMessageDialog("AppVet Error",
							"Could not delete app", true);
				} else {
					final AppInfoGwt currentlySelectedApp = appSelectionModel
							.getSelectedObject();
					final int currentlySelectedIndex = getAppsListIndex(
							currentlySelectedApp, allApps);
					for (int i = 0; i < allApps.size(); i++) {
						final AppInfoGwt appInfoGwt = allApps.get(i);
						if (appInfoGwt.appId.equals(appid)) {
							allApps.remove(i);
							if (!searchMode) {
								appsListTable.remove(i);
							} else {
								appsListTable.remove(appid);
							}
							break;
						}
					}
					if (!searchMode) {
						if (allApps.size() > 0) {
							appSelectionModel.setSelected(
									allApps.get(currentlySelectedIndex),
									true);
						} else {
							appInfoVersion.setHTML("");
							appInfoIcon.setVisible(false);
							appInfoName.setText("");
							toolResultsHtml.setText("");
							logButton.setEnabled(false);
							addReportButton.setEnabled(false);
							deleteButton.setEnabled(false);
						}
					}
				}
			}

		});
	}

	public int getAppsListIndex(AppInfoGwt item, List<AppInfoGwt> appsList) {
		if (item != null) {
			for (int i = 0; i < appsList.size(); i++) {
				if (item.appId.equals(appsList.get(i).appId)) {
					return i;
				}
			}
		}
		return 0;
	}

	public synchronized void getUpdatedApps(String username) {
		appVetServiceAsync.getUpdatedApps(lastAppsListUpdate, username,
				new AsyncCallback<List<AppInfoGwt>>() {

			@Override
			public void onFailure(Throwable caught) {
				log.severe("Error retrieving updated apps: "
						+ caught.getMessage());
			}

			@Override
			public void onSuccess(List<AppInfoGwt> updatedAppsList) {
				if (updatedAppsList == null) {
					showMessageDialog("AppVet Database Error",
							"Could not retrieve updated apps", true);
				} else {
					if (updatedAppsList.size() > 0) {
						final AppInfoGwt timeStampObject = updatedAppsList
								.remove(0);
						lastAppsListUpdate = timeStampObject
								.getLastAppUpdate();
						if (updatedAppsList.size() > 0) {
							setUpdatedApps(updatedAppsList);
						}
					}
				}
			}

		});
	}

	@Override
	public void onBrowserEvent(Event event) {
		newBrowserEvent = true;
		sessionExpirationLong = new Date().getTime() 
				+ MAX_SESSION_IDLE_DURATION;
	}

	public void pollServer(String username) {
		final String user = username;
		pollingTimer = new Timer() {

			@Override
			public void run() {
				if (newBrowserEvent) {
					updateSessionExpiration();
					newBrowserEvent = false;
				}
				getUpdatedApps(user);
			}

		};
		pollingTimer.scheduleRepeating(POLLING_INTERVAL);
	}

	public void resizeComponents() {
		final int marginsHeights = 19;
		final int appVetPanelHeight = getOffsetHeight();
		final int appsListButtonPanelHeight = appsListButtonPanel
				.getOffsetHeight();
		final int appsListTableHeight = appVetPanelHeight
				- (int) NORTH_PANEL_HEIGHT - (int) SOUTH_PANEL_HEIGHT
				- appsListButtonPanelHeight - marginsHeights;
		appsListTable.setSize("100%", appsListTableHeight + "px");
		appsListTable.dataGrid.redraw();
		final int rightCenterPanelHeight = appVetPanelHeight
				- (int) NORTH_PANEL_HEIGHT - (int) SOUTH_PANEL_HEIGHT;
		rightCenterPanel.setSize("99%", rightCenterPanelHeight + "px");
	}

	// The size of the AppVet panel is 0 until displayed in rootlayoutpanel.
	public void scheduleResize() {
		final Timer resizeTimer = new Timer() {

			@Override
			public void run() {
				resizeComponents();
			}

		};
		resizeTimer.schedule(250);
	}

	public synchronized int search() {
		searchMode = true;
		statusMessageLabel.setText("Searching...");
		final String[] tokens = searchTextBox.getValue().split("\\s+");
		if (tokens == null) {
			return 0;
		}
		final ArrayList<AppInfoGwt> searchList = new ArrayList<AppInfoGwt>();
		for (int i = 0; i < tokens.length; i++) {
			if (ValidateBase.isLegalSearchString(tokens[i])) {
				for (int j = 0; j < allApps.size(); j++) {
					final AppInfoGwt appInfoSummary = allApps.get(j);
					if (appInfoSummary.tokenMatch(tokens[i])) {
						searchList.add(appInfoSummary);
					}
				}
			} else {
				log.warning("Search token: " + tokens[i] + " is not valid");
			}
		}
		searchTextBox.setText("Search");
		statusMessageLabel.setText("");
		appsListTable.setDataList(searchList);
		if (searchList.size() == 0) {
			showMessageDialog("Search Results", "No search results were found",
					true);
			return 0;
		} else {
			appSelectionModel.setSelected(searchList.get(0), true);
			return searchList.size();
		}
	}

	public synchronized void setAllApps() {
		appsLabel.setText("Apps");
		appsListTable.setDataList(allApps);
	}

	public synchronized void setUpdatedApps(List<AppInfoGwt> updatedAppsList) {
		for (int i = 0; i < updatedAppsList.size(); i++) {
			final AppInfoGwt updatedAppInfo = updatedAppsList.get(i);
			
			log.info("UPDATED APPSTATUS: " + updatedAppInfo.appStatus.name());
			int matchIndex = -1;
			for (int j = 0; j < allApps.size(); j++) {
				final AppInfoGwt appInList = allApps.get(j);
				if (updatedAppInfo.appId.equals(appInList.appId)) {
					matchIndex = j;
					break;
				}
			}
			if (matchIndex > -1) {
				// overwrites existing app
				allApps.set(matchIndex, updatedAppInfo);
				if (!searchMode) {
					appsListTable.set(matchIndex, updatedAppInfo);
				}
			} else {
				// adds new app
				allApps.add(0, updatedAppInfo);
				if (!searchMode) {
					appsListTable.add(0, updatedAppInfo);
				}
			}
		}
		final AppInfoGwt currentlySelectedApp = appSelectionModel
				.getSelectedObject();
		if (currentlySelectedApp == null) {
			return;
		}
		final int currentlySelectedIndex = getAppsListIndex(
				currentlySelectedApp, allApps);
		if (currentlySelectedIndex < 0) {
			return;
		}
		if (!searchMode) {
			if (allApps.size() > 0) {
				appSelectionModel.setSelected(
						allApps.get(currentlySelectedIndex), true);
			} else {
				appInfoIcon.setVisible(false);
				appInfoName.setText("");
				toolResultsHtml.setText("");
				logButton.setEnabled(false);
				addReportButton.setEnabled(false);
				deleteButton.setEnabled(false);
			}
		}
	}

	public synchronized void updateSessionExpiration() {
		appVetServiceAsync.updateSessionTimeout(sessionId, sessionExpirationLong,
				new AsyncCallback<Boolean>() {

			@Override
			public void onFailure(Throwable caught) {
				log.severe("Could not update session: "
						+ caught.getMessage());
			}

			@Override
			public void onSuccess(Boolean updatedSessionTimeout) {
				if (!updatedSessionTimeout) {
					// Session has expired
					pollingTimer.cancel();
					showExpiredSessionMessage();
				}
			}

		});
	}

	public void updateUserInfo() {
		userInfoEditDialogBox = new UserAcctDialogBox(userInfo);
		userInfoEditDialogBox.setText("Account Settings");
		userInfoEditDialogBox.center();
		userInfoEditDialogBox.cancelButton.setFocus(true);
		userInfoEditDialogBox.cancelButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				killDialogBox(userInfoEditDialogBox);
			}

		});
		userInfoEditDialogBox.updateButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				final String newLastName = userInfoEditDialogBox.lastNameTextBox
						.getText();
				final String newFirstName = userInfoEditDialogBox.firstNameTextBox
						.getText();
				final String newOrganization = userInfoEditDialogBox.organizationTextBox
						.getText();
				final String newEmail = userInfoEditDialogBox.emailTextBox
						.getText();
				final String newPassword1 = userInfoEditDialogBox.password1TextBox
						.getValue();
				final String newPassword2 = userInfoEditDialogBox.password2TextBox
						.getValue();
				final UserInfoGwt newUserInfo = new UserInfoGwt();
				newUserInfo.setUserName(userInfo.getUserName());
				newUserInfo.setLastName(newLastName);
				newUserInfo.setFirstName(newFirstName);
				newUserInfo.setOrganization(newOrganization);
				newUserInfo.setEmail(newEmail);
				newUserInfo.setChangePassword(true);
				newUserInfo.setPasswords(newPassword1, newPassword2);
				newUserInfo.setRole(userInfo.getRole());
				if (!newUserInfo.isValid()) {
					return;
				}

				appVetServiceAsync.updateSelf(newUserInfo,
						new AsyncCallback<Boolean>() {

					@Override
					public void onFailure(Throwable caught) {
						showMessageDialog("Update Error",
								"Could not update user information",
								true);
						killDialogBox(userInfoEditDialogBox);

					}

					@Override
					public void onSuccess(Boolean result) {
						final boolean updated = result.booleanValue();
						if (updated) {
							accountMenuItem.setText(userInfo
									.getNameWithLastNameInitial());
							userInfo.setUserName(userInfo.getUserName());
							userInfo.setLastName(newUserInfo
									.getLastName());
							userInfo.setFirstName(newUserInfo
									.getFirstName());
							userInfo.setOrganization(newUserInfo
									.getOrganization());
							userInfo.setEmail(newUserInfo.getEmail());
							newUserInfo.setChangePassword(false);
							newUserInfo.setPassword("");
							userInfo.setRole(newUserInfo.getRole());
							showMessageDialog(
									"Update Status",
									"Account settings updated successfully",
									false);
							killDialogBox(userInfoEditDialogBox);
						} else {
							showMessageDialog(
									"Update Error",
									"Could not update user information",
									true);
							killDialogBox(userInfoEditDialogBox);
						}
					}

				});
			}
		});
	}
}
