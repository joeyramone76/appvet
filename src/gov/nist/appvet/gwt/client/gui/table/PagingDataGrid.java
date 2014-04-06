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
package gov.nist.appvet.gwt.client.gui.table;

import gov.nist.appvet.gwt.shared.AppInfoGwt;
import gov.nist.appvet.gwt.shared.UserInfoGwt;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.SimplePager.TextLocation;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.view.client.ListDataProvider;

/**
 * Abstract PaggingDataGrid class to set initial GWT DataGrid and Simple Pager
 * with ListDataProvider
 * 
 * @author Ravi Soni
 * 
 *         $$Id: PagingDataGrid.java 38562 2014-03-30 16:20:25Z steveq $$
 */
public abstract class PagingDataGrid<T> extends Composite {

	public DataGrid<T> dataGrid;
	private final SimplePager pager;
	private String height;
	public ListDataProvider<T> dataProvider;
	private final DockPanel dock = new DockPanel();
	public int iconVersion = 0;

	public PagingDataGrid() {
		initWidget(dock);
		dataGrid = new DataGrid<T>();
		dataGrid.setWidth("100%");
		final SimplePager.Resources pagerResources = GWT
				.create(SimplePager.Resources.class);
		pager = new SimplePager(TextLocation.CENTER, pagerResources, false, 0,
				true);
		pager.setDisplay(dataGrid);
		dataProvider = new ListDataProvider<T>();
		dataProvider.setList(new ArrayList<T>());
		dataGrid.setEmptyTableWidget(new HTML("No Data to Display"));
		final ListHandler<T> sortHandler = new ListHandler<T>(
				dataProvider.getList());
		initTableColumns(dataGrid, sortHandler);
		dataGrid.addColumnSortHandler(sortHandler);
		dataProvider.addDataDisplay(dataGrid);
		pager.setVisible(true);
		dataGrid.setVisible(true);
		dock.add(dataGrid, DockPanel.CENTER);
		dock.add(pager, DockPanel.SOUTH);
		dock.setWidth("100%");
		dock.setCellWidth(dataGrid, "100%");
		dock.setCellWidth(pager, "100%");
	}

	public void add(int index, T element) {
		final List<T> list = dataProvider.getList();
		list.add(index, element);
	}

	public synchronized List<T> deleteUser(String username) {
		final List<T> list = dataProvider.getList();
		for (int i = 0; i < list.size(); i++) {
			final UserInfoGwt appInfoGwt = (UserInfoGwt) list.get(i);
			if (appInfoGwt.getUserName().equals(username)) {
				list.remove(i);
				break;
			}
		}
		return list;
	}

	public ListDataProvider<T> getDataProvider() {
		return dataProvider;
	}

	public String getHeight() {
		return height;
	}

	public abstract void initTableColumns(DataGrid<T> dataGrid,
			ListHandler<T> sortHandler);

	public void remove(int index) {
		final List<T> list = dataProvider.getList();
		list.remove(index);
	}

	public void remove(String appid) {
		final List<T> list = dataProvider.getList();
		for (int i = 0; i < list.size(); i++) {
			final AppInfoGwt appInfoGwt = (AppInfoGwt) list.get(i);
			if (appInfoGwt.appId.equals(appid)) {
				list.remove(i);
				break;
			}
		}
	}

	public void set(int index, T element) {
		final List<T> list = dataProvider.getList();
		list.set(index, element);
	}

	public void clearDataList() {
		final List<T> list = dataProvider.getList();
		list.clear();
		dataProvider.refresh();
	}

	public void setDataList(List<T> dataList) {
		// Note that list can switch between all apps and searched apps
		final List<T> list = dataProvider.getList();
		list.clear();
		list.addAll(dataList);
		dataProvider.refresh();
	}

	public void setDataProvider(ListDataProvider<T> dataProvider) {
		this.dataProvider = dataProvider;
	}

	public void setEmptyTableWidget() {
		dataGrid.setEmptyTableWidget(new HTML(
				"The current request has taken longer than the allowed time limit. Please try your report query again."));
	}

	@Override
	public void setHeight(String height) {
		this.height = height;
		dataGrid.setHeight(height);
	}
}
