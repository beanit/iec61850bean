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

import com.beanit.asn1bean.ber.types.BerBitString;
import com.beanit.iec61850bean.internal.mms.asn1.AccessResult;
import com.beanit.iec61850bean.internal.mms.asn1.Data;
import com.beanit.iec61850bean.internal.mms.asn1.Identifier;
import com.beanit.iec61850bean.internal.mms.asn1.InformationReport;
import com.beanit.iec61850bean.internal.mms.asn1.MMSpdu;
import com.beanit.iec61850bean.internal.mms.asn1.ObjectName;
import com.beanit.iec61850bean.internal.mms.asn1.UnconfirmedPDU;
import com.beanit.iec61850bean.internal.mms.asn1.UnconfirmedService;
import com.beanit.iec61850bean.internal.mms.asn1.VariableAccessSpecification;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Urcb extends Rcb {

  final HashMap<FcModelNode, BdaReasonForInclusion> membersToBeReported = new LinkedHashMap<>();
  ServerAssociation reserved = null;
  boolean enabled = false;
  private Timer integrityTimer;
  // private ScheduledFuture<?> integrityFuture = null;
  private ScheduledFuture<?> bufTmFuture = null;

  public Urcb(ObjectReference objectReference, List<FcModelNode> children) {
    super(objectReference, Fc.RP, children);
  }

  /**
   * Reserve URCB - The attribute Resv (if set to TRUE) shall indicate that the URCB is currently
   * exclusively reserved for the client that has set the value to TRUE. Other clients shall not be
   * allowed to set any attribute of that URCB.
   *
   * @return the Resv child
   */
  public BdaBoolean getResv() {
    return (BdaBoolean) children.get("Resv");
  }

  void enable() {

    for (FcModelNode dataSetMember : dataSet) {
      for (BasicDataAttribute bda : dataSetMember.getBasicDataAttributes()) {
        if (bda.dchg) {
          if (getTrgOps().isDataChange()) {
            synchronized (bda.chgRcbs) {
              bda.chgRcbs.add(this);
            }
          }
        } else if (bda.qchg) {
          if (getTrgOps().isQualityChange()) {
            synchronized (bda.chgRcbs) {
              bda.chgRcbs.add(this);
            }
          }
        }
        if (bda.dupd) {
          if (getTrgOps().isDataUpdate()) {
            synchronized (bda.dupdRcbs) {
              bda.dupdRcbs.add(this);
            }
          }
        }
      }
    }

    if (getTrgOps().isIntegrity() && !(getIntgPd().getValue() < 10l)) {
      integrityTimer = new Timer();

      integrityTimer.schedule(
          new TimerTask() {
            // integrityFuture = reserved.executor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
              synchronized (Urcb.this) {
                if (!enabled) {
                  return;
                }
                reserved.sendAnMmsPdu(getMmsReport(true, false));
              }
            }
            // }, getIntgPd().getValue(), getIntgPd().getValue(), TimeUnit.MILLISECONDS);
          },
          getIntgPd().getValue(),
          getIntgPd().getValue());
    }

    enabled = true;
  }

  void disable() {

    for (FcModelNode dataSetMember : dataSet) {
      for (BasicDataAttribute bda : dataSetMember.getBasicDataAttributes()) {
        if (bda.dchg) {
          if (getTrgOps().isDataChange()) {
            synchronized (bda.chgRcbs) {
              bda.chgRcbs.remove(this);
            }
          }
        } else if (bda.qchg) {
          if (getTrgOps().isQualityChange()) {
            synchronized (bda.chgRcbs) {
              bda.chgRcbs.remove(this);
            }
          }
        }
        if (bda.dupd) {
          if (getTrgOps().isDataUpdate()) {
            synchronized (bda.dupdRcbs) {
              bda.dupdRcbs.remove(this);
            }
          }
        }
      }
    }

    // if (integrityFuture != null) {
    // integrityFuture.cancel(false);
    // }
    if (integrityTimer != null) {
      integrityTimer.cancel();
    }

    enabled = false;
  }

  void generalInterrogation() {
    reserved.executor.execute(
        new Runnable() {
          @Override
          public void run() {
            synchronized (Urcb.this) {
              if (!enabled) {
                return;
              }
              reserved.sendAnMmsPdu(getMmsReport(false, true));
            }
          }
        });
  }

  private MMSpdu getMmsReport(boolean integrity, boolean gi) {

    InformationReport.ListOfAccessResult listOfAccessResult =
        new InformationReport.ListOfAccessResult();

    List<AccessResult> accessResults = listOfAccessResult.getAccessResult();

    AccessResult accessResult = new AccessResult();
    accessResult.setSuccess(getRptId().getMmsDataObj());
    accessResults.add(accessResult);

    accessResult = new AccessResult();
    accessResult.setSuccess(getOptFlds().getMmsDataObj());
    accessResults.add(accessResult);

    if (getOptFlds().isSequenceNumber()) {
      accessResult = new AccessResult();
      accessResult.setSuccess(getSqNum().getMmsDataObj());
      accessResults.add(accessResult);
    }
    getSqNum().setValue((short) (getSqNum().getValue() + 1));

    if (getOptFlds().isReportTimestamp()) {
      BdaEntryTime entryTime = new BdaEntryTime(null, null, null, false, false);
      entryTime.setTimestamp(System.currentTimeMillis());

      accessResult = new AccessResult();
      accessResult.setSuccess(entryTime.getMmsDataObj());
      accessResults.add(accessResult);
    }

    if (getOptFlds().isDataSetName()) {
      accessResult = new AccessResult();
      accessResult.setSuccess(getDatSet().getMmsDataObj());
      accessResults.add(accessResult);
    }

    if (getOptFlds().isConfigRevision()) {
      accessResult = new AccessResult();
      accessResult.setSuccess(getConfRev().getMmsDataObj());
      accessResults.add(accessResult);
    }

    // segmentation not supported

    List<FcModelNode> dataSetMembers = dataSet.getMembers();
    int dataSetSize = dataSetMembers.size();

    // inclusion bitstring
    byte[] inclusionStringArray = new byte[(dataSetSize - 1) / 8 + 1];

    if (integrity || gi) {

      for (int i = 0; i < dataSetSize; i++) {
        inclusionStringArray[i / 8] = (byte) (inclusionStringArray[i / 8] | 1 << (7 - i % 8));
      }
      BerBitString inclusionString = new BerBitString(inclusionStringArray, dataSetSize);

      Data data = new Data();
      data.setBitString(inclusionString);
      accessResult = new AccessResult();
      accessResult.setSuccess(data);
      accessResults.add(accessResult);

      // data reference sending not supported for now

      for (FcModelNode dataSetMember : dataSetMembers) {
        accessResult = new AccessResult();
        accessResult.setSuccess(dataSetMember.getMmsDataObj());
        accessResults.add(accessResult);
      }

      BdaReasonForInclusion reasonForInclusion = new BdaReasonForInclusion(null);
      if (integrity) {
        reasonForInclusion.setIntegrity(true);
      } else {
        reasonForInclusion.setGeneralInterrogation(true);
      }

      if (getOptFlds().isReasonForInclusion()) {
        for (int i = 0; i < dataSetMembers.size(); i++) {
          accessResult = new AccessResult();
          accessResult.setSuccess(reasonForInclusion.getMmsDataObj());
          accessResults.add(accessResult);
        }
      }

    } else {

      int index = 0;
      for (FcModelNode dataSetMember : dataSet) {
        if (membersToBeReported.get(dataSetMember) != null) {
          inclusionStringArray[index / 8] =
              (byte) (inclusionStringArray[index / 8] | 1 << (7 - index % 8));
        }
        index++;
      }
      BerBitString inclusionString = new BerBitString(inclusionStringArray, dataSetSize);

      Data data = new Data();
      data.setBitString(inclusionString);
      accessResult = new AccessResult();
      accessResult.setSuccess(data);
      accessResults.add(accessResult);

      // data reference sending not supported for now

      for (FcModelNode dataSetMember : dataSetMembers) {
        if (membersToBeReported.get(dataSetMember) != null) {
          accessResult = new AccessResult();
          accessResult.setSuccess(dataSetMember.getMmsDataObj());
          accessResults.add(accessResult);
        }
      }

      if (getOptFlds().isReasonForInclusion()) {
        for (FcModelNode dataSetMember : dataSetMembers) {
          BdaReasonForInclusion reasonForInclusion = membersToBeReported.get(dataSetMember);
          if (reasonForInclusion != null) {
            accessResult = new AccessResult();
            accessResult.setSuccess(reasonForInclusion.getMmsDataObj());
            accessResults.add(accessResult);
          }
        }
      }

      membersToBeReported.clear();
      bufTmFuture = null;
    }

    ObjectName objectName = new ObjectName();
    objectName.setVmdSpecific(new Identifier("RPT".getBytes(UTF_8)));

    VariableAccessSpecification varAccSpec = new VariableAccessSpecification();
    varAccSpec.setVariableListName(objectName);

    InformationReport infoReport = new InformationReport();
    infoReport.setVariableAccessSpecification(varAccSpec);
    infoReport.setListOfAccessResult(listOfAccessResult);

    UnconfirmedService unconfirmedService = new UnconfirmedService();
    unconfirmedService.setInformationReport(infoReport);

    UnconfirmedPDU unconfirmedPDU = new UnconfirmedPDU();
    unconfirmedPDU.setService(unconfirmedService);

    MMSpdu mmsPdu = new MMSpdu();
    mmsPdu.setUnconfirmedPDU(unconfirmedPDU);

    return mmsPdu;
  }

  @Override
  public FcDataObject copy() {
    List<FcModelNode> childCopies = new ArrayList<>(children.size());
    for (ModelNode childNode : children.values()) {
      childCopies.add((FcModelNode) childNode.copy());
    }
    Urcb urcb = new Urcb(objectReference, childCopies);
    urcb.dataSet = dataSet;
    return urcb;
  }

  void report(BasicDataAttribute bda, boolean dchg, boolean qchg, boolean dupd) {

    synchronized (this) {
      if (!enabled) {
        return;
      }

      FcModelNode memberFound = null;
      FcModelNode fcModelNode = bda;
      while (memberFound == null) {
        for (FcModelNode member : dataSet) {
          if (member == fcModelNode) {
            memberFound = fcModelNode;
            break;
          }
        }
        if (memberFound != null) {
          break;
        }
        if (!(fcModelNode.parent instanceof FcModelNode)) {
          // Unable to report Basic Data Attribute because it is not part of the referenced data set
          return;
        }
        fcModelNode = (FcModelNode) fcModelNode.parent;
      }

      BdaReasonForInclusion reasonForInclusion = membersToBeReported.get(fcModelNode);
      if (reasonForInclusion == null) {
        reasonForInclusion = new BdaReasonForInclusion(null);
        membersToBeReported.put(fcModelNode, reasonForInclusion);
      }

      if (dchg) {
        reasonForInclusion.setDataChange(true);
      }
      if (dupd) {
        reasonForInclusion.setDataUpdate(true);
      } else if (qchg) {
        reasonForInclusion.setQualityChange(true);
      }

      // if bufTmFuture is not null then it is already scheduled and will send the combined report
      if (bufTmFuture == null) {
        bufTmFuture =
            reserved.executor.schedule(
                new Runnable() {
                  @Override
                  public void run() {
                    synchronized (Urcb.this) {
                      if (!enabled) {
                        return;
                      }
                      reserved.sendAnMmsPdu(getMmsReport(false, false));
                    }
                  }
                },
                getBufTm().getValue(),
                TimeUnit.MILLISECONDS);
      }
    }
  }
}
