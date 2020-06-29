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

import com.beanit.iec61850bean.internal.mms.asn1.Data;
import com.beanit.iec61850bean.internal.mms.asn1.Identifier;
import com.beanit.iec61850bean.internal.mms.asn1.TypeDescription;
import com.beanit.iec61850bean.internal.mms.asn1.TypeDescription.Structure;
import com.beanit.iec61850bean.internal.mms.asn1.TypeDescription.Structure.Components;
import com.beanit.iec61850bean.internal.mms.asn1.TypeSpecification;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public abstract class ModelNode implements Iterable<ModelNode> {

  protected ObjectReference objectReference;
  protected Map<String, ModelNode> children;
  ModelNode parent;

  /**
   * Returns a copy of model node with all of its children. Creates new BasicDataAttribute values
   * but reuses ObjectReferences, FunctionalConstraints.
   *
   * @return a copy of model node with all of its children.
   */
  public abstract ModelNode copy();

  /**
   * Returns the child node with the given name. Will always return null if called on a logical node
   * because a logical node need the functional constraint to uniquely identify a child. For logical
   * nodes use <code>getChild(String name, Fc fc)</code> instead.
   *
   * @param name the name of the requested child node
   * @return the child node with the given name.
   */
  public ModelNode getChild(String name) {
    return getChild(name, null);
  }

  /**
   * Returns the child node with the given name and functional constraint. The fc is ignored if this
   * function is called on any model node other than logical node.
   *
   * @param name the name of the requested child node
   * @param fc the functional constraint of the requested child node
   * @return the child node with the given name and functional constrain
   */
  public ModelNode getChild(String name, Fc fc) {
    return children.get(name);
  }

  @SuppressWarnings("unchecked")
  public Collection<ModelNode> getChildren() {
    if (children == null) {
      return null;
    }
    return children.values();
  }

  protected Iterator<Iterator<? extends ModelNode>> getIterators() {
    List<Iterator<? extends ModelNode>> iterators = new ArrayList<>();
    if (children != null) {
      iterators.add(children.values().iterator());
    }
    return iterators.iterator();
  }

  /**
   * Returns the reference of the model node.
   *
   * @return the reference of the model node.
   */
  public ObjectReference getReference() {
    return objectReference;
  }

  /**
   * Returns the name of the model node.
   *
   * @return the name of the model node.
   */
  public String getName() {
    return objectReference.getName();
  }

  @Override
  public Iterator<ModelNode> iterator() {
    return children.values().iterator();
  }

  /**
   * Returns a list of all leaf nodes (basic data attributes) contained in the subtree of this model
   * node.
   *
   * @return a list of all leaf nodes (basic data attributes) contained in the subtree of this model
   *     node.
   */
  public List<BasicDataAttribute> getBasicDataAttributes() {
    List<BasicDataAttribute> subBasicDataAttributes = new ArrayList<>();
    for (ModelNode child : children.values()) {
      subBasicDataAttributes.addAll(child.getBasicDataAttributes());
    }
    return subBasicDataAttributes;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(getReference().toString());
    for (ModelNode childNode : children.values()) {
      sb.append("\n");
      sb.append(childNode.toString());
    }
    return sb.toString();
  }

  /**
   * Returns the parent node of this node.
   *
   * @return the parent node of this node.
   */
  public ModelNode getParent() {
    return parent;
  }

  void setParent(ModelNode parent) {
    this.parent = parent;
  }

  Data getMmsDataObj() {
    return null;
  }

  void setValueFromMmsDataObj(Data data) throws ServiceError {}

  TypeDescription getMmsTypeSpec() {

    Components componentsSequenceType = new Components();

    List<TypeDescription.Structure.Components.SEQUENCE> structComponents =
        componentsSequenceType.getSEQUENCE();
    for (ModelNode child : children.values()) {
      TypeSpecification typeSpecification = new TypeSpecification();
      typeSpecification.setTypeDescription(child.getMmsTypeSpec());

      TypeDescription.Structure.Components.SEQUENCE component =
          new TypeDescription.Structure.Components.SEQUENCE();
      component.setComponentName(new Identifier(child.getName().getBytes(UTF_8)));
      component.setComponentType(typeSpecification);

      structComponents.add(component);
    }

    Structure structure = new Structure();
    structure.setComponents(componentsSequenceType);

    TypeDescription typeDescription = new TypeDescription();
    typeDescription.setStructure(structure);

    return typeDescription;
  }
}
