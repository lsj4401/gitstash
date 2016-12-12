/*
 * Copyright (C) ZUM internet Corp., All rights reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */

package application.ignore;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.util.StringUtils;

public class FileFilter {
  private List<String> patterns = new ArrayList<>();

  public FileFilter(String ignorePath) {
    patterns.add(".git");
    try(BufferedReader in = new BufferedReader(new FileReader(ignorePath))) {
      String line;
      while ((line = in.readLine()) != null) {
        if (StringUtils.hasText(line)) {
          patterns.add(line.replaceAll("\\*", "\\\\*"));
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public boolean isIgnore(String path) {
//    for (String regex : patterns) {
//      if (Pattern.matches(regex, path)) {
//        return true;
//      }
//    }
    return false;
  }

}

