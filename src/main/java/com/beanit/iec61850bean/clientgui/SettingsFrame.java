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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;

public class SettingsFrame extends JDialog implements ActionListener {

  private static final long serialVersionUID = -411845634137160667L;

  private int tselLocal1 = 0;
  private final JTextField tselLocalField1 = new JTextField(Integer.toString(tselLocal1));
  private int tselLocal2 = 0;
  private final JTextField tselLocalField2 = new JTextField(Integer.toString(tselLocal2));
  private int tselRemote1 = 0;
  private final JTextField tselRemoteField1 = new JTextField(Integer.toString(tselRemote1));
  private int tselRemote2 = 1;
  private final JTextField tselRemoteField2 = new JTextField(Integer.toString(tselRemote2));

  public SettingsFrame() {
    setModalityType(ModalityType.APPLICATION_MODAL);

    final GridBagLayout layout = new GridBagLayout();
    setLayout(layout);

    this.setSize(200, 120);
    setLocationRelativeTo(null);

    JLabel label = new JLabel("TSelLocal: ");
    GridBagConstraints constraint = new GridBagConstraints();
    constraint.gridwidth = 1;
    constraint.gridheight = 1;
    constraint.gridx = 0;
    constraint.gridy = 0;
    constraint.insets = new Insets(5, 5, 5, 5);
    constraint.anchor = GridBagConstraints.WEST;
    layout.setConstraints(label, constraint);
    add(label);

    constraint = new GridBagConstraints();
    constraint.fill = GridBagConstraints.HORIZONTAL;
    constraint.gridwidth = 1;
    constraint.gridheight = 1;
    constraint.gridx = 1;
    constraint.gridy = 0;
    constraint.weightx = 1;
    constraint.insets = new Insets(5, 5, 5, 5);
    constraint.anchor = GridBagConstraints.WEST;
    layout.setConstraints(tselLocalField1, constraint);
    add(tselLocalField1);

    constraint = new GridBagConstraints();
    constraint.fill = GridBagConstraints.HORIZONTAL;
    constraint.gridwidth = 1;
    constraint.gridheight = 1;
    constraint.gridx = 2;
    constraint.gridy = 0;
    constraint.weightx = 1;
    constraint.insets = new Insets(5, 5, 5, 5);
    constraint.anchor = GridBagConstraints.WEST;
    layout.setConstraints(tselLocalField2, constraint);
    add(tselLocalField2);

    label = new JLabel("TSelRemote: ");
    constraint = new GridBagConstraints();
    constraint.gridwidth = 1;
    constraint.gridheight = 1;
    constraint.gridx = 0;
    constraint.gridy = 1;
    constraint.insets = new Insets(5, 5, 5, 5);
    constraint.anchor = GridBagConstraints.WEST;
    layout.setConstraints(label, constraint);
    add(label);

    constraint = new GridBagConstraints();
    constraint.fill = GridBagConstraints.HORIZONTAL;
    constraint.gridwidth = 1;
    constraint.gridheight = 1;
    constraint.gridx = 1;
    constraint.gridy = 1;
    constraint.weightx = 1;
    constraint.insets = new Insets(5, 5, 5, 5);
    constraint.anchor = GridBagConstraints.WEST;
    layout.setConstraints(tselRemoteField1, constraint);
    add(tselRemoteField1);

    constraint = new GridBagConstraints();
    constraint.fill = GridBagConstraints.HORIZONTAL;
    constraint.gridwidth = 1;
    constraint.gridheight = 1;
    constraint.gridx = 2;
    constraint.gridy = 1;
    constraint.weightx = 1;
    constraint.insets = new Insets(5, 5, 5, 5);
    constraint.anchor = GridBagConstraints.WEST;
    layout.setConstraints(tselRemoteField2, constraint);
    add(tselRemoteField2);

    JButton button = new JButton("Cancel");
    button.setActionCommand("Cancel");
    button.addActionListener(this);
    constraint.gridwidth = 1;
    constraint.gridheight = 1;
    constraint.gridx = 0;
    constraint.gridy = 2;
    constraint.insets = new Insets(5, 5, 5, 5);
    constraint.anchor = GridBagConstraints.SOUTHWEST;
    layout.setConstraints(button, constraint);
    add(button);

    button = new JButton("OK");
    button.setActionCommand("Okay");
    button.addActionListener(this);
    constraint.gridwidth = 2;
    constraint.gridheight = 1;
    constraint.gridx = 1;
    constraint.gridy = 2;
    constraint.insets = new Insets(5, 5, 5, 5);
    constraint.anchor = GridBagConstraints.SOUTHWEST;
    layout.setConstraints(button, constraint);
    add(button);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if ("cancel".equalsIgnoreCase(e.getActionCommand())) {
      tselLocalField1.setText(Integer.toString(tselLocal1 & 0xFF));
      tselLocalField2.setText(Integer.toString(tselLocal2 & 0xFF));
      tselRemoteField1.setText(Integer.toString(tselRemote1 & 0xFF));
      tselRemoteField2.setText(Integer.toString(tselRemote2 & 0xFF));
      setVisible(false);
    } else if ("okay".equalsIgnoreCase(e.getActionCommand())) {
      tselLocal1 = parseTextField(tselLocalField1, tselLocal1);
      tselLocal2 = parseTextField(tselLocalField2, tselLocal2);
      tselRemote1 = parseTextField(tselRemoteField1, tselRemote1);
      tselRemote2 = parseTextField(tselRemoteField2, tselRemote2);
      setVisible(false);
    }
  }

  public byte[] getTselLocal() {
    return new byte[] {(byte) tselLocal1, (byte) tselLocal2};
  }

  public void setTselLocal(byte[] tsel) {
    if (tsel.length != 2) {
      throw new IllegalArgumentException("TSel must consist of 2 bytes");
    }
    tselLocal1 = tsel[0];
    tselLocal2 = tsel[1];

    tselLocalField1.setText(Integer.toString(tselLocal1 & 0xFF));
    tselLocalField2.setText(Integer.toString(tselLocal2 & 0xFF));
  }

  public byte[] getTselRemote() {
    return new byte[] {(byte) tselRemote1, (byte) tselRemote2};
  }

  public void setTselRemote(byte[] tsel) {
    if (tsel.length != 2) {
      throw new IllegalArgumentException("TSel must consist of 2 bytes");
    }
    tselRemote1 = tsel[0];
    tselRemote2 = tsel[1];

    tselRemoteField1.setText(Integer.toString(tselRemote1 & 0xFF));
    tselRemoteField2.setText(Integer.toString(tselRemote2 & 0xFF));
  }

  private int parseTextField(JTextField field, int oldValue) {
    int value = oldValue;
    try {
      int newValue = Integer.parseInt(field.getText());
      if (newValue >= 0 && newValue <= 255) {
        value = newValue;
      }
    } catch (NumberFormatException e) {
      return oldValue;
    }
    return value;
  }
}
