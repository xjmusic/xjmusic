// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

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
   Add a column to a table with control buttons@param <N>      type of table@param type     of table

   @param table    for which to add column
   @param onEdit   action to perform when editing an item
   @param onMove   action to perform when moving an item
   @param onClone  action to perform when cloning an item
   @param onDelete action to perform when deleting an item
   */
  protected <N> void addActionsColumn(
    Class<N> type,
    TableView<N> table,
    Consumer<N> onEdit,
    Consumer<N> onMove,
    Consumer<N> onClone,
    Consumer<N> onDelete
  ) {
    TableColumn<N, N> buttonsColumn = new TableColumn<>("Actions");
    buttonsColumn.setCellFactory(param -> new ButtonCell<>(type, onEdit, onMove, onClone, onDelete));
    buttonsColumn.setPrefWidth(100);
    table.getColumns().add(buttonsColumn);
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
            case 1 -> Platform.runLater(() -> setSelectedItem.accept(table.getSelectionModel().getSelectedItem()));
            case 2 -> Platform.runLater(() -> openItem.accept(table.getSelectionModel().getSelectedItem()));
          }
      });
  }

  /**
   Button cell for table view

   @param <N> type of table
   */
  protected static class ButtonCell<N> extends TableCell<N, N> {

    /**
     Constructor@param type

     @param onEdit   action
     @param onMove   action
     @param onClone  action
     @param onDelete action
     */
    public ButtonCell(Class<N> type, Consumer<N> onEdit, Consumer<N> onMove, Consumer<N> onClone, Consumer<N> onDelete) {
      var editButton = buildButton(String.format("Edit %s", type.getSimpleName()), "icons/pen-to-square.png", onEdit);
      var moveButton = buildButton(String.format("Move %s", type.getSimpleName()), "icons/move.png", onMove);
      var cloneButton = buildButton(String.format("Clone %s", type.getSimpleName()), "icons/copy.png", onClone);
      var deleteButton = buildButton(String.format("Delete %s", type.getSimpleName()), "icons/document-xmark.png", onDelete);
      var buttons = new HBox(editButton, moveButton, cloneButton, deleteButton);
      buttons.setSpacing(5);
      buttons.visibleProperty().bind(emptyProperty().not());
      setGraphic(buttons);
    }

    @Override
    protected void updateItem(N item, boolean empty) {
      super.updateItem(item, empty);
    }

    /**
     Build a button

     @param tooltip  text of tooltip
     @param onAction callback
     @return button
     */
    private Button buildButton(String tooltip, String imageSource, Consumer<N> onAction) {
      var button = new Button();
      button.setTooltip(new Tooltip(tooltip));
      button.getStyleClass().add("icon-button");
      var image = new ImageView(imageSource);
      image.setFitWidth(16);
      image.setFitHeight(16);
      button.setGraphic(image);
      button.setOnAction(event -> onAction.accept(getTableRow().getItem()));
      return button;
    }
  }
}
