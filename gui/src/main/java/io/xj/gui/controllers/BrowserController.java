// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers;

import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.function.Consumer;

public abstract class BrowserController implements ReadyAfterBootController {
  /**
   Add a column to a table

   @param <N>      type of table
   @param table    for which to add column
   @param property of column
   @param name     of column
   */
  protected <N> void addColumn(TableView<N> table, int width, String property, String name) {
    TableColumn<N, String> nameColumn = new TableColumn<>(name);
    nameColumn.setCellValueFactory(new PropertyValueFactory<>(property));
    nameColumn.setPrefWidth(width);
    table.getColumns().add(nameColumn);
  }

  /**
   Setup the data for the libraries table.@param <N>   type of table

   @param table for which to setup data
   @param data  observable list
   */
  protected <N> void setupData(TableView<N> table, ObservableList<N> data, Consumer<N> setSelectedItem, Consumer<N> openItem) {
    table.setItems(data);
    table.setOnMousePressed(
      event -> {
        if (event.isPrimaryButtonDown())
          switch (event.getClickCount()) {
            case 1 -> setSelectedItem.accept(table.getSelectionModel().getSelectedItem());
            case 2 -> openItem.accept(table.getSelectionModel().getSelectedItem());
          }
      });
  }
}
