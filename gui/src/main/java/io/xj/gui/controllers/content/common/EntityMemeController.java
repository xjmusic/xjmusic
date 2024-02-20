package io.xj.gui.controllers.content.common;

import io.xj.gui.services.ProjectService;
import io.xj.hub.entity.EntityException;
import io.xj.hub.util.StringUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class EntityMemeController<M extends Serializable> {
  static final Logger LOG = LoggerFactory.getLogger(EntityMemeController.class);
  private final ApplicationContext ac;
  private final ProjectService projectService;
  private Callable<Collection<M>> doReadAll;
  private Callable<M> doCreate;
  private Consumer<M> doUpdate;

  @Value("classpath:/views/content/common/entity-meme-tag.fxml")
  private Resource memeTagFxml;

  @FXML
  public Button addMemeButton;

  @FXML
  public HBox memeTagContainer;

  public EntityMemeController(
    ApplicationContext ac,
    ProjectService projectService
  ) {
    this.ac = ac;
    this.projectService = projectService;
  }

  /**
   Set up the controller@param root      Parent

   @param doReadAll to read all memes
   @param doCreate  to create a meme
   @param doUpdate  to update a meme
   */
  public void setup(
    Callable<Collection<M>> doReadAll,
    Callable<M> doCreate,
    Consumer<M> doUpdate
  ) {
    this.doReadAll = doReadAll;
    this.doCreate = doCreate;
    this.doUpdate = doUpdate;
    renderMemes();
  }

  @FXML
  private void createMeme() {
    try {
      M meme = doCreate.call();
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
      Collection<M> memes = doReadAll.call();
      for (M meme : memes) {
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
  protected void renderMemeTag(M meme) {
    try {
      FXMLLoader loader = new FXMLLoader(memeTagFxml.getURL());
      loader.setControllerFactory(ac::getBean);
      Parent tagRoot = loader.load();
      EntityMemeTagController<M> memeTagController = loader.getController();
      memeTagController.setup(tagRoot, meme, doUpdate, (M toDelete) -> {
        projectService.deleteContent(toDelete);
        memeTagContainer.getChildren().remove(tagRoot);
      });
    } catch (IOException | EntityException e) {
      LOG.error("Error rendering Meme!\n{}", StringUtils.formatStackTrace(e));
    }
  }
}
