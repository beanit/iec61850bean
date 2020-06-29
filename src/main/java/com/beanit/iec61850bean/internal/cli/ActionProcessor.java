/*
 * Copyright 2018 beanit
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.beanit.iec61850bean.internal.cli;

import static java.lang.System.exit;
import static java.lang.System.out;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.Map;

public class ActionProcessor {

  private static final String SEPARATOR_LINE =
      "------------------------------------------------------";

  private final BufferedReader reader;
  private final ActionListener actionListener;
  private final Map<String, Action> actionMap = new LinkedHashMap<>();
  private final Action helpAction = new Action("h", "print help message");
  private final Action quitAction = new Action("q", "quit the application");
  private volatile boolean closed = false;

  public ActionProcessor(ActionListener actionListener) {
    reader = new BufferedReader(new InputStreamReader(System.in, UTF_8));
    this.actionListener = actionListener;
  }

  public void addAction(Action action) {
    actionMap.put(action.getKey(), action);
  }

  public BufferedReader getReader() {
    return reader;
  }

  public void start() {

    actionMap.put(helpAction.getKey(), helpAction);
    actionMap.put(quitAction.getKey(), quitAction);

    printHelp();

    try {

      String actionKey;
      while (true) {

        if (closed) {
          exit(1);
          return;
        }

        System.out.println("\n** Enter action key: ");

        try {
          actionKey = reader.readLine();
        } catch (IOException e) {
          System.err.printf("%s. Application is being shut down.\n", e.getMessage());
          exit(2);
          return;
        }

        if (closed) {
          exit(1);
          return;
        }

        if (actionMap.get(actionKey) == null) {
          System.err.println("Illegal action key.\n");
          printHelp();
          continue;
        }

        if (actionKey.equals(helpAction.getKey())) {
          printHelp();
          continue;
        }

        if (actionKey.equals(quitAction.getKey())) {
          actionListener.quit();
          return;
        }

        actionListener.actionCalled(actionKey);
      }

    } catch (Exception e) {
      e.printStackTrace();
      actionListener.quit();
    } finally {
      close();
    }
  }

  private void printHelp() {
    final String message = " %s - %s\n";
    out.flush();
    out.println();
    out.println(SEPARATOR_LINE);

    for (Action action : actionMap.values()) {
      out.printf(message, action.getKey(), action.getDescription());
    }

    out.println(SEPARATOR_LINE);
  }

  public void close() {
    closed = true;
    try {
      reader.close();
    } catch (IOException ignored) {
      // if closing fails there is nothing meaningful that can be done
    }
  }
}
