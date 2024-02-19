package io.xj.gui.controllers.content.program;

import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class TimelineItemController {
    @FXML
    public AnchorPane timelineParent;

    public void setUp(int timelineItemId) {
    }
}
