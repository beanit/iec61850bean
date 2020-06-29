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
package com.beanit.iec61850bean;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.beanit.iec61850bean.internal.mms.asn1.AlternateAccess;
import com.beanit.iec61850bean.internal.mms.asn1.AlternateAccessSelection;
import com.beanit.iec61850bean.internal.mms.asn1.AlternateAccessSelection.SelectAccess;
import com.beanit.iec61850bean.internal.mms.asn1.AlternateAccessSelection.SelectAccess.Component;
import com.beanit.iec61850bean.internal.mms.asn1.BasicIdentifier;
import com.beanit.iec61850bean.internal.mms.asn1.Identifier;
import com.beanit.iec61850bean.internal.mms.asn1.ObjectName;
import com.beanit.iec61850bean.internal.mms.asn1.Unsigned32;
import com.beanit.iec61850bean.internal.mms.asn1.VariableDefs;
import com.beanit.iec61850bean.internal.mms.asn1.VariableSpecification;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public abstract class FcModelNode extends ModelNode {

  Fc fc;
  private VariableDefs.SEQUENCE variableDef = null;
  private ServerAssociation selected = null;
  private TimerTask task = null;

  public Fc getFc() {
    return fc;
  }

  boolean select(ServerAssociation association, Timer timer) {
    if (selected != null) {
      if (selected != association) {
        return false;
      }
    } else {
      selected = association;
      association.selects.add(this);
    }

    ModelNode sboTimeoutNode =
        association.serverModel.findModelNode(objectReference, Fc.CF).getChild("sboTimeout");

    if (sboTimeoutNode == null) {
      return true;
    }

    long sboTimeout = ((BdaInt32U) sboTimeoutNode).getValue();

    if (sboTimeout == 0) {
      return true;
    }

    class SelectResetTask extends TimerTask {
      ServerAssociation association;

      SelectResetTask(ServerAssociation association) {
        this.association = association;
      }

      @Override
      public void run() {
        synchronized (association.serverModel) {
          if (task == this) {
            task = null;
            deselectAndRemove(association);
          }
        }
      }
    }

    if (task != null) {
      task.cancel();
    }

    task = new SelectResetTask(association);
    timer.schedule(task, sboTimeout);

    return true;
  }

  void deselectAndRemove(ServerAssociation association) {
    selected = null;
    if (task != null) {
      task.cancel();
      task = null;
    }
    association.selects.remove(this);
  }

  void deselect() {
    selected = null;
    if (task != null) {
      task.cancel();
      task = null;
    }
  }

  boolean isSelected() {
    return selected != null;
  }

  boolean isSelectedBy(ServerAssociation association) {
    return selected == association;
  }

  VariableDefs.SEQUENCE getMmsVariableDef() {

    if (variableDef != null) {
      return variableDef;
    }

    AlternateAccess alternateAccess = null;

    StringBuilder preArrayIndexItemId = new StringBuilder(objectReference.get(1));
    preArrayIndexItemId.append("$");
    preArrayIndexItemId.append(fc);

    int arrayIndexPosition = objectReference.getArrayIndexPosition();
    if (arrayIndexPosition != -1) {

      for (int i = 2; i < arrayIndexPosition; i++) {
        preArrayIndexItemId.append("$");
        preArrayIndexItemId.append(objectReference.get(i));
      }

      alternateAccess = new AlternateAccess();
      List<AlternateAccess.CHOICE> subSeqOfAlternateAccess = alternateAccess.getCHOICE();
      Unsigned32 indexBerInteger =
          new Unsigned32(Integer.parseInt(objectReference.get(arrayIndexPosition)));

      if (arrayIndexPosition < (objectReference.size() - 1)) {
        // this reference points to a sub-node of an array element

        StringBuilder postArrayIndexItemId =
            new StringBuilder(objectReference.get(arrayIndexPosition + 1));

        for (int i = (arrayIndexPosition + 2); i < objectReference.size(); i++) {
          postArrayIndexItemId.append("$");
          postArrayIndexItemId.append(objectReference.get(i));
        }

        BasicIdentifier subIndexReference =
            new BasicIdentifier(postArrayIndexItemId.toString().getBytes(UTF_8));

        AlternateAccessSelection.SelectAccess subIndexReferenceSelectAccess =
            new AlternateAccessSelection.SelectAccess();
        Component component = new Component();
        component.setBasic(subIndexReference);
        subIndexReferenceSelectAccess.setComponent(component);

        AlternateAccessSelection subIndexReferenceAlternateAccessSelection =
            new AlternateAccessSelection();
        subIndexReferenceAlternateAccessSelection.setSelectAccess(subIndexReferenceSelectAccess);

        AlternateAccess.CHOICE subIndexReferenceAlternateAccessSubChoice =
            new AlternateAccess.CHOICE();
        subIndexReferenceAlternateAccessSubChoice.setUnnamed(
            subIndexReferenceAlternateAccessSelection);

        AlternateAccess subIndexReferenceAlternateAccess = new AlternateAccess();

        List<AlternateAccess.CHOICE> subIndexReferenceAlternateAccessSubSeqOf =
            subIndexReferenceAlternateAccess.getCHOICE();
        subIndexReferenceAlternateAccessSubSeqOf.add(subIndexReferenceAlternateAccessSubChoice);

        // set array index:

        AlternateAccessSelection.SelectAlternateAccess.AccessSelection indexAccessSelectionChoice =
            new AlternateAccessSelection.SelectAlternateAccess.AccessSelection();
        indexAccessSelectionChoice.setIndex(indexBerInteger);

        AlternateAccessSelection.SelectAlternateAccess indexAndLowerReferenceSelectAlternateAccess =
            new AlternateAccessSelection.SelectAlternateAccess();
        indexAndLowerReferenceSelectAlternateAccess.setAccessSelection(indexAccessSelectionChoice);
        indexAndLowerReferenceSelectAlternateAccess.setAlternateAccess(
            subIndexReferenceAlternateAccess);

        AlternateAccessSelection indexAndLowerReferenceAlternateAccessSelection =
            new AlternateAccessSelection();
        indexAndLowerReferenceAlternateAccessSelection.setSelectAlternateAccess(
            indexAndLowerReferenceSelectAlternateAccess);

        AlternateAccess.CHOICE indexAndLowerReferenceAlternateAccessChoice =
            new AlternateAccess.CHOICE();
        indexAndLowerReferenceAlternateAccessChoice.setUnnamed(
            indexAndLowerReferenceAlternateAccessSelection);

        subSeqOfAlternateAccess.add(indexAndLowerReferenceAlternateAccessChoice);

      } else {
        SelectAccess selectAccess = new SelectAccess();
        selectAccess.setIndex(indexBerInteger);

        AlternateAccessSelection alternateAccessSelection = new AlternateAccessSelection();
        alternateAccessSelection.setSelectAccess(selectAccess);

        AlternateAccess.CHOICE alternateAccessChoice = new AlternateAccess.CHOICE();
        alternateAccessChoice.setUnnamed(alternateAccessSelection);

        subSeqOfAlternateAccess.add(alternateAccessChoice);
      }

    } else {

      for (int i = 2; i < objectReference.size(); i++) {
        preArrayIndexItemId.append("$");
        preArrayIndexItemId.append(objectReference.get(i));
      }
    }

    ObjectName.DomainSpecific domainSpecificObjectName = new ObjectName.DomainSpecific();
    domainSpecificObjectName.setDomainID(new Identifier(objectReference.get(0).getBytes(UTF_8)));
    domainSpecificObjectName.setItemID(
        new Identifier(preArrayIndexItemId.toString().getBytes(UTF_8)));

    ObjectName objectName = new ObjectName();
    objectName.setDomainSpecific(domainSpecificObjectName);

    VariableSpecification varSpec = new VariableSpecification();
    varSpec.setName(objectName);

    variableDef = new VariableDefs.SEQUENCE();
    variableDef.setAlternateAccess(alternateAccess);
    variableDef.setVariableSpecification(varSpec);

    return variableDef;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(getReference().toString()).append(" [").append(fc).append("]");
    for (ModelNode childNode : children.values()) {
      sb.append("\n");
      sb.append(childNode.toString());
    }
    return sb.toString();
  }
}
