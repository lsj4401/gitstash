/*
 * Copyright (C) ZUM internet Corp., All rights reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */

package application.fileview;

import java.io.File;

public class LocalNode {
  private File file;

  public LocalNode(File file) {
    this.file = file;
  }

  public Type getType() {
    return file.isDirectory() ? Type.FOLDER : Type.FILE;
  }

  public String getPullPath() {
    return file.getPath();
  }

  @Override
  public String toString() {
    return file.getName();
  }
}
