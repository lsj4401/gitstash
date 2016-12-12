/*
 * Copyright (C) ZUM internet Corp., All rights reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */

package application.fileview;

public class GitNode {
  private String title;
  private String stashName;
  private String text;
  private int idx;

  public GitNode(String unTrack, String name, String text) {
    this.title = unTrack;
    this.stashName = name;
    this.text = text;
  }

  public String getStashName() {
    return this.stashName;
  }

  public String getText() {
    return this.text;
  }

  @Override
  public String toString() {
    return title;
  }

  public void setIdx(int idx) {
    this.idx = idx;
  }

  public int getIndex() {
    return this.idx;
  }
}
