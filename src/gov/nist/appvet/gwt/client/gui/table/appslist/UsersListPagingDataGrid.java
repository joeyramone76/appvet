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
import gov.nist.appvet.gwt.shared.UserInfoGwt;

import java.util.Comparator;

import com.google.gwt.cell.client.TextCell;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.DataGrid;


public class UsersListPagingDataGrid<T> extends PagingDataGrid<T> {

    @Override
    public void initTableColumns(DataGrid<T> dataGrid,
	    ListHandler<T> sortHandler) {

	//------------------------- Last Name ----------------------------------
	final Column<T, String> lastNameColumn = new Column<T, String>(
		new TextCell()) {

	    @Override
	    public String getValue(T object) {
		return ((UserInfoGwt) object).getLastName();
	    }

	};
	lastNameColumn.setSortable(true);
	sortHandler.setComparator(lastNameColumn, new Comparator<T>() {

	    @Override
	    public int compare(T o1, T o2) {
		return ((UserInfoGwt) o1).getLastName().compareTo(
			((UserInfoGwt) o2).getLastName());
	    }

	});
	dataGrid.addColumn(lastNameColumn, "Last Name");
	dataGrid.setColumnWidth(lastNameColumn, "50px");

	//------------------------- First Name ---------------------------------
	final Column<T, String> firstNameColumn = new Column<T, String>(
		new TextCell()) {

	    @Override
	    public String getValue(T object) {
		return ((UserInfoGwt) object).getFirstName();
	    }

	};
	firstNameColumn.setSortable(true);
	sortHandler.setComparator(firstNameColumn, new Comparator<T>() {

	    @Override
	    public int compare(T o1, T o2) {
		return ((UserInfoGwt) o1).getFirstName().compareTo(
			((UserInfoGwt) o2).getFirstName());
	    }

	});
	dataGrid.addColumn(firstNameColumn, "First Name");
	dataGrid.setColumnWidth(firstNameColumn, "50px");

	//--------------------------- User ID ----------------------------------
	final Column<T, String> userIdColumn = new Column<T, String>(
		new TextCell()) {

	    @Override
	    public String getValue(T object) {
		return ((UserInfoGwt) object).getUserName();
	    }

	};
	userIdColumn.setSortable(true);
	sortHandler.setComparator(userIdColumn, new Comparator<T>() {

	    @Override
	    public int compare(T o1, T o2) {
		return ((UserInfoGwt) o1).getUserName().compareTo(
			((UserInfoGwt) o2).getUserName());
	    }

	});
	dataGrid.addColumn(userIdColumn, "User ID");
	dataGrid.setColumnWidth(userIdColumn, "50px");
    }
}
