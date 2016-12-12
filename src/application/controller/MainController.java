/*
 * Copyright (C) ZUM internet Corp., All rights reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */

package application.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.springframework.util.StringUtils;

import application.Main;
import application.fileview.GitNode;
import application.fileview.LocalNode;
import application.ignore.FileFilter;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.DirectoryChooser;

public class MainController {
  public GridPane mainWindow;
  public MenuItem folderSelect;
  public TreeView localFileView;
  public TreeView stashFileView;
  public TextFlow sourceText;
  public TextField searchTextField;

  private Repository repo;
  private File rootDir;
  private FileFilter ignoreFilter;

  private GitNode selectedGitNode;

  private List<TreeItem> searchedItem = new ArrayList<>();
  private Map<String, GitNode> stashFileMap = new HashMap<>();

  private LoggerController loggerController;

  public MainController() throws IOException {
//    FXMLLoader fxmlLoader = FXMLLoader.load(Main.class.getResource("logView.fxml"));
    loggerController = Main.loggerController;
  }

  public void folderSelectAction(ActionEvent actionEvent) throws IOException {
    rootDir = getChooseFolder();
    ignoreFilter = new FileFilter(rootDir.getPath() + File.separator + ".gitignore");
    setLocalTreeView(rootDir, null);
    this.repo = new FileRepositoryBuilder().setGitDir(new File(rootDir.toString() + File.separator + ".git")).build();
    localFileViewAddListener();

    loggerController.addLogMessage("레파지토리 설정 " + rootDir.getPath());
  }

  private File getChooseFolder() {
    DirectoryChooser directoryChooser = new DirectoryChooser();
    directoryChooser.setTitle("폴더 선택");
    loggerController.addLogMessage("git 레파지토리 선택 완료.");
    return directoryChooser.showDialog(Main.stage);
  }

  private void setLocalTreeView(File dir, TreeItem<LocalNode> parent) {
    TreeItem<LocalNode> root = new TreeItem<>(new LocalNode(dir));
    root.setExpanded(true);
    try {
      File[] files = dir.listFiles();
      for (File file : files) {
        if (ignoreFilter.isIgnore(file.getPath())) {
          continue;
        }

        if (file.isDirectory()) {
          setLocalTreeView(file, root);
        } else {
          root.getChildren().add(new TreeItem<>(new LocalNode(file)));
        }
      }
      if (parent == null) {
        localFileView.setRoot(root);
      } else {
        parent.getChildren().add(root);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void localFileViewAddListener() {
    localFileView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
      TreeItem<LocalNode> selectedItem = (TreeItem<LocalNode>) newValue;
      System.out.println("Selected Text : " + selectedItem.getValue().getPullPath());
    });
  }

  public void clickAllStash(MouseEvent mouseEvent) throws IOException {
    try (Git git = new Git(repo)) {
      Status status = git.status().call();
      Set<String> unTracked = status.getUntracked();
      int count = 0;
      try (FileWriter fw = new FileWriter("log.txt")) {
        for (String unTrack : unTracked) {
          String sourceText = getSourceText(unTrack);
          git.add().addFilepattern(unTrack).call();
          RevCommit stash = git.stashCreate().call();
          fw.write(unTrack + "\t" + stash.getName() + "\n");

          stashFileMap.put(unTrack, new GitNode(unTrack, stash.getName(), sourceText));
          count += 1;
        }
        setGitTreeView(stashFileMap);
      }
      loggerController.addLogMessage("AllStash 완료 총" + count + "개");
    } catch (GitAPIException e) {
      e.printStackTrace();
    }
  }

  private void gitFileViewAddListener() {
    stashFileView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
      TreeItem<GitNode> selectedItem = (TreeItem<GitNode>) newValue;
      if (selectedItem == null) {
        return;
      }
      selectedGitNode = selectedItem.getValue();
      Text text = new Text(selectedGitNode.getText());
      sourceText.getChildren().clear();
      sourceText.getChildren().add(text);
      sourceText.requestLayout();
    });
  }

  public void moveLocalAction(ActionEvent actionEvent) {
    try (Git git = new Git(repo)) {
      ObjectId applyStash = git.stashApply().setStashRef(selectedGitNode.getStashName()).call();
      Collection<RevCommit> stashList = git.stashList().call();
      git.stashDrop().setStashRef(getStashIdx(stashList)).call();
      stashFileMap.remove(selectedGitNode.toString());
      setGitTreeView(stashFileMap);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private int getStashIdx(Collection<RevCommit> stashList) {
    int stashIdx = 0;
    for (RevCommit revCommit : stashList) {
      if (revCommit.getName().equals(selectedGitNode.getStashName())) {
        return stashIdx;
      }
      stashIdx += 1;
    }
    return 0;
  }

  public void moveStashAction(ActionEvent actionEvent) {
    try (Git git = new Git(repo)) {
      Collection<RevCommit> stashList = git.stashList().call();
      for (RevCommit revCommit : stashList) {
        git.stashApply().setStashRef(revCommit.getName()).call();
      }
      git.stashDrop().setAll(true).call();
      stashFileMap.clear();
      setGitTreeView(stashFileMap);
      loggerController.addLogMessage("apply 완료 : " + stashList.size());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void setGitTreeView(Map<String, GitNode> stashFileMap) throws IOException {
    TreeItem<GitNode> root = new TreeItem<>(new GitNode("stash", null, ""));
    root.setExpanded(true);
    for (Entry<String, GitNode> entry : stashFileMap.entrySet()) {
      root.getChildren().add(new TreeItem<>(entry.getValue()));
    }

    stashFileView.setRoot(root);
    gitFileViewAddListener();
  }

  public String getSourceText(String unTrack) {
    try (BufferedReader in = new BufferedReader(
        new FileReader(rootDir.getPath() + File.separator + unTrack.replace("/", "\\")))) {
      return IOUtils.toString(in);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return "";
  }

  private static int searchEnterCount = 0;

  public void searchAction(KeyEvent keyEvent) {
    if (keyEvent.getCharacter().equals("\r")) {
      if (searchedItem.size() > searchEnterCount) {
        selectStashTreeViewAndScroll(searchedItem, searchEnterCount);
        searchEnterCount += 1;
      }
    } else {
      searchEnterCount = 0;
      searchedItem.clear();
      String searchString = searchTextField.getText() + keyEvent.getCharacter();
      if (StringUtils.isEmpty(searchString)) {
        return;
      }
      stashFileView.getRoot().getChildren().stream().filter(item -> item instanceof TreeItem)
          .filter(item -> ((TreeItem) item).getValue().toString().contains(searchString)).forEach(item -> {
        searchedItem.add((TreeItem) item);
        ((TreeItem<GitNode>) item).getValue().setIdx(stashFileView.getRoot().getChildren().indexOf(item));
      });

      selectStashTreeViewAndScroll(searchedItem, 0);
    }
  }

  private void selectStashTreeViewAndScroll(List<TreeItem> itemList, int idx) {
    if (itemList.size() > idx) {
      TreeItem<GitNode> item = itemList.get(idx);
      stashFileView.getSelectionModel().select(item);
      stashFileView.scrollTo(item.getValue().getIndex());
    }
  }


}
