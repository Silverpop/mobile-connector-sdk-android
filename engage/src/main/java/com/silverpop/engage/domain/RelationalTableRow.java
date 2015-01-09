package com.silverpop.engage.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lindsay Thurmond on 1/9/15.
 */
public class RelationalTableRow {

    public class Column {
        private String name;
        private Object value;

        public Column(String name, Object value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public Object getValue() {
            return value;
        }
    }

    List<Column> columns;

    public RelationalTableRow() {
    }

    public void addColumn(String name, Object value) {
        if (columns == null) {
            columns = new ArrayList<Column>();
        }
        columns.add(new Column(name, value));
    }

    public List<Column> getColumns() {
        return columns;
    }
}
