package uk.co.raphel.railsim.configapp;/**
 * Created by johnr on 19/03/2017.
 */

import uk.co.raphel.railsim.common.TrackDiagramEntry;

import javax.swing.table.AbstractTableModel;
import java.util.List;

/**
 * * Created : 19/03/2017
 * * Author  : johnr
 **/
public class ServiceTableModel extends AbstractTableModel {

    private final String[] columnNames = {"Route Point", "Action"};
    private List<TrackDiagramEntry> trackNames; // Column 0
    private EditableTrainService service;


    public void clear() {
        service = null;
    }
    public int getColumnCount() {
        return columnNames.length;
    }

    public int getRowCount() {
        return trackNames.size();
    }

    public String getColumnName(int col) {
        return columnNames[col];
    }

    public Object getValueAt(int row, int col) {
        if(col == 0) {
            return trackNames.get(row).getName();
        }   else {
            return service.getCallingPoints().get(trackNames.get(row).getId());
        }
    }

    public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }

    public void setData(EditableTrainService service) {
        this.service = service;
        fireTableDataChanged();
    }
    public void setTrackNames(List<TrackDiagramEntry> trackNames) {
        this.trackNames = trackNames;
    }
    /*
     * Don't need to implement this method unless your table's
     * editable.
     */
    public boolean isCellEditable(int row, int col) {
        //Note that the data/cell address is constant,
        //no matter where the cell appears onscreen.
        return col >= 1;
    }

    /*
     * Don't need to implement this method unless your table's
     * data can change.
     */
    public void setValueAt(Object value, int row, int col) {
        if(col>0) {
            service.getCallingPoints().put(trackNames.get(row).getId(), (String)value);
        }
        fireTableCellUpdated(row, col);
    }
}
