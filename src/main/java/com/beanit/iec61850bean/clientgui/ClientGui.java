/*
 * Copyright 2011 The IEC61850bean Authors
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
package com.beanit.iec61850bean.clientgui;

import com.beanit.iec61850bean.ClientAssociation;
import com.beanit.iec61850bean.ClientSap;
import com.beanit.iec61850bean.ServerModel;
import com.beanit.iec61850bean.ServiceError;
import com.beanit.iec61850bean.clientgui.util.Counter;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

public class ClientGui extends JFrame implements ActionListener, TreeSelectionListener {

  private static final String ADDRESS_KEY = "serverAddress";
  private static final String PORT_KEY = "serverPort";
  private static final String TSEL_LOCAL_KEY = "tselLocal";
  private static final String TSEL_REMOTE_KEY = "tselRemote";
  private static final String LASTCONNECTION_FILE = "lastconnection.properties";

  private static final long serialVersionUID = -1938913902977758367L;

  private final JTextField ipTextField = new JTextField("127.0.0.1");
  private final JTextField portTextField = new JTextField("10002");
  private final JTree tree = new JTree(new DefaultMutableTreeNode("No server connected"));
  private final JPanel detailsPanel = new JPanel();
  private final GridBagLayout detailsLayout = new GridBagLayout();

  private final SettingsFrame settingsFrame = new SettingsFrame();

  private ClientAssociation association;

  private DataTreeNode selectedNode;

  public ClientGui() {
    super("IEC61850bean Client GUI");

    Properties lastConnection = new Properties();

    InputStream in = null;
    try {
      in = new FileInputStream(LASTCONNECTION_FILE);
      lastConnection.load(in);

      ipTextField.setText(lastConnection.getProperty(ADDRESS_KEY));
      portTextField.setText(lastConnection.getProperty(PORT_KEY));

      String[] tselString = lastConnection.getProperty(TSEL_LOCAL_KEY).split(",");
      byte[] tsel =
          new byte[] {
            (byte) Integer.parseInt(tselString[0]), (byte) Integer.parseInt(tselString[1])
          };
      settingsFrame.setTselLocal(tsel);

      tselString = lastConnection.getProperty(TSEL_REMOTE_KEY).split(",");
      tsel =
          new byte[] {
            (byte) Integer.parseInt(tselString[0]), (byte) Integer.parseInt(tselString[1])
          };
      settingsFrame.setTselRemote(tsel);
    } catch (Exception ex) {
      // no lastconnection.properties file found, use default.
    } finally {
      try {
        if (in != null) {
          in.close();
        }
      } catch (IOException ignored) {
        // there is nothing that can be done if closing fails
      }
    }

    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ClassNotFoundException e) {
      System.out.println("Class not found: " + e.getMessage());
    } catch (InstantiationException e) {
      System.out.println("Object not instantiated: " + e.getMessage());
    } catch (IllegalAccessException e) {
      System.out.println("Illegal acces: " + e.getMessage());
    } catch (UnsupportedLookAndFeelException e) {
      System.out.println("Unsupported LookAndFeel: " + e.getMessage());
    }

    GridBagLayout gbl = new GridBagLayout();
    setLayout(gbl);

    JPanel topPanel = new JPanel();
    topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));

    GridBagConstraints topPanelConstraint = new GridBagConstraints();
    topPanelConstraint.fill = GridBagConstraints.HORIZONTAL;
    topPanelConstraint.gridwidth = GridBagConstraints.REMAINDER;
    topPanelConstraint.gridx = 0;
    topPanelConstraint.gridy = 0;
    topPanelConstraint.insets = new Insets(5, 5, 5, 5);
    topPanelConstraint.anchor = GridBagConstraints.NORTH;
    gbl.setConstraints(topPanel, topPanelConstraint);
    add(topPanel);

    JLabel label = new JLabel("IP: ");
    topPanel.add(label);
    topPanel.add(ipTextField);
    topPanel.add(Box.createRigidArea(new Dimension(5, 0)));

    label = new JLabel("Port: ");
    topPanel.add(label);
    topPanel.add(portTextField);
    topPanel.add(Box.createRigidArea(new Dimension(5, 0)));

    JButton newServerButton = new JButton("Connect to Server");
    newServerButton.addActionListener(this);
    newServerButton.setActionCommand("Connect");
    topPanel.add(newServerButton);
    topPanel.add(Box.createRigidArea(new Dimension(5, 0)));

    JButton settingsButton = new JButton("Settings");
    settingsButton.addActionListener(this);
    settingsButton.setActionCommand("Settings");
    topPanel.add(settingsButton);

    ToolTipManager.sharedInstance().registerComponent(tree);

    tree.setCellRenderer(new DataObjectTreeCellRenderer());
    tree.setMinimumSize(new Dimension(100, 0));
    tree.addTreeSelectionListener(this);
    JScrollPane treeScrollPane = new JScrollPane(tree);
    treeScrollPane.setMinimumSize(new Dimension(100, 0));
    treeScrollPane.setVisible(true);

    GridBagConstraints treeScrollPaneConstraint = new GridBagConstraints();
    treeScrollPaneConstraint.fill = GridBagConstraints.BOTH;
    treeScrollPaneConstraint.gridx = 0;
    treeScrollPaneConstraint.gridy = 1;
    treeScrollPaneConstraint.weightx = 0.2;
    treeScrollPaneConstraint.weighty = 1;
    treeScrollPaneConstraint.insets = new Insets(5, 5, 5, 5);
    gbl.setConstraints(treeScrollPane, treeScrollPaneConstraint);
    add(treeScrollPane);

    detailsPanel.setLayout(detailsLayout);
    detailsPanel.setAlignmentY(TOP_ALIGNMENT);
    JScrollPane detailsScrollPane = new JScrollPane(detailsPanel);
    detailsPanel.setMaximumSize(detailsScrollPane.getSize());
    detailsScrollPane.setMinimumSize(new Dimension(0, 0));
    detailsScrollPane.setPreferredSize(new Dimension(200, 0));
    detailsScrollPane.setVisible(true);
    GridBagConstraints detailsScrollPaneConstraint = new GridBagConstraints();
    detailsScrollPaneConstraint.fill = GridBagConstraints.BOTH;
    detailsScrollPaneConstraint.gridx = 1;
    detailsScrollPaneConstraint.gridy = 1;
    detailsScrollPaneConstraint.weightx = 0.8;
    detailsScrollPaneConstraint.weighty = 1;
    detailsScrollPaneConstraint.insets = new Insets(5, 5, 5, 5);
    gbl.setConstraints(detailsScrollPane, detailsScrollPaneConstraint);
    add(detailsScrollPane);

    // Display the window.
    setSize(700, 500);
    setMinimumSize(new Dimension(420, 0));
    setVisible(true);
  }

  public static void main(String[] args) {
    ClientGui clientGui = new ClientGui();
    clientGui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    clientGui.setVisible(true);
  }

  @Override
  public void actionPerformed(ActionEvent arg0) {
    if ("connect".equalsIgnoreCase(arg0.getActionCommand())) {
      connect();
    } else if ("reload".equalsIgnoreCase(arg0.getActionCommand())) {
      reload();
    } else if ("write".equalsIgnoreCase(arg0.getActionCommand())) {
      write();
    } else if ("settings".equalsIgnoreCase(arg0.getActionCommand())) {
      settingsFrame.setVisible(true);
    }
  }

  @Override
  public void valueChanged(TreeSelectionEvent e) {
    detailsPanel.removeAll();
    detailsPanel.repaint();
    if (e.getNewLeadSelectionPath() != null) {
      selectedNode = (DataTreeNode) e.getNewLeadSelectionPath().getLastPathComponent();
      if (selectedNode.readable()) {
        showDataDetails(selectedNode, new Counter());

        JPanel filler = new JPanel();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = GridBagConstraints.RELATIVE;
        gbc.gridwidth = 3;
        gbc.gridheight = 1;
        gbc.weightx = 0;
        gbc.weighty = 1;
        detailsLayout.setConstraints(filler, gbc);
        detailsPanel.add(filler);

        JButton button = new JButton("Reload values");
        button.addActionListener(this);
        button.setActionCommand("reload");
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridx = 0;
        gbc.gridy = GridBagConstraints.RELATIVE;
        gbc.gridwidth = 2;
        gbc.gridheight = 1;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.anchor = GridBagConstraints.SOUTHWEST;
        gbc.insets = new Insets(0, 5, 5, 0);
        detailsLayout.setConstraints(button, gbc);
        detailsPanel.add(button);

        if (selectedNode.writable()) {
          button = new JButton("Write values");
          button.addActionListener(this);
          button.setActionCommand("write");
          gbc = new GridBagConstraints();
          gbc.fill = GridBagConstraints.NONE;
          gbc.gridx = 2;
          gbc.gridy = GridBagConstraints.RELATIVE;
          gbc.gridwidth = 1;
          gbc.gridheight = 1;
          gbc.weightx = 0;
          gbc.weighty = 0;
          gbc.anchor = GridBagConstraints.SOUTHEAST;
          gbc.insets = new Insets(0, 0, 5, 5);
          detailsLayout.setConstraints(button, gbc);
          detailsPanel.add(button);
        }
      }
    } else {
      selectedNode = null;
    }

    validate();
  }

  private void connect() {
    ClientSap clientSap = new ClientSap();

    InetAddress address = null;
    try {
      address = InetAddress.getByName(ipTextField.getText());
    } catch (UnknownHostException e1) {
      e1.printStackTrace();
      return;
    }

    int remotePort = 10002;
    try {
      remotePort = Integer.parseInt(portTextField.getText());
      if (remotePort < 1 || remotePort > 0xFFFF) {
        throw new NumberFormatException("port must be in range [1, 65535]");
      }
    } catch (NumberFormatException e) {
      e.printStackTrace();
      return;
    }

    clientSap.setTSelLocal(settingsFrame.getTselLocal());
    clientSap.setTSelRemote(settingsFrame.getTselRemote());

    try {
      association = clientSap.associate(address, remotePort, null, null);
    } catch (IOException e) {
      System.out.println("Error connecting to server: " + e.getMessage());
      return;
    }

    ServerModel serverModel;
    try {
      serverModel = association.retrieveModel();
      association.getAllDataValues();
    } catch (ServiceError e) {
      System.out.println("Service Error requesting model." + e.getMessage());
      association.close();
      return;
    } catch (IOException e) {
      System.out.println("Fatal IOException requesting model." + e.getMessage());
      return;
    }

    ServerModelParser parser = new ServerModelParser(serverModel);
    tree.setModel(new DefaultTreeModel(parser.getModelTree()));

    Properties lastConnectSettings = new Properties();
    FileOutputStream out = null;
    try {
      lastConnectSettings.setProperty(ADDRESS_KEY, ipTextField.getText());
      lastConnectSettings.setProperty(PORT_KEY, portTextField.getText());
      byte[] tsel = settingsFrame.getTselLocal();
      lastConnectSettings.setProperty(TSEL_LOCAL_KEY, tsel[0] + "," + tsel[1]);
      tsel = settingsFrame.getTselRemote();
      lastConnectSettings.setProperty(TSEL_REMOTE_KEY, tsel[0] + "," + tsel[1]);

      out = new FileOutputStream(LASTCONNECTION_FILE);
      lastConnectSettings.store(out, null);
    } catch (IOException ex) {
      System.out.println("Writing properties file failed. Reason: " + ex.getMessage());
    } finally {
      try {
        if (out != null) {
          out.close();
        }
      } catch (IOException e) {
        // nothing meaningful can be done if closing fails
      }
    }

    validate();
  }

  private void reload() {
    if (selectedNode.readable()) {
      try {
        selectedNode.reset(association);
      } catch (ServiceError e) {
        System.out.println("ServiceError on reading" + e.getMessage());
        return;
      } catch (IOException e) {
        System.out.println("IOException on reading" + e.getMessage());
        return;
      }
      validate();
    }
  }

  private void write() {
    if (selectedNode.writable()) {
      try {
        selectedNode.writeValues(association);
      } catch (ServiceError e) {
        System.out.println("ServiceError on writing" + e.getMessage());
        return;
      } catch (IOException e) {
        System.out.println("IOException on writing" + e.getMessage());
        return;
      }
      validate();
    }
  }

  private void showDataDetails(DataTreeNode node, Counter y) {
    if (node.getData() != null) {
      BasicDataBind<?> data = node.getData();
      JLabel nameLabel = data.getNameLabel();
      nameLabel.setText(nameLabel.getText() + ": ");
      addDetailsComponent(nameLabel, 0, y.getValue(), 1, 1, 0, 0);
      addDetailsComponent(data.getValueField(), 1, y.getValue(), 2, 1, 1, 0);
      y.increment();
    } else {
      for (int i = 0; i < node.getChildCount(); i++) {
        y.increment();
        DataObjectTreeNode childNode = (DataObjectTreeNode) node.getChildAt(i);
        showDataDetails(childNode, childNode.toString(), y);
      }
    }
  }

  private void showDataDetails(DataTreeNode node, String pre, Counter y) {
    if (node.getData() != null) {
      BasicDataBind<?> data = node.getData();
      JLabel nameLabel = data.getNameLabel();
      nameLabel.setText(pre + ": ");
      addDetailsComponent(nameLabel, 0, y.getValue(), 1, 1, 0, 0);
      addDetailsComponent(data.getValueField(), 1, y.getValue(), 2, 1, 1, 0);
      y.increment();
    } else {
      for (int i = 0; i < node.getChildCount(); i++) {
        y.increment();
        DataObjectTreeNode childNode = (DataObjectTreeNode) node.getChildAt(i);
        showDataDetails(childNode, pre + "." + childNode.toString(), y);
        detailsPanel.add(new JSeparator());
        addDetailsComponent(new JSeparator(), 0, y.getValue(), 3, 1, 1, 0);
      }
    }
  }

  private void addDetailsComponent(
      Component c, int x, int y, int width, int height, double weightx, double weighty) {
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.gridx = x;
    gbc.gridy = y;
    gbc.gridwidth = width;
    gbc.gridheight = height;
    gbc.weightx = weightx;
    gbc.weighty = weighty;
    gbc.anchor = GridBagConstraints.NORTH;
    gbc.insets = new Insets(3, 3, 3, 3);
    detailsLayout.setConstraints(c, gbc);
    detailsPanel.add(c);
  }
}
