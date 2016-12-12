/*
 * Copyright (C) ZUM internet Corp., All rights reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */

package application.controller;

import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class LoggerController {

  public TextFlow logger;
  public ScrollPane loggerScroller;
  private Scene scene;

  public void addLogMessage(String message) {
    logger.getChildren().add(new Text(message + "\n"));
    loggerScroller.setVvalue(1);
  }

  public void setScene(Scene scene) {
    this.scene = scene;
    logger = (TextFlow) this.scene.lookup("#logger");
    loggerScroller = (ScrollPane) this.scene.lookup("#loggerScroller");
  }
}
