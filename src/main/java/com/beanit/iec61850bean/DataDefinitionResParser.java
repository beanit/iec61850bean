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

import com.beanit.iec61850bean.internal.mms.asn1.ConfirmedServiceResponse;
import com.beanit.iec61850bean.internal.mms.asn1.GetVariableAccessAttributesResponse;
import com.beanit.iec61850bean.internal.mms.asn1.TypeDescription;
import com.beanit.iec61850bean.internal.mms.asn1.TypeDescription.Structure.Components;
import com.beanit.iec61850bean.internal.mms.asn1.TypeSpecification;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

final class DataDefinitionResParser {

  static LogicalNode parseGetDataDefinitionResponse(
      ConfirmedServiceResponse confirmedServiceResponse, ObjectReference lnRef)
      throws ServiceError {

    if (confirmedServiceResponse.getGetVariableAccessAttributes() == null) {
      throw new ServiceError(
          ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
          "decodeGetDataDefinitionResponse: Error decoding GetDataDefinitionResponsePdu");
    }

    GetVariableAccessAttributesResponse varAccAttrs =
        confirmedServiceResponse.getGetVariableAccessAttributes();
    TypeDescription typeSpec = varAccAttrs.getTypeDescription();
    if (typeSpec.getStructure() == null) {
      throw new ServiceError(
          ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
          "decodeGetDataDefinitionResponse: Error decoding GetDataDefinitionResponsePdu");
    }

    Components structure = typeSpec.getStructure().getComponents();

    List<FcDataObject> fcDataObjects = new ArrayList<>();

    Fc fc;
    for (TypeDescription.Structure.Components.SEQUENCE fcComponent : structure.getSEQUENCE()) {
      if (fcComponent.getComponentName() == null) {
        throw new ServiceError(
            ServiceError.PARAMETER_VALUE_INAPPROPRIATE,
            "Error decoding GetDataDefinitionResponsePdu");
      }

      if (fcComponent.getComponentType().getTypeDescription().getStructure() == null) {
        throw new ServiceError(
            ServiceError.PARAMETER_VALUE_INAPPROPRIATE,
            "Error decoding GetDataDefinitionResponsePdu");
      }

      String fcString = fcComponent.getComponentName().toString();
      if (fcString.equals("LG")
          || fcString.equals("GO")
          || fcString.equals("GS")
          || fcString.equals("MS")
          || fcString.equals("US")) {
        continue;
      }

      fc = Fc.fromString(fcComponent.getComponentName().toString());
      Components subStructure =
          fcComponent.getComponentType().getTypeDescription().getStructure().getComponents();

      fcDataObjects.addAll(getFcDataObjectsFromSubStructure(lnRef, fc, subStructure));
    }

    LogicalNode ln = new LogicalNode(lnRef, fcDataObjects);

    return ln;
  }

  private static List<FcDataObject> getFcDataObjectsFromSubStructure(
      ObjectReference lnRef, Fc fc, Components components) throws ServiceError {

    List<TypeDescription.Structure.Components.SEQUENCE> structComponents = components.getSEQUENCE();
    List<FcDataObject> dataObjects = new ArrayList<>(structComponents.size());

    for (TypeDescription.Structure.Components.SEQUENCE doComp : structComponents) {
      if (doComp.getComponentName() == null) {
        throw new ServiceError(
            ServiceError.PARAMETER_VALUE_INAPPROPRIATE,
            "Error decoding GetDataDefinitionResponsePdu");
      }
      if (doComp.getComponentType().getTypeDescription() == null) {
        throw new ServiceError(
            ServiceError.PARAMETER_VALUE_INAPPROPRIATE,
            "Error decoding GetDataDefinitionResponsePdu");
      }

      ObjectReference doRef =
          new ObjectReference(lnRef + "." + doComp.getComponentName().toString());
      List<FcModelNode> children =
          getDoSubModelNodesFromSubStructure(
              doRef,
              fc,
              doComp.getComponentType().getTypeDescription().getStructure().getComponents());
      if (fc == Fc.RP) {
        dataObjects.add(new Urcb(doRef, children));
      } else if (fc == Fc.BR) {
        dataObjects.add(new Brcb(doRef, children));
      } else {
        dataObjects.add(new FcDataObject(doRef, fc, children));
      }
    }

    return dataObjects;
  }

  private static List<FcModelNode> getDoSubModelNodesFromSubStructure(
      ObjectReference parentRef, Fc fc, Components structure) throws ServiceError {

    Collection<TypeDescription.Structure.Components.SEQUENCE> structComponents =
        structure.getSEQUENCE();
    List<FcModelNode> dataObjects = new ArrayList<>(structComponents.size());

    for (TypeDescription.Structure.Components.SEQUENCE component : structComponents) {
      if (component.getComponentName() == null) {
        throw new ServiceError(
            ServiceError.PARAMETER_VALUE_INAPPROPRIATE,
            "Error decoding GetDataDefinitionResponsePdu");
      }

      String childName = component.getComponentName().toString();
      dataObjects.add(
          getModelNodesFromTypeSpecification(
              new ObjectReference(parentRef + "." + childName), fc, component.getComponentType()));
    }
    return dataObjects;
  }

  private static FcModelNode getModelNodesFromTypeSpecification(
      ObjectReference ref, Fc fc, TypeSpecification mmsTypeSpec) throws ServiceError {

    if (mmsTypeSpec.getTypeDescription().getArray() != null) {

      int numArrayElements =
          mmsTypeSpec.getTypeDescription().getArray().getNumberOfElements().intValue();
      List<FcModelNode> arrayChildren = new ArrayList<>(numArrayElements);
      for (int i = 0; i < numArrayElements; i++) {
        arrayChildren.add(
            getModelNodesFromTypeSpecification(
                new ObjectReference(ref + "(" + i + ")"),
                fc,
                mmsTypeSpec.getTypeDescription().getArray().getElementType()));
      }

      return new Array(ref, fc, arrayChildren);
    }

    if (mmsTypeSpec.getTypeDescription().getStructure() != null) {
      List<FcModelNode> children =
          getDoSubModelNodesFromSubStructure(
              ref, fc, mmsTypeSpec.getTypeDescription().getStructure().getComponents());
      return (new ConstructedDataAttribute(ref, fc, children));
    }

    // it is a single element
    BasicDataAttribute bt = convertMmsBasicTypeSpec(ref, fc, mmsTypeSpec.getTypeDescription());
    if (bt == null) {
      throw new ServiceError(
          ServiceError.PARAMETER_VALUE_INAPPROPRIATE,
          "decodeGetDataDefinitionResponse: Unknown data type received " + ref);
    }
    return (bt);
  }

  private static BasicDataAttribute convertMmsBasicTypeSpec(
      ObjectReference ref, Fc fc, TypeDescription mmsTypeSpec) throws ServiceError {

    if (mmsTypeSpec.getBool() != null) {
      return new BdaBoolean(ref, fc, null, false, false);
    }
    if (mmsTypeSpec.getBitString() != null) {
      int bitStringMaxLength = Math.abs(mmsTypeSpec.getBitString().intValue());

      if (bitStringMaxLength == 13) {
        return new BdaQuality(ref, fc, null, false);
      } else if (bitStringMaxLength == 10) {
        return new BdaOptFlds(ref, fc);
      } else if (bitStringMaxLength == 6) {
        return new BdaTriggerConditions(ref, fc);
      } else if (bitStringMaxLength == 2) {
        if (fc == Fc.CO) {
          // if name == ctlVal
          if (ref.getName().charAt(1) == 't') {
            return new BdaTapCommand(ref, fc, null, false, false);
          }
          // name == Check
          else {
            return new BdaCheck(ref);
          }
        } else {
          return new BdaDoubleBitPos(ref, fc, null, false, false);
        }
      }
      return null;
    } else if (mmsTypeSpec.getInteger() != null) {
      switch (mmsTypeSpec.getInteger().intValue()) {
        case 8:
          return new BdaInt8(ref, fc, null, false, false);
        case 16:
          return new BdaInt16(ref, fc, null, false, false);
        case 32:
          return new BdaInt32(ref, fc, null, false, false);
        case 64:
          return new BdaInt64(ref, fc, null, false, false);
        case 128:
          return new BdaInt128(ref, fc, null, false, false);
      }
    } else if (mmsTypeSpec.getUnsigned() != null) {
      switch (mmsTypeSpec.getUnsigned().intValue()) {
        case 8:
          return new BdaInt8U(ref, fc, null, false, false);
        case 16:
          return new BdaInt16U(ref, fc, null, false, false);
        case 32:
          return new BdaInt32U(ref, fc, null, false, false);
      }
    } else if (mmsTypeSpec.getFloatingPoint() != null) {
      int floatSize = mmsTypeSpec.getFloatingPoint().getFormatWidth().intValue();
      if (floatSize == 32) {
        return new BdaFloat32(ref, fc, null, false, false);
      } else if (floatSize == 64) {
        return new BdaFloat64(ref, fc, null, false, false);
      }
      throw new ServiceError(
          ServiceError.PARAMETER_VALUE_INAPPROPRIATE,
          "FLOAT of size: " + floatSize + " is not supported.");
    } else if (mmsTypeSpec.getOctetString() != null) {
      int stringSize = mmsTypeSpec.getOctetString().intValue();
      if (stringSize > 255 || stringSize < -255) {
        throw new ServiceError(
            ServiceError.PARAMETER_VALUE_INAPPROPRIATE,
            "OCTET_STRING of size: " + stringSize + " is not supported.");
      }
      return new BdaOctetString(ref, fc, null, Math.abs(stringSize), false, false);

    } else if (mmsTypeSpec.getVisibleString() != null) {
      int stringSize = mmsTypeSpec.getVisibleString().intValue();
      if (stringSize > 255 || stringSize < -255) {
        throw new ServiceError(
            ServiceError.PARAMETER_VALUE_INAPPROPRIATE,
            "VISIBLE_STRING of size: " + stringSize + " is not supported.");
      }
      return new BdaVisibleString(ref, fc, null, Math.abs(stringSize), false, false);
    } else if (mmsTypeSpec.getMMSString() != null) {
      int stringSize = mmsTypeSpec.getMMSString().intValue();
      if (stringSize > 255 || stringSize < -255) {
        throw new ServiceError(
            ServiceError.PARAMETER_VALUE_INAPPROPRIATE,
            "UNICODE_STRING of size: " + stringSize + " is not supported.");
      }
      return new BdaUnicodeString(ref, fc, null, Math.abs(stringSize), false, false);
    } else if (mmsTypeSpec.getUtcTime() != null) {
      return new BdaTimestamp(ref, fc, null, false, false);
    } else if (mmsTypeSpec.getBinaryTime() != null) {
      return new BdaEntryTime(ref, fc, null, false, false);
    }
    return null;
  }
}
