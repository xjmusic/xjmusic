package io.xj.gui.controllers.content.program;

import javafx.scene.control.Button;
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
}
