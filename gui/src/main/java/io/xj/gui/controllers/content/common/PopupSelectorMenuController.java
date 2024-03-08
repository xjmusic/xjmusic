package io.xj.gui.controllers.content.common;

import io.xj.hub.entity.EntityException;
import io.xj.hub.entity.EntityUtils;
import io.xj.hub.util.StringUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.controlsfx.control.SearchableComboBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Stream;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PopupSelectorMenuController {
  static final Logger LOG = LoggerFactory.getLogger(PopupSelectorMenuController.class);

  @FXML
  VBox container;

  @FXML
  SearchableComboBox<Option> selector;

  /**
   Set up the popup selector menu with the given set of options.

   @param options  the options, having fields "name" and "id"
   @param onSelect callback when a selection is made
   */
  public void setup(
    Collection<?> options,
    Consumer<UUID> onSelect
  ) {
    selector.setItems(FXCollections.observableList(
      options.stream()
        .flatMap(option -> {
          try {
            return Stream.of(new Option(option));
          } catch (Exception e) {
            LOG.error("Unable to create Option from {}! {}\n{}", option, e, StringUtils.formatStackTrace(e));
            return Stream.empty();
          }
        })
        .sorted(Comparator.comparing(Option::toString))
        .toList()
    ));

    selector.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (Objects.nonNull(newValue)) {
        onSelect.accept(newValue.getId());
        closeWindow();
      }
    });

    Platform.runLater(() -> {
      selector.requestFocus();
      selector.show();
    });
  }

  /**
   Close the window.
   */
  private void closeWindow() {
    Stage stage = (Stage) selector.getScene().getWindow();
    stage.close();
  }

  /**
   This class is used to display the selection option name in the ChoiceBox while preserving the underlying ID
   */
  public static class Option {
    private final UUID id;
    private final String name;

    public Option(Object option) throws EntityException {
      id = EntityUtils.getId(option);
      name = (String) EntityUtils.get(option, EntityUtils.NAME_KEY).orElseThrow();
    }

    @Override
    public String toString() {
      return name;
    }

    public UUID getId() {
      return id;
    }
  }
}
