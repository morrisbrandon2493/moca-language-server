package com.github.mrglassdanny.mocalanguageserver.moca.connection;

import java.util.ArrayList;

public class MocaResults {

    private static final int COLUMN_NOT_FOUND = -1;

    // metadata index info:
    // 0 - name
    // 1 - type
    // 2 - length
    private Object[][] metadata;
    private Object[][] values;

    private int getColumnIndex(String columnName) {
        for (int columnIdx = 0; columnIdx < this.metadata.length; columnIdx++) {
            if (this.metadata[columnIdx][0].toString().compareToIgnoreCase(columnName) == 0) {
                return columnIdx;
            }
        }

        return MocaResults.COLUMN_NOT_FOUND;
    }

    public ArrayList<String[]> toStringTable() {

        ArrayList<String[]> data = new ArrayList<>(this.getRowCount());

        String[] cols = new String[this.metadata.length];
        for (int i = 0; i < this.metadata.length; i++) {
            cols[i] = String.valueOf(this.metadata[i][0]);
        }

        data.add(cols);

        for (int i = 0; i < this.values.length; i++) {
            String[] row = new String[this.metadata.length];
            for (int j = 0; j < this.values[i].length; j++) {
                if (this.values[i][j] == null) {
                    row[j] = "";
                } else {
                    row[j] = String.valueOf(this.values[i][j]);
                }
            }

            data.add(row);
        }

        return data;
    }

    public int getRowCount() {
        if (this.values != null) {
            return this.values.length;
        } else {
            return 0;
        }

    }

    // Row index starts at 0!
    public String getString(int rowIdx, String columnName) {
        if (this.values.length <= rowIdx) {
            return null;
        }

        int columnIdx = this.getColumnIndex(columnName);

        if (columnIdx == MocaResults.COLUMN_NOT_FOUND) {
            return null;
        }

        Object obj = this.values[rowIdx][columnIdx];

        if (obj == null) {
            return null;
        } else if (obj instanceof String) {
            return (String) obj;
        } else {
            return String.valueOf(obj);
        }
    }

    public int getInt(int rowIdx, String columnName) {
        if (this.values.length <= rowIdx) {
            return 0;
        }

        int columnIdx = this.getColumnIndex(columnName);

        if (columnIdx == MocaResults.COLUMN_NOT_FOUND) {
            return 0;
        }

        Object obj = this.values[rowIdx][columnIdx];

        if (obj == null) {
            return 0;
        } else if (obj instanceof Number) {
            return ((Number) obj).intValue();
        } else {
            return Integer.parseInt(String.valueOf(obj));
        }
    }

    public boolean getBoolean(int rowIdx, String columnName) {
        if (this.values.length <= rowIdx) {
            return false;
        }

        int columnIdx = this.getColumnIndex(columnName);

        if (columnIdx == MocaResults.COLUMN_NOT_FOUND) {
            return false;
        }

        Object obj = this.values[rowIdx][columnIdx];

        if (obj == null) {
            return false;
        } else if (obj instanceof Boolean) {
            return (Boolean) obj;
        } else if (obj instanceof Number) {
            return ((Number) obj).intValue() != 0;
        } else if (obj instanceof String) {
            return Integer.parseInt((String) obj) != 0;
        } else {
            return false;
        }
    }

}
