/*
 * Copyright 2008-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package basilisk.javafx.scene.control;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Callback;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

import static basilisk.util.BasiliskClassUtils.requireState;
import static java.util.Objects.requireNonNull;

/**
 * @author Andres Almiray
 * @since 1.0.0
 */
public class DefaultTableViewModel<E> implements TableViewModel<E> {
    private static final String ERROR_TABLE_VIEW_NULL = "Argument 'tableView' must not be null";

    private final ObservableList<E> source;
    private final TableViewFormat<E> format;
    private final List<TableColumn<E, Object>> columns = new ArrayList<>();

    public DefaultTableViewModel(@Nonnull ObservableList<E> source, @Nonnull TableViewFormat<E> format) {
        this.source = requireNonNull(source, "Argument 'source' must not be null");
        this.format = requireNonNull(format, "Argument 'format' must not be null");
        computeColumns();
    }

    @SuppressWarnings("unchecked")
    private void computeColumns() {
        for (int i = 0; i < format.getColumnCount(); i++) {
            final String columnName = format.getColumnName(i);
            TableColumn column = new TableColumn(columnName);

            final int columnIndex = i;
            final TableCellFactory tableCellFactory = format.getTableCellFactory(i);
            if (tableCellFactory != null) {
                column.setCellFactory(new Callback() {
                    @Override
                    public Object call(Object cell) {
                        return tableCellFactory.createTableCell((TableColumn) cell);
                    }
                });
            }
            column.setCellValueFactory(new Callback() {
                @Override
                public Object call(Object cell) {
                    return format.getObservableValue((E) ((TableColumn.CellDataFeatures) cell).getValue(), columnIndex);
                }
            });

            columns.add(column);
        }
    }

    @Nonnull
    public ObservableList<E> getSource() {
        return source;
    }

    @Nonnull
    public TableViewFormat<E> getFormat() {
        return format;
    }

    @Nonnull
    @Override
    public TableColumn<E, ?> getColumnAt(int index) {
        requireState(index >= 0, "Argument 'index' must be greater or equal to zero");
        requireState(index < columns.size(), "Argument 'index' must be less than " + columns.size());
        return columns.get(index);
    }

    @Override
    public void attachTo(@Nonnull TableView<E> tableView) {
        requireNonNull(tableView, ERROR_TABLE_VIEW_NULL);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.setItems(source);
        tableView.getColumns().addAll(columns);

        int proportionalSize = 100 / columns.size();
        for (int i = 0; i < columns.size(); i++) {
            Double size = format.getColumnSize(i);
            int columnSize = size != null ? (int) (100 * size) : proportionalSize;
            TableColumn column = columns.get(i);
            column.setMaxWidth(1f * Integer.MAX_VALUE * columnSize);
        }
    }

    @Override
    public void detachFrom(@Nonnull TableView<E> tableView) {
        requireNonNull(tableView, ERROR_TABLE_VIEW_NULL);
        tableView.setItems(FXCollections.<E>emptyObservableList());
        tableView.getColumns().removeAll(columns);
    }
}
