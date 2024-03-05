package io.xj.gui.controllers.content.common;

import io.xj.gui.services.ProjectService;
import io.xj.hub.entity.EntityException;
import io.xj.hub.util.StringUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class EntityMemesController {
  static final Logger LOG = LoggerFactory.getLogger(EntityMemesController.class);
  private final ApplicationContext ac;
  private final ProjectService projectService;
  private Callable<Collection<?>> doReadAll;
  private Callable<Object> doCreate;
  private Consumer<Object> doUpdate;

  @Value("classpath:/views/content/common/entity-meme-tag.fxml")
  private Resource memeTagFxml;

  @FXML
  public StackPane labelContainer;

  @FXML
  public Button addMemeButton;

  @FXML
  public FlowPane memeTagContainer;

  public EntityMemesController(
      ApplicationContext ac,
      ProjectService projectService
  ) {
    this.ac = ac;
    this.projectService = projectService;
  }

  /**
   Set up the controller@param root      Parent@param showLabel

   @param doReadAll to read all memes
   @param doCreate  to create a meme
   @param doUpdate  to update a meme
   */
  public void setup(
      boolean showLabel,
      Callable<Collection<?>> doReadAll,
      Callable<Object> doCreate,
      Consumer<Object> doUpdate
  ) {
    this.doReadAll = doReadAll;
    this.doCreate = doCreate;
    this.doUpdate = doUpdate;

    labelContainer.setVisible(showLabel);
    labelContainer.setManaged(showLabel);

    renderMemes();
  }

  @FXML
  private void createMeme() {
    try {
      var meme = doCreate.call();
      renderMemeTag(meme);
    } catch (Exception e) {
      LOG.error("Error adding Meme!\n{}", StringUtils.formatStackTrace(e), e);
    }
  }

  /**
   Clear and re-render all memes
   */
  private void renderMemes() {
    try {
      memeTagContainer.getChildren().clear();
      Collection<?> memes = doReadAll.call();
      for (var meme : memes) {
        renderMemeTag(meme);
      }
    } catch (Exception e) {
      LOG.error("Error rendering Meme Tags!\n{}", StringUtils.formatStackTrace(e), e);
    }
  }

  /**
   Render a meme tag

   @param meme to render
   */
  protected void renderMemeTag(Object meme) {
    try {
      FXMLLoader loader = new FXMLLoader(memeTagFxml.getURL());
      loader.setControllerFactory(ac::getBean);
      Parent tagRoot = loader.load();
      EntityMemeTagController memeTagController = loader.getController();
      memeTagContainer.getChildren().add(tagRoot);
      memeTagController.setup(meme, doUpdate, (Object toDelete) -> {
        projectService.deleteContent(toDelete);
        memeTagContainer.getChildren().remove(tagRoot);
      });
    } catch (IOException | EntityException e) {
      LOG.error("Error rendering Meme!\n{}", StringUtils.formatStackTrace(e));
    }
  }
}
