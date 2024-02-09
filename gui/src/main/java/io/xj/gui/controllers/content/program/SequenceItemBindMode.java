package io.xj.gui.controllers.content.program;

import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SequenceItemBindMode {
  public Button deleteSequence;
  public Button addMemeButton;
  public Text sequenceName;
  private int parentPosition;

  public void setUp(VBox container, Parent root, HBox bindViewParentContainer, int parentPosition) {
    this.parentPosition = parentPosition;
    deleteSequence(container, root, bindViewParentContainer);
  }

  private void deleteSequence(VBox container, Parent root, HBox bindViewParentContainer) {
    deleteSequence.setOnAction(e -> {
      container.getChildren().remove(root);
      checkIfNextAndCurrentItemIsEmpty(bindViewParentContainer, container);
    });
  }

  public void checkIfNextAndCurrentItemIsEmpty(HBox bindViewParentContainer, VBox container) {
    if (container.getChildren().size() < 3) {
      //get last position
      int lastPosition = bindViewParentContainer.getChildren().size() - 1;
      //store current position
      int current = parentPosition;
      //if an empty item is ahead of the current empty item
      if (parentPosition == lastPosition - 1) {
        //remove the last empty item
        bindViewParentContainer.getChildren().remove(lastPosition);
        //continue removing the empty elements while moving the current position back
        while (((VBox) bindViewParentContainer.getChildren().get(current - 1)).getChildren().size() < 3) {
          bindViewParentContainer.getChildren().remove(current);
          current = current - 1;
        }
      }
    }
  }
}
