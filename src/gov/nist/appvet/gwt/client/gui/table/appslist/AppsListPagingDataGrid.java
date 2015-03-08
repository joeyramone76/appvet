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
package gov.nist.appvet.gwt.client.gui.table.appslist;

import gov.nist.appvet.gwt.client.gui.table.PagingDataGrid;
import gov.nist.appvet.gwt.shared.AppInfoGwt;
import gov.nist.appvet.shared.os.DeviceOS;
import gov.nist.appvet.shared.status.AppStatus;

import java.util.Comparator;
import java.util.Date;
import java.util.logging.Logger;

import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;


public class AppsListPagingDataGrid<T> extends PagingDataGrid<T> {

	private final DateTimeFormat dateTimeFormat = DateTimeFormat
			.getFormat("yyyy-MM-dd HH:mm:ss");
	private String appVetHostUrl = null;
	private static Logger log = Logger.getLogger("AppsListPagingDataGrid");

	@Override
	public void initTableColumns(DataGrid<T> dataGrid,
			ListHandler<T> sortHandler) {

		//--------------------------- App ID -----------------------------------
		final Column<T, String> appIdColumn = new Column<T, String>(
				new TextCell()) {

			@Override
			public String getValue(T object) {
				return ((AppInfoGwt) object).appId;
			}

		};
		appIdColumn.setSortable(true);
		sortHandler.setComparator(appIdColumn, new Comparator<T>() {

			@Override
			public int compare(T o1, T o2) {
				return ((AppInfoGwt) o1).appId
						.compareTo(((AppInfoGwt) o2).appId);
			}

		});
		appIdColumn.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
		dataGrid.addColumn(appIdColumn, "ID");
		dataGrid.setColumnWidth(appIdColumn, "40px");

		//----------------------- Platform/OS Icon -----------------------------
		final SafeHtmlCell osIconCell = new SafeHtmlCell();
		final Column<T, SafeHtml> osIconColumn = new Column<T, SafeHtml>(osIconCell) {

			@Override
			public SafeHtml getValue(T object) {
				final SafeHtmlBuilder sb = new SafeHtmlBuilder();
				final DeviceOS os = ((AppInfoGwt) object).os;
				if (os == null) {
					log.warning("OS is null");
					return sb.toSafeHtml();
				} else {
					log.info("App status in table is: " + os);
				}
				if (os == DeviceOS.ANDROID) {
					iconVersion++; 
					final String iconPath = appVetHostUrl
							+ "/appvet_images/android_logo_black.png";
					sb.appendHtmlConstant("<img width=\"15\" src=\"" + iconPath
							+ "\" alt=\"\" />");
				} else if (os == DeviceOS.IOS) {
					final String iconPath = appVetHostUrl
							+ "/appvet_images/apple_logo_black.png";
					sb.appendHtmlConstant("<img width=\"15\" src=\"" + iconPath
							+ "\" alt=\"\" />");
				} 
				return sb.toSafeHtml();
			}

		};
		osIconColumn.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
		osIconColumn.setSortable(false);
		dataGrid.addColumn(osIconColumn, "");
		dataGrid.setColumnWidth(osIconColumn, "20px");

		//--------------------------- App Icon ---------------------------------
		final SafeHtmlCell iconCell = new SafeHtmlCell();
		final Column<T, SafeHtml> iconColumn = new Column<T, SafeHtml>(iconCell) {

			@Override
			public SafeHtml getValue(T object) {
				final SafeHtmlBuilder sb = new SafeHtmlBuilder();
				final String appId = ((AppInfoGwt) object).appId;
				final AppStatus appStatus = ((AppInfoGwt) object).appStatus;
				if (appStatus == null) {
					log.warning("App status is null");
					return sb.toSafeHtml();
				} else {
					log.info("App status in table is: " + appStatus.name());
				}
				if (appStatus == AppStatus.REGISTERING) {
					iconVersion++; 
					final String iconPath = appVetHostUrl
							+ "/appvet_images/default.png?v" + iconVersion;
					sb.appendHtmlConstant("<img width=\"20\" src=\"" + iconPath
							+ "\" alt=\"\" />");
				} else if (appStatus == AppStatus.PENDING) {
					final String iconPath = appVetHostUrl
							+ "/appvet_images/default.png";
					sb.appendHtmlConstant("<img width=\"20\" src=\"" + iconPath
							+ "\" alt=\"\" />");
				} else if (appStatus == AppStatus.PROCESSING) {
					iconVersion++; 
					final String iconPath = appVetHostUrl + "/appvet_images/" + appId
							+ ".png?v" + iconVersion;
					sb.appendHtmlConstant("<img width=\"20\" src=\"" + iconPath
							+ "\" alt=\"\" />");
				} else {
					iconVersion++; 
					final String iconPath = appVetHostUrl + "/appvet_images/" + appId
							+ ".png";
					sb.appendHtmlConstant("<img width=\"20\" src=\"" + iconPath
							+ "\" alt=\"\" />");
				}
				return sb.toSafeHtml();
			}

		};
		iconColumn.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		iconColumn.setSortable(false);
		dataGrid.addColumn(iconColumn, "");
		dataGrid.setColumnWidth(iconColumn, "25px");

		//------------------------- App Name -----------------------------------
		final Column<T, String> appNameColumn = new Column<T, String>(
				new TextCell()) {

			@Override
			public String getValue(T object) {
				return ((AppInfoGwt) object).appName;
			}

		};
		appNameColumn.setSortable(true);
		sortHandler.setComparator(appNameColumn, new Comparator<T>() {

			@Override
			public int compare(T o1, T o2) {
				return ((AppInfoGwt) o1).appName
						.compareTo(((AppInfoGwt) o2).appName);
			}

		});
		appNameColumn.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
		dataGrid.addColumn(appNameColumn, "App");
		dataGrid.setColumnWidth(appNameColumn, "127px");

		//----------------------------- Status ---------------------------------
		final SafeHtmlCell statusCell = new SafeHtmlCell();
		final Column<T, SafeHtml> statusColumn = new Column<T, SafeHtml>(
				statusCell) {

			@Override
			public SafeHtml getValue(T object) {
				final SafeHtmlBuilder sb = new SafeHtmlBuilder();
				final AppStatus appStatus = ((AppInfoGwt) object).appStatus;
				String statusHtml = null;
				if (appStatus == AppStatus.ERROR) {
					statusHtml = "<div id=\"error\" style='color: red'>ERROR</div>";
				} else if (appStatus == AppStatus.WARNING) {
					statusHtml = "<div id=\"warning\" style='color: orange'>"
							+ appStatus + "</div>";
				} else if (appStatus == AppStatus.PASS) {
					statusHtml = "<div id=\"endorsed\" style='color: green'>"
							+ appStatus + "</div>";
				} else if (appStatus == AppStatus.FAIL) {
					statusHtml = "<div id=\"error\" style='color: red'>FAIL</div>";
				} else {
					statusHtml = "<div id=\"error\" style='color: black'>" + appStatus.name() + "</div>";
				}
				sb.appendHtmlConstant(statusHtml);
				return sb.toSafeHtml();
			}

		};
		statusColumn.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
		statusColumn.setSortable(true);
		sortHandler.setComparator(statusColumn, new Comparator<T>() {

			@Override
			public int compare(T o1, T o2) {
				return ((AppInfoGwt) o1).appStatus
						.compareTo(((AppInfoGwt) o2).appStatus);
			}

		});
		dataGrid.addColumn(statusColumn, "Status");
		dataGrid.setColumnWidth(statusColumn, "60px");

		//--------------------------- Submitter -------------------------------
		final Column<T, String> submitterColumn = new Column<T, String>(
				new TextCell()) {

			@Override
			public String getValue(T object) {
				return ((AppInfoGwt) object).userName;
			}

		};
		submitterColumn.setSortable(true);
		sortHandler.setComparator(submitterColumn, new Comparator<T>() {

			@Override
			public int compare(T o1, T o2) {
				return ((AppInfoGwt) o1).userName
						.compareTo(((AppInfoGwt) o2).userName);
			}

		});
		submitterColumn
		.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
		dataGrid.addColumn(submitterColumn, "User");
		dataGrid.setColumnWidth(submitterColumn, "60px");

		//--------------------------- Submit Time ------------------------------
		final Column<T, String> submitTimeColumn = new Column<T, String>(
				new TextCell()) {

			@Override
			public String getValue(T object) {

				final AppInfoGwt appInfo = (AppInfoGwt) object;
				final Date date = new Date(appInfo.submitTime);
				final String dateString = dateTimeFormat.format(date);
				return dateString;
			}

		};
		submitTimeColumn.setSortable(true);
		sortHandler.setComparator(submitTimeColumn, new Comparator<T>() {

			@Override
			public int compare(T o1, T o2) {
				final AppInfoGwt appInfo1 = (AppInfoGwt) o1;
				final Date date1 = new Date(appInfo1.submitTime);
				final String dateString1 = dateTimeFormat.format(date1);
				final AppInfoGwt appInfo2 = (AppInfoGwt) o2;
				final Date date2 = new Date(appInfo2.submitTime);
				final String dateString2 = dateTimeFormat.format(date2);
				return dateString1.compareTo(dateString2);
			}

		});
		submitTimeColumn
		.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
		dataGrid.addColumn(submitTimeColumn, "Date/Time");
		dataGrid.setColumnWidth(submitTimeColumn, "100px");
	}

	public void setAppVetHostUrl(String appVetHostUrl) {
		this.appVetHostUrl = appVetHostUrl;
	}
}
