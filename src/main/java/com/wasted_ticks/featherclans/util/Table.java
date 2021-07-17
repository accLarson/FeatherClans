package com.wasted_ticks.featherclans.util;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Table {

    private static String delimiter = "             ";
    private List<Row> table = new LinkedList<>();
    private int columns;
    private boolean hasHeader = false;

    public Table(String... headers) {
        this.columns = headers.length;
        this.addHeader(headers);
    }

    public List<String> generate() {
        //array of two integers
        Integer[] columnWidths = new Integer[columns];

        for (Row row : table) {
            for (int i = 0; i < columns; i++) {

                if (columnWidths[i] == null) columnWidths[i] = row.texts.get(i).length();

                else if (row.texts.get(i).length() > columnWidths[i])
                    columnWidths[i] = row.texts.get(i).length();
            }
        }

        List<String> lines = new ArrayList<>();

        for (Row r : table) {
            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < columns; i++) {
                String text = r.texts.get(i);
                String spaces = spaces(columnWidths[i] - text.length());

                sb.append(text).append(spaces);

                if (i < columns - 1) sb.append(delimiter);
            }

            lines.add(sb.toString());
        }
        return lines;
    }

    private String spaces(int length) {
        String s = "";
        for (int i = 0; i < length; i++)
            s += " ";
        return s;
    }

    public void addRow(String... values) {
        Row row = new Row(values);
        table.add(row);
    }

    public void addHeader(String... values) {
        Row row = new Row(values);
        this.table.add(0, row);
        this.hasHeader = true;
    }

    private class Row {

        public List<String> texts = new ArrayList<>();

        public Row(String... texts) {
            for (String text : texts) {
                this.texts.add(text == null ? "" : text);
            }
        }
    }
}
