/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.hal.dmr;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;
import org.jboss.hal.dmr.stream.ModelException;
import org.jboss.hal.dmr.stream.ModelStreamFactory;
import org.jboss.hal.dmr.stream.ModelWriter;

import static org.jboss.hal.dmr.ModelDescriptionConstants.FAILURE_DESCRIPTION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.OUTCOME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SUCCESS;

/**
 * A dynamic model representation node object.
 * <p>
 * A node can be of any type specified in the {@link ModelType} enumeration.  The type can
 * be queried via {@link #getType()} and updated via any of the {@code set*()} methods.  The
 * value of the node can be acquired via the {@code as<type>()} methods, where {@code <type>} is
 * the desired value type.  If the type is not the same as the node type, a conversion is attempted between
 * the types.
 * <p>A node can be made read-only by way of its {@link #protect()} method, which will prevent
 * any further changes to the node or its sub-nodes.
 * <p>Instances of this class are <b>not</b> thread-safe and need to be synchronized externally.
 *
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
@JsType
public class ModelNode {

    private static final String VALUE_IS_NULL = "value is null";
    private static final String NEW_VALUE_IS_NULL = "newValue is null";

    private boolean protect = false;
    private ModelValue value = ModelValue.UNDEFINED;

    /**
     * Creates a new {@code ModelNode} with an undefined value.
     */
    public ModelNode() {
    }

    /**
     * Creates a new {@code ModelNode} with the given {@code value}.
     *
     * @param value the value. Cannot be {@code null}
     *
     * @throws IllegalArgumentException if {@code value} is {@code null}
     */
    @JsIgnore
    public ModelNode(BigDecimal value) {
        if (value == null) {
            throw new IllegalArgumentException(VALUE_IS_NULL);
        }
        this.value = new BigDecimalModelValue(value);
    }

    /**
     * Creates a new {@code ModelNode} with the given {@code value}.
     *
     * @param value the value. Cannot be {@code null}
     *
     * @throws IllegalArgumentException if {@code value} is {@code null}
     */
    @JsIgnore
    public ModelNode(BigInteger value) {
        if (value == null) {
            throw new IllegalArgumentException(VALUE_IS_NULL);
        }
        this.value = new BigIntegerModelValue(value);
    }

    /**
     * Creates a new {@code ModelNode} with the given {@code value}.
     *
     * @param value the value.
     */
    @JsIgnore
    public ModelNode(boolean value) {
        this(value ? BooleanModelValue.TRUE : BooleanModelValue.FALSE);
    }

    /**
     * Creates a new {@code ModelNode} with the given {@code value}.
     *
     * @param value the value. Cannot be {@code null}
     *
     * @throws IllegalArgumentException if {@code value} is {@code null}
     */
    @JsIgnore
    public ModelNode(byte[] value) {
        if (value == null) {
            throw new IllegalArgumentException(VALUE_IS_NULL);
        }
        this.value = new BytesModelValue(value);
    }

    /**
     * Creates a new {@code ModelNode} with the given {@code value}.
     *
     * @param value the value.
     */
    @JsIgnore
    public ModelNode(double value) {
        this.value = new DoubleModelValue(value);
    }

    /**
     * Creates a new {@code ModelNode} with the given {@code value}.
     *
     * @param value the value.
     */
    @JsIgnore
    public ModelNode(int value) {
        this.value = new IntModelValue(value);
    }

    /**
     * Creates a new {@code ModelNode} with the given {@code value}.
     *
     * @param value the value.
     */
    @JsIgnore
    public ModelNode(long value) {
        this.value = new LongModelValue(value);
    }

    /**
     * Creates a new {@code ModelNode} with the given {@code value}.
     *
     * @param value the value. Cannot be {@code null}
     *
     * @throws IllegalArgumentException if {@code value} is {@code null}
     */
    @JsIgnore
    public ModelNode(String value) {
        if (value == null) {
            throw new IllegalArgumentException(VALUE_IS_NULL);
        }
        this.value = new StringModelValue(value);
    }

    /**
     * Creates a new {@code ModelNode} with the given {@code value}.
     *
     * @param value the value. Cannot be {@code null}
     *
     * @throws IllegalArgumentException if {@code value} is {@code null}
     */
    @JsIgnore
    public ModelNode(ModelType value) {
        if (value == null) {
            throw new IllegalArgumentException(VALUE_IS_NULL);
        }
        this.value = TypeModelValue.of(value);
    }

    ModelNode(ModelValue value) {
        this.value = value;
    }

    /**
     * Prevent further modifications to this node and its sub-nodes.  Note that copies
     * of this node made after this method call will not be protected.
     */
    @JsIgnore
    public void protect() {
        if (!protect) {
            protect = true;
            value = value.protect();
        }
    }

    /**
     * Get the value of this node as a {@code long}.  Collection types will return the size
     * of the collection for this value.  Other types may attempt a string conversion.
     *
     * @return the long value
     *
     * @throws IllegalArgumentException if this node is not {@link #isDefined() defined} or if no conversion is possible
     */
    @JsIgnore
    public long asLong() throws IllegalArgumentException {
        return value.asLong();
    }

    /**
     * Get the value of this node as a {@code long}.  Collection types will return the size
     * of the collection for this value.  Other types may attempt a string conversion.
     *
     * @param defVal the default value to return if this node is not {@link #isDefined() defined}
     *
     * @return the long value
     *
     * @throws NumberFormatException    if this node's {@link #getType() type} is {@link ModelType#STRING} and a numeric
     *                                  conversion of the string value is not possible
     * @throws IllegalArgumentException if this node's {@link #getType() type} is one where no numeric conversion is
     *                                  possible
     */
    @JsIgnore
    public long asLong(long defVal) {
        return value.asLong(defVal);
    }

    /**
     * Get the value of this node as an {@code int}.  Collection types will return the size
     * of the collection for this value.  Other types may attempt a string conversion.
     *
     * @return the int value
     *
     * @throws IllegalArgumentException if this node is not {@link #isDefined() defined} or if no conversion is possible
     */
    public int asInt() throws IllegalArgumentException {
        return value.asInt();
    }

    /**
     * Get the value of this node as an {@code int}.  Collection types will return the size
     * of the collection for this value.  Other types may attempt a string conversion.
     *
     * @param defVal the default value to return if this node is not {@link #isDefined() defined}
     *
     * @return the int value
     *
     * @throws NumberFormatException    if this node's {@link #getType() type} is {@link ModelType#STRING} and a numeric
     *                                  conversion of the string value is not possible
     * @throws IllegalArgumentException if this node's {@link #getType() type} is one where no numeric conversion is
     *                                  possible
     */
    public int asInt(int defVal) {
        return value.asInt(defVal);
    }

    /**
     * Get the value of this node as a {@code boolean}.  Collection types return {@code true} for non-empty
     * collections.  Numerical types return {@code true} for non-zero values.
     *
     * @return the boolean value
     *
     * @throws IllegalArgumentException if this node is not {@link #isDefined() defined} or if no conversion is possible
     */
    public boolean asBoolean() throws IllegalArgumentException {
        return value.asBoolean();
    }

    /**
     * Get the value of this node as a {@code boolean}.  Collection types return {@code true} for non-empty
     * collections.  Numerical types return {@code true} for non-zero values.
     *
     * @param defVal the default value to return if this node is not {@link #isDefined() defined}
     *
     * @return the boolean value
     *
     * @throws IllegalArgumentException if this node's {@link #getType() type} is one where no numeric conversion is
     *                                  possible or if the type is {@link ModelType#STRING} and the string value is not
     *                                  equal, ignoring case, to the literal {@code true} or {@code false}
     */
    public boolean asBoolean(boolean defVal) {
        return value.asBoolean(defVal);
    }


    /**
     * Get the value as a string.  This is the literal value of this model node.  More than one node type may
     * yield the same value for this method.
     *
     * @return the string value. A node that is not {@link #isDefined() defined} returns the literal string {@code undefined}
     */
    public String asString() {
        return value.asString();
    }

    /**
     * Get the value as a string.  This is the literal value of this model node.  More than one node type may
     * yield the same value for this method.
     *
     * @param defVal the default value to return if this node is not {@link #isDefined() defined}
     *
     * @return the string value.
     */
    public String asString(String defVal) {
        return isDefined() ? value.asString() : defVal;
    }

    /**
     * Get the value of this node as a {@code double}.  Collection types will return the size
     * of the collection for this value.  Other types may attempt a string conversion.
     *
     * @return the double value
     *
     * @throws IllegalArgumentException if this node is not {@link #isDefined() defined} or if no conversion is possible
     */
    public double asDouble() throws IllegalArgumentException {
        return value.asDouble();
    }

    /**
     * Get the value of this node as an {@code double}.  Collection types will return the size
     * of the collection for this value.  Other types may attempt a string conversion.
     *
     * @param defVal the default value to return if this node is not {@link #isDefined() defined}
     *
     * @return the int value
     *
     * @throws NumberFormatException    if this node's {@link #getType() type} is {@link ModelType#STRING} and a numeric
     *                                  conversion of the string value is not possible
     * @throws IllegalArgumentException if this node's {@link #getType() type} is one where no numeric conversion is
     *                                  possible
     */
    public double asDouble(double defVal) {
        return value.asDouble(defVal);
    }

    /**
     * Get the value of this node as a type, expressed using the {@code ModelType} enum.  The string
     * value of this node must be convertible to a type.
     *
     * @return the {@code ModelType} value
     *
     * @throws IllegalArgumentException if this node is not {@link #isDefined() defined} or if no conversion is possible
     */
    @JsIgnore
    public ModelType asType() throws IllegalArgumentException {
        return value.asType();
    }

    /**
     * Get the value of this node as a {@code BigDecimal}. Collection types will return the size
     * of the collection for this value.  Other types may attempt a string conversion.
     *
     * @return the {@code BigDecimal} value
     *
     * @throws IllegalArgumentException if this node is not {@link #isDefined() defined} or if no conversion is possible
     */
    @JsIgnore
    public BigDecimal asBigDecimal() throws IllegalArgumentException {
        return value.asBigDecimal();
    }

    /**
     * Get the value of this node as a {@code BigInteger}.  Collection types will return the size
     * of the collection for this value.  Other types may attempt a string conversion.
     *
     * @return the {@code BigInteger} value
     *
     * @throws IllegalArgumentException if this node is not {@link #isDefined() defined} or if no conversion is possible
     */
    @JsIgnore
    public BigInteger asBigInteger() throws IllegalArgumentException {
        return value.asBigInteger();
    }

    /**
     * Get the value of this node as a byte array.  Strings and string-like values will return
     * the UTF-8 encoding of the string.  Numerical values will return the byte representation of the
     * number.
     *
     * @return the bytes
     *
     * @throws IllegalArgumentException if this node is not {@link #isDefined() defined} or if no conversion is possible
     */
    public byte[] asBytes() throws IllegalArgumentException {
        return value.asBytes();
    }

    /**
     * Get the value of this node as a property.  Object values will return a property if there is exactly one
     * property in the object.  List values will return a property if there are exactly two items in the list,
     * and if the first is convertible to a string.
     *
     * @return the property value
     *
     * @throws IllegalArgumentException if this node is not {@link #isDefined() defined} or if no conversion is possible
     */
    public Property asProperty() throws IllegalArgumentException {
        return value.asProperty();
    }

    /**
     * Get the value of this node as a property list.  Object values will return a list of properties representing
     * each key-value pair in the object.  List values will return all the values of the list, failing if any of the
     * values are not convertible to a property value.
     *
     * @return the property list value
     *
     * @throws IllegalArgumentException if this node is not {@link #isDefined() defined} or if no conversion is possible
     */
    public List<Property> asPropertyList() throws IllegalArgumentException {
        return value.asPropertyList();
    }

    /**
     * Get a copy of this value as an object.  Object values will simply copy themselves as by the {@link #clone()}
     * method.
     * Property values will return a single-entry object whose key and value are copied from the property key and
     * value.
     * List values will attempt to interpolate the list into an object by iterating each item, mapping each property
     * into an object entry and otherwise taking pairs of list entries, converting the first to a string, and using the
     * pair of entries as a single object entry.  If an object key appears more than once in the source object, the
     * last
     * key takes precedence.
     *
     * @return the object value
     *
     * @throws IllegalArgumentException if this node is not {@link #isDefined() defined} or if no conversion is possible
     */
    public ModelNode asObject() throws IllegalArgumentException {
        return value.asObject();
    }

    /**
     * Get the value of this node as {@code type}. This method simply delegates to the various {@code asXXX()}
     * methods depending on the specified type.
     *
     * @return the value as {@code type} or null if the type is {@link ModelType#UNDEFINED}
     *
     * @throws IllegalArgumentException if no conversion is possible
     */
    @JsIgnore
    public Object as(ModelType type) {
        Object result;
        switch (type) {
            case BIG_DECIMAL:
                result = asBigDecimal();
                break;
            case BIG_INTEGER:
                result = asBigInteger();
                break;
            case BOOLEAN:
                result = asBoolean();
                break;
            case BYTES:
                result = asBytes();
                break;
            case DOUBLE:
                result = asDouble();
                break;
            case EXPRESSION:
                result = asString();
                break;
            case INT:
                result = asInt();
                break;
            case LIST:
                result = asList();
                break;
            case LONG:
                result = asLong();
                break;
            case OBJECT:
                result = asObject();
                break;
            case PROPERTY:
                result = asProperty();
                break;
            case STRING:
                result = asString();
                break;
            case TYPE:
                result = asType();
                break;
            case UNDEFINED:
                result = null;
                break;
            default:
                result = null;
                break;
        }
        return result;
    }

    /**
     * Determine whether this node is defined.  Equivalent to the expression: {@code getType() != ModelType.UNDEFINED}.
     *
     * @return {@code true} if this node's value is defined
     */
    public boolean isDefined() {
        return getType() != ModelType.UNDEFINED;
    }

    /**
     * Change this node's value to the given value.
     *
     * @param newValue the new value
     *
     * @return this node
     */
    @JsMethod(name = "setInt")
    public ModelNode set(int newValue) {
        checkProtect();
        value = new IntModelValue(newValue);
        return this;
    }

    /**
     * Change this node's value to the given value.
     *
     * @param newValue the new value
     *
     * @return this node
     */
    @JsIgnore
    public ModelNode set(long newValue) {
        checkProtect();
        value = new LongModelValue(newValue);
        return this;
    }

    /**
     * Change this node's value to the given value.
     *
     * @param newValue the new value
     *
     * @return this node
     */
    @JsMethod(name = "setDouble")
    public ModelNode set(double newValue) {
        checkProtect();
        value = new DoubleModelValue(newValue);
        return this;
    }

    /**
     * Change this node's value to the given value.
     *
     * @param newValue the new value
     *
     * @return this node
     */
    @JsMethod(name = "setBoolean")
    public ModelNode set(boolean newValue) {
        checkProtect();
        value = BooleanModelValue.valueOf(newValue);
        return this;
    }

    /**
     * Change this node's value to the given expression value.
     *
     * @param newValue the new value
     *
     * @return this node
     *
     * @deprecated Use {@link #set(ValueExpression)} instead.
     */
    public ModelNode setExpression(String newValue) {
        if (newValue == null) {
            throw new IllegalArgumentException(NEW_VALUE_IS_NULL);
        }
        checkProtect();
        value = new ExpressionValue(newValue);
        return this;
    }

    /**
     * Change this node's value to the given value.
     *
     * @param newValue the new value
     *
     * @return this node
     */
    @JsIgnore
    public ModelNode set(ValueExpression newValue) {
        if (newValue == null) {
            throw new IllegalArgumentException(NEW_VALUE_IS_NULL);
        }
        checkProtect();
        value = new ExpressionValue(newValue);
        return this;
    }

    /**
     * Change this node's value to the given value.
     *
     * @param newValue the new value
     *
     * @return this node
     */
    @JsMethod(name = "setString")
    public ModelNode set(String newValue) {
        if (newValue == null) {
            throw new IllegalArgumentException(NEW_VALUE_IS_NULL);
        }
        checkProtect();
        value = new StringModelValue(newValue);
        return this;
    }

    /**
     * Change this node's value to the given value.
     *
     * @param newValue the new value
     *
     * @return this node
     */
    @JsIgnore
    public ModelNode set(BigDecimal newValue) {
        if (newValue == null) {
            throw new IllegalArgumentException(NEW_VALUE_IS_NULL);
        }
        checkProtect();
        value = new BigDecimalModelValue(newValue);
        return this;
    }

    /**
     * Change this node's value to the given value.
     *
     * @param newValue the new value
     *
     * @return this node
     */
    @JsIgnore
    public ModelNode set(BigInteger newValue) {
        if (newValue == null) {
            throw new IllegalArgumentException(NEW_VALUE_IS_NULL);
        }
        checkProtect();
        value = new BigIntegerModelValue(newValue);
        return this;
    }

    /**
     * Change this node's value to the given value.  The value is copied from the parameter.
     *
     * @param newValue the new value
     *
     * @return this node
     */
    @JsMethod(name = "setNode")
    public ModelNode set(ModelNode newValue) {
        if (newValue == null) {
            throw new IllegalArgumentException(NEW_VALUE_IS_NULL);
        }
        checkProtect();
        value = newValue.value.copy();
        return this;
    }

    /**
     * Change this node's value to the given value.
     *
     * @param newValue the new value
     *
     * @return this node
     */
    @JsIgnore
    public ModelNode set(byte[] newValue) {
        if (newValue == null) {
            throw new IllegalArgumentException(NEW_VALUE_IS_NULL);
        }
        checkProtect();
        value = new BytesModelValue(newValue.length == 0 ? newValue : newValue.clone());
        return this;
    }

    /**
     * Change this node's value to the given value.
     *
     * @param newValue the new value
     *
     * @return this node
     */
    @JsIgnore
    public ModelNode set(ModelType newValue) {
        if (newValue == null) {
            throw new IllegalArgumentException(NEW_VALUE_IS_NULL);
        }
        checkProtect();
        value = TypeModelValue.of(newValue);
        return this;
    }

    /**
     * Change this node's value to the given value.
     *
     * @param newValue the new value
     *
     * @return this node
     */
    @JsMethod(name = "setProperty")
    public ModelNode set(Property newValue) {
        if (newValue == null) {
            throw new IllegalArgumentException(NEW_VALUE_IS_NULL);
        }
        set(newValue.getName(), newValue.getValue());
        return this;
    }

    /**
     * Change this node's value to a property with the given name and value.
     *
     * @param propertyName  the property name
     * @param propertyValue the property value
     *
     * @return this node
     */
    @JsIgnore
    public ModelNode set(String propertyName, ModelNode propertyValue) {
        checkProtect();
        value = new PropertyModelValue(propertyName, propertyValue, true);
        return this;
    }

    /**
     * Change this node's value to a property with the given name and value.
     *
     * @param propertyName  the property name
     * @param propertyValue the property value
     *
     * @return this node
     */
    @JsIgnore
    public ModelNode set(String propertyName, int propertyValue) {
        checkProtect();
        ModelNode node = new ModelNode();
        node.set(propertyValue);
        value = new PropertyModelValue(propertyName, node);
        return this;
    }

    /**
     * Change this node's value to a property with the given name and value.
     *
     * @param propertyName  the property name
     * @param propertyValue the property value
     *
     * @return this node
     */
    @JsIgnore
    public ModelNode set(String propertyName, long propertyValue) {
        checkProtect();
        ModelNode node = new ModelNode();
        node.set(propertyValue);
        value = new PropertyModelValue(propertyName, node);
        return this;
    }

    /**
     * Change this node's value to a property with the given name and value.
     *
     * @param propertyName  the property name
     * @param propertyValue the property value
     *
     * @return this node
     */
    @JsIgnore
    public ModelNode set(String propertyName, double propertyValue) {
        checkProtect();
        ModelNode node = new ModelNode();
        node.set(propertyValue);
        value = new PropertyModelValue(propertyName, node);
        return this;
    }

    /**
     * Change this node's value to a property with the given name and value.
     *
     * @param propertyName  the property name
     * @param propertyValue the property value
     *
     * @return this node
     */
    @JsIgnore
    public ModelNode set(String propertyName, boolean propertyValue) {
        checkProtect();
        ModelNode node = new ModelNode();
        node.set(propertyValue);
        value = new PropertyModelValue(propertyName, node);
        return this;
    }

    /**
     * Change this node's value to a property with the given name and value.
     *
     * @param propertyName  the property name
     * @param propertyValue the property value
     *
     * @return this node
     */
    @JsIgnore
    public ModelNode set(String propertyName, String propertyValue) {
        checkProtect();
        ModelNode node = new ModelNode();
        node.set(propertyValue);
        value = new PropertyModelValue(propertyName, node);
        return this;
    }

    /**
     * Change this node's value to a property with the given name and expression value.
     *
     * @param propertyName  the property name
     * @param propertyValue the property expression value
     *
     * @return this node
     *
     * @deprecated Use {@link #set(String, ValueExpression)} instead.
     */
    @JsIgnore
    @Deprecated
    public ModelNode setExpression(String propertyName, String propertyValue) {
        checkProtect();
        ModelNode node = new ModelNode();
        node.set(new ValueExpression(propertyValue));
        value = new PropertyModelValue(propertyName, node);
        return this;
    }

    /**
     * Change this node's value to a property with the given name and value.
     *
     * @param propertyName  the property name
     * @param propertyValue the property value
     *
     * @return this node
     */
    @JsIgnore
    public ModelNode set(String propertyName, ValueExpression propertyValue) {
        checkProtect();
        ModelNode node = new ModelNode();
        node.set(propertyValue);
        value = new PropertyModelValue(propertyName, node);
        return this;
    }

    /**
     * Change this node's value to a property with the given name and value.
     *
     * @param propertyName  the property name
     * @param propertyValue the property value
     *
     * @return this node
     */
    @JsIgnore
    public ModelNode set(String propertyName, BigDecimal propertyValue) {
        checkProtect();
        ModelNode node = new ModelNode();
        node.set(propertyValue);
        value = new PropertyModelValue(propertyName, node);
        return this;
    }

    /**
     * Change this node's value to a property with the given name and value.
     *
     * @param propertyName  the property name
     * @param propertyValue the property value
     *
     * @return this node
     */
    @JsIgnore
    public ModelNode set(String propertyName, BigInteger propertyValue) {
        checkProtect();
        ModelNode node = new ModelNode();
        node.set(propertyValue);
        value = new PropertyModelValue(propertyName, node);
        return this;
    }

    /**
     * Change this node's value to a property with the given name and value.
     *
     * @param propertyName  the property name
     * @param propertyValue the property value
     *
     * @return this node
     */
    @JsIgnore
    public ModelNode set(String propertyName, byte[] propertyValue) {
        checkProtect();
        ModelNode node = new ModelNode();
        node.set(propertyValue);
        value = new PropertyModelValue(propertyName, node);
        return this;
    }

    /**
     * Change this node's value to a property with the given name and value.
     *
     * @param propertyName  the property name
     * @param propertyValue the property value
     *
     * @return this node
     */
    @JsIgnore
    public ModelNode set(String propertyName, ModelType propertyValue) {
        checkProtect();
        ModelNode node = new ModelNode();
        node.set(propertyValue);
        value = new PropertyModelValue(propertyName, node);
        return this;
    }

    /**
     * Change this node's value to a list whose values are copied from the given collection.
     *
     * @param newValue the list value
     *
     * @return this node
     */
    @JsIgnore
    public ModelNode set(Collection<ModelNode> newValue) {
        if (newValue == null) {
            throw new IllegalArgumentException(NEW_VALUE_IS_NULL);
        }
        checkProtect();
        ArrayList<ModelNode> list = new ArrayList<>(newValue.size());
        for (ModelNode node : newValue) {
            if (node == null) {
                list.add(new ModelNode());
            } else {
                list.add(node.clone());
            }
        }
        value = new ListModelValue(list);
        return this;
    }

    /**
     * Change this node's value to an empty list.
     *
     * @return this node
     */
    public ModelNode setEmptyList() {
        checkProtect();
        value = new ListModelValue();
        return this;
    }

    /**
     * Change this node's value to an empty object.
     *
     * @return this node
     */
    public ModelNode setEmptyObject() {
        checkProtect();
        value = new ObjectModelValue();
        return this;
    }

    /**
     * Clear this node's value and change its type to {@link ModelType#UNDEFINED}.
     *
     * @return this node
     */
    public ModelNode clear() {
        checkProtect();
        value = ModelValue.UNDEFINED;
        return this;
    }

    /**
     * Get the child of this node with the given name.  If no such child exists, create it.  If the node is undefined,
     * it will be initialized to be of type {@link ModelType#OBJECT}.
     * <p>
     * When called on property values, the name must match the property name.
     *
     * @param name the child name
     *
     * @return the child
     *
     * @throws IllegalArgumentException if this node does not support getting a child with the given name
     */
    public ModelNode get(String name) {
        ModelValue value = this.value;
        if (value == ModelValue.UNDEFINED) {
            checkProtect();
            return (this.value = new ObjectModelValue()).getChild(name);
        }
        return value.getChild(name);
    }

    /**
     * Require the existence of a child of this node with the given name, returning the child.  If no such child exists,
     * an exception is thrown.
     * <p>
     * When called on property values, the name must match the property name.
     *
     * @param name the child name
     *
     * @return the child
     *
     * @throws NoSuchElementException if the element does not exist
     */
    @JsIgnore
    public ModelNode require(String name) throws NoSuchElementException {
        return value.requireChild(name);
    }

    /**
     * Remove a child of this node, returning the child.  If no such child exists,
     * {@code null} is returned.
     * <p>
     * When called on property values, the name must match the property name.
     *
     * @param name the child name
     *
     * @return the child, or {@code null} if no child with the given {@code name} exists
     */
    @JsIgnore
    public ModelNode remove(String name) throws NoSuchElementException {
        return value.removeChild(name);
    }

    /**
     * Remove a child of this list, returning the child.  If no such child exists,
     * an exception is thrown.
     * <p>
     * When called on property values, the name must match the property name.
     *
     * @param index the child index
     *
     * @return the child
     *
     * @throws NoSuchElementException if the element does not exist
     */
    @JsIgnore
    public ModelNode remove(int index) throws NoSuchElementException {
        return value.removeChild(index);
    }

    /**
     * Get the child of this node with the given index.  If no such child exists, create it (adding list entries as
     * needed).
     * If the node is undefined, it will be initialized to be of type {@link ModelType#LIST}.
     * <p>
     * When called on property values, the index must be zero.
     *
     * @param index the child index
     *
     * @return the child
     *
     * @throws IllegalArgumentException if this node does not support getting a child with the given index
     */
    @JsIgnore
    public ModelNode get(int index) {
        ModelValue value = this.value;
        if (value == ModelValue.UNDEFINED) {
            checkProtect();
            return (this.value = new ListModelValue()).getChild(index);
        }
        return value.getChild(index);
    }

    /**
     * Require the existence of a child of this node with the given index, returning the child.  If no such child
     * exists,
     * an exception is thrown.
     * <p>
     * When called on property values, the index must be zero.
     *
     * @param index the child index
     *
     * @return the child
     *
     * @throws NoSuchElementException if the element does not exist
     */
    @JsIgnore
    public ModelNode require(int index) {
        return value.requireChild(index);
    }

    /**
     * Add the given value to the end of this node's value list.  If the node is undefined, it will be initialized to be
     * of type {@link ModelType#LIST}.
     *
     * @param newValue the new value to add
     *
     * @return this node
     *
     * @throws IllegalArgumentException if this node is {@link #isDefined() defined} and its {@link #getType() type} is
     *                                  not {@link ModelType#LIST}
     */
    @JsIgnore
    public ModelNode add(int newValue) {
        add().set(newValue);
        return this;
    }

    /**
     * Add the given value to the end of this node's value list.  If the node is undefined, it will be initialized to be
     * of type {@link ModelType#LIST}.
     *
     * @param newValue the new value to add
     *
     * @return this node
     *
     * @throws IllegalArgumentException if this node is {@link #isDefined() defined} and its {@link #getType() type} is
     *                                  not {@link ModelType#LIST}
     */
    @JsIgnore
    public ModelNode add(long newValue) {
        add().set(newValue);
        return this;
    }

    /**
     * Add the given value to the end of this node's value list.  If the node is undefined, it will be initialized to be
     * of type {@link ModelType#LIST}.
     *
     * @param newValue the new value to add
     *
     * @return this node
     *
     * @throws IllegalArgumentException if this node is {@link #isDefined() defined} and its {@link #getType() type} is
     *                                  not {@link ModelType#LIST}
     */
    @JsIgnore
    public ModelNode add(double newValue) {
        add().set(newValue);
        return this;
    }

    /**
     * Add the given value to the end of this node's value list.  If the node is undefined, it will be initialized to be
     * of type {@link ModelType#LIST}.
     *
     * @param newValue the new value to add
     *
     * @return this node
     *
     * @throws IllegalArgumentException if this node is {@link #isDefined() defined} and its {@link #getType() type} is
     *                                  not {@link ModelType#LIST}
     */
    @JsIgnore
    public ModelNode add(boolean newValue) {
        add().set(newValue);
        return this;
    }

    /**
     * Add the given expression to the end of this node's value list.  If the node is undefined, it will be initialized
     * to be
     * of type {@link ModelType#LIST}.
     *
     * @param newValue the new value to add
     *
     * @return this node
     *
     * @throws IllegalArgumentException if this node is {@link #isDefined() defined} and its {@link #getType() type} is
     *                                  not {@link ModelType#LIST}
     * @deprecated Use {@link #add(ValueExpression)} instead.
     */
    @JsIgnore
    @Deprecated
    public ModelNode addExpression(String newValue) {
        add().set(new ValueExpression(newValue));
        return this;
    }

    /**
     * Add the given value to the end of this node's value list.  If the node is undefined, it will be initialized to be
     * of type {@link ModelType#LIST}.
     *
     * @param newValue the new value to add
     *
     * @return this node
     *
     * @throws IllegalArgumentException if this node is {@link #isDefined() defined} and its {@link #getType() type} is
     *                                  not {@link ModelType#LIST}
     */
    @JsIgnore
    public ModelNode add(ValueExpression newValue) {
        add().set(newValue);
        return this;
    }

    /**
     * Add the given value to the end of this node's value list.  If the node is undefined, it will be initialized to be
     * of type {@link ModelType#LIST}.
     *
     * @param newValue the new value to add
     *
     * @return this node
     *
     * @throws IllegalArgumentException if this node is {@link #isDefined() defined} and its {@link #getType() type} is
     *                                  not {@link ModelType#LIST}
     */
    @JsIgnore
    public ModelNode add(String newValue) {
        add().set(newValue);
        return this;
    }

    /**
     * Add the given value to the end of this node's value list.  If the node is undefined, it will be initialized to be
     * of type {@link ModelType#LIST}.
     *
     * @param newValue the new value to add
     *
     * @return this node
     *
     * @throws IllegalArgumentException if this node is {@link #isDefined() defined} and its {@link #getType() type} is
     *                                  not {@link ModelType#LIST}
     */
    @JsIgnore
    public ModelNode add(BigDecimal newValue) {
        add().set(newValue);
        return this;
    }

    /**
     * Add the given value to the end of this node's value list.  If the node is undefined, it will be initialized to be
     * of type {@link ModelType#LIST}.
     *
     * @param newValue the new value to add
     *
     * @return this node
     *
     * @throws IllegalArgumentException if this node is {@link #isDefined() defined} and its {@link #getType() type} is
     *                                  not {@link ModelType#LIST}
     */
    @JsIgnore
    public ModelNode add(BigInteger newValue) {
        add().set(newValue);
        return this;
    }

    /**
     * Add a copy of the given value to the end of this node's value list.  If the node is undefined, it will be
     * initialized to be
     * of type {@link ModelType#LIST}.
     *
     * @param newValue the new value to add
     *
     * @return this node
     *
     * @throws IllegalArgumentException if this node is {@link #isDefined() defined} and its {@link #getType() type} is
     *                                  not {@link ModelType#LIST}
     */
    @JsIgnore
    public ModelNode add(ModelNode newValue) {
        add().set(newValue);
        return this;
    }

    /**
     * insert copy of the given value to provided index of this node's value list.  If the node is undefined, it will be
     * initialized to be
     * of type {@link ModelType#LIST}. An index equal to the current number of child elements
     * held by this node is allowed (thus adding a child) but an index greater than that is not allowed (i.e.
     * adding intervening elements is not supported.)
     *
     * @param newValue the new value to add
     *
     * @return this node
     *
     * @throws IndexOutOfBoundsException if {@code index} is greater than zero and is greater than the number of child
     *                                   nodes currently stored in this node
     * @throws IllegalArgumentException  if this node is {@link #isDefined() defined} and its {@link #getType() type} is
     *                                   not {@link ModelType#LIST}
     */
    @JsIgnore
    public ModelNode insert(ModelNode newValue, int index) {
        insert(index).set(newValue);
        return this;
    }

    /**
     * Add the given value to the end of this node's value list.  If the node is undefined, it will be initialized to be
     * of type {@link ModelType#LIST}.
     *
     * @param newValue the new value to add
     *
     * @return this node
     *
     * @throws IllegalArgumentException if this node is {@link #isDefined() defined} and its {@link #getType() type} is
     *                                  not {@link ModelType#LIST}
     */
    @JsIgnore
    public ModelNode add(byte[] newValue) {
        add().set(newValue);
        return this;
    }

    /**
     * Add a property to the end of this node's value list.  If the node is undefined, it
     * will be initialized to be of type {@link ModelType#LIST}.
     *
     * @param property the property
     *
     * @return this node
     *
     * @throws IllegalArgumentException if this node is {@link #isDefined() defined} and its {@link #getType() type} is
     *                                  not {@link ModelType#LIST}
     */
    @JsIgnore
    public ModelNode add(Property property) {
        add().set(property);
        return this;
    }

    /**
     * Add a property with the given name and value to the end of this node's value list.  If the node is undefined, it
     * will be initialized to be of type {@link ModelType#LIST}.
     *
     * @param propertyName  the property name
     * @param propertyValue the property value
     *
     * @return this node
     *
     * @throws IllegalArgumentException if this node is {@link #isDefined() defined} and its {@link #getType() type} is
     *                                  not {@link ModelType#LIST}
     */
    @JsIgnore
    public ModelNode add(String propertyName, int propertyValue) {
        add().set(propertyName, propertyValue);
        return this;
    }

    /**
     * Add a property with the given name and value to the end of this node's value list.  If the node is undefined, it
     * will be initialized to be of type {@link ModelType#LIST}.
     *
     * @param propertyName  the property name
     * @param propertyValue the property value
     *
     * @return this node
     *
     * @throws IllegalArgumentException if this node is {@link #isDefined() defined} and its {@link #getType() type} is
     *                                  not {@link ModelType#LIST}
     */
    @JsIgnore
    public ModelNode add(String propertyName, long propertyValue) {
        add().set(propertyName, propertyValue);
        return this;
    }

    /**
     * Add a property with the given name and value to the end of this node's value list.  If the node is undefined, it
     * will be initialized to be of type {@link ModelType#LIST}.
     *
     * @param propertyName  the property name
     * @param propertyValue the property value
     *
     * @return this node
     *
     * @throws IllegalArgumentException if this node is {@link #isDefined() defined} and its {@link #getType() type} is
     *                                  not {@link ModelType#LIST}
     */
    @JsIgnore
    public ModelNode add(String propertyName, double propertyValue) {
        add().set(propertyName, propertyValue);
        return this;
    }

    /**
     * Add a property with the given name and value to the end of this node's value list.  If the node is undefined, it
     * will be initialized to be of type {@link ModelType#LIST}.
     *
     * @param propertyName  the property name
     * @param propertyValue the property value
     *
     * @return this node
     *
     * @throws IllegalArgumentException if this node is {@link #isDefined() defined} and its {@link #getType() type} is
     *                                  not {@link ModelType#LIST}
     */
    @JsIgnore
    public ModelNode add(String propertyName, boolean propertyValue) {
        add().set(propertyName, propertyValue);
        return this;
    }

    /**
     * Add a property with the given name and value to the end of this node's value list.  If the node is undefined, it
     * will be initialized to be of type {@link ModelType#LIST}.
     *
     * @param propertyName  the property name
     * @param propertyValue the property value
     *
     * @return this node
     *
     * @throws IllegalArgumentException if this node is {@link #isDefined() defined} and its {@link #getType() type} is
     *                                  not {@link ModelType#LIST}
     */
    @JsIgnore
    public ModelNode add(String propertyName, ValueExpression propertyValue) {
        add().set(propertyName, propertyValue);
        return this;
    }

    /**
     * Add a property with the given name and value to the end of this node's value list.  If the node is undefined, it
     * will be initialized to be of type {@link ModelType#LIST}.
     *
     * @param propertyName  the property name
     * @param propertyValue the property value
     *
     * @return this node
     *
     * @throws IllegalArgumentException if this node is {@link #isDefined() defined} and its {@link #getType() type} is
     *                                  not {@link ModelType#LIST}
     */
    @JsIgnore
    public ModelNode add(String propertyName, String propertyValue) {
        add().set(propertyName, propertyValue);
        return this;
    }

    /**
     * Add a property with the given name and value to the end of this node's value list.  If the node is undefined, it
     * will be initialized to be of type {@link ModelType#LIST}.
     *
     * @param propertyName  the property name
     * @param propertyValue the property value
     *
     * @return this node
     *
     * @throws IllegalArgumentException if this node is {@link #isDefined() defined} and its {@link #getType() type} is
     *                                  not {@link ModelType#LIST}
     */
    @JsIgnore
    public ModelNode add(String propertyName, BigDecimal propertyValue) {
        add().set(propertyName, propertyValue);
        return this;
    }

    /**
     * Add a property with the given name and value to the end of this node's value list.  If the node is undefined, it
     * will be initialized to be of type {@link ModelType#LIST}.
     *
     * @param propertyName  the property name
     * @param propertyValue the property value
     *
     * @return this node
     *
     * @throws IllegalArgumentException if this node is {@link #isDefined() defined} and its {@link #getType() type} is
     *                                  not {@link ModelType#LIST}
     */
    @JsIgnore
    public ModelNode add(String propertyName, BigInteger propertyValue) {
        add().set(propertyName, propertyValue);
        return this;
    }

    /**
     * Add a property with the given name and value to the end of this node's value list.  If the node is undefined, it
     * will be initialized to be of type {@link ModelType#LIST}.
     *
     * @param propertyName  the property name
     * @param propertyValue the property value
     *
     * @return this node
     *
     * @throws IllegalArgumentException if this node is {@link #isDefined() defined} and its {@link #getType() type} is
     *                                  not {@link ModelType#LIST}
     */
    @JsIgnore
    public ModelNode add(String propertyName, ModelNode propertyValue) {
        add().set(propertyName, propertyValue);
        return this;
    }

    /**
     * Add a property with the given name and value to the end of this node's value list.  If the node is undefined, it
     * will be initialized to be of type {@link ModelType#LIST}.
     *
     * @param propertyName  the property name
     * @param propertyValue the property value
     *
     * @return this node
     *
     * @throws IllegalArgumentException if this node is {@link #isDefined() defined} and its {@link #getType() type} is
     *                                  not {@link ModelType#LIST}
     */
    @JsIgnore
    public ModelNode add(String propertyName, byte[] propertyValue) {
        add().set(propertyName, propertyValue);
        return this;
    }

    /**
     * Add a node to the end of this node's value list and return it.  If the node is undefined, it
     * will be initialized to be of type {@link ModelType#LIST}.
     *
     * @return the new node
     *
     * @throws IllegalArgumentException if this node is {@link #isDefined() defined} and its {@link #getType() type} is
     *                                  not {@link ModelType#LIST}
     */
    @JsIgnore
    public ModelNode add() {
        checkProtect();
        ModelValue value = this.value;
        if (value == ModelValue.UNDEFINED) {
            return (this.value = new ListModelValue()).addChild();
        }
        return value.addChild();
    }

    /**
     * Insert a node at provided index of this node's value list and return it.  If the node is undefined, it
     * will be initialized to be of type {@link ModelType#LIST}. An index equal to the current number of child elements
     * held by this node is allowed (thus adding a child) but an index greater than that is not allowed (i.e.
     * adding intervening elements is not supported.)
     *
     * @param index where in list to put it
     *
     * @return the new node
     *
     * @throws IndexOutOfBoundsException if {@code index} is greater than zero and is greater than the number of child
     *                                   nodes currently stored in this node
     * @throws IllegalArgumentException  if this node is {@link #isDefined() defined} and its {@link #getType() type} is
     *                                   not {@link ModelType#LIST}
     */
    @JsIgnore
    public ModelNode insert(int index) {
        checkProtect();
        ModelValue value = this.value;
        if (value == ModelValue.UNDEFINED) {
            return (this.value = new ListModelValue()).insertChild(index);
        }
        return value.insertChild(index);
    }

    /**
     * Add a node of type {@link ModelType#LIST} to the end of this node's value list and return it.  If this node is
     * undefined, it will be initialized to be of type {@link ModelType#LIST}.
     *
     * @return the new node
     *
     * @throws IllegalArgumentException if this node is {@link #isDefined() defined} and its {@link #getType() type} is
     *                                  not {@link ModelType#LIST}
     */
    public ModelNode addEmptyList() {
        ModelNode node = add();
        node.setEmptyList();
        return node;
    }

    /**
     * Add a node of type {@link ModelType#OBJECT} to the end of this node's value list and return it.  If this node is
     * undefined, it will be initialized to be of type {@link ModelType#LIST}.
     *
     * @return the new node
     *
     * @throws IllegalArgumentException if this node is {@link #isDefined() defined} and its {@link #getType() type} is
     *                                  not {@link ModelType#LIST}
     */
    public ModelNode addEmptyObject() {
        ModelNode node = add();
        node.setEmptyObject();
        return node;
    }

    /**
     * Determine whether this node has a child with the given index.  Property node types always contain exactly one
     * value.
     *
     * @param index the index
     *
     * @return {@code true} if there is a (possibly undefined) node at the given index
     */
    @JsIgnore
    public boolean has(int index) {
        return value.has(index);
    }

    /**
     * Determine whether this node has a child with the given name.  Property node types always contain exactly one
     * value with a key equal to the property name.
     *
     * @param key the name
     *
     * @return {@code true} if there is a (possibly undefined) node at the given key
     */
    public boolean has(String key) {
        return value.has(key);
    }

    /**
     * Recursively determine whether this node has children with the given names. If any child along the path does not
     * exist, return {@code false}.
     *
     * @param names the child names
     *
     * @return {@code true} if a call to {@link #get(String...)} with the given {@code names} would succeed without
     * needing to create any new nodes; {@code false} otherwise
     */
    @JsIgnore
    public boolean has(String... names) {
        ModelNode current = this;
        for (String part : names) {
            if (current.has(part)) {
                current = current.get(part);
            } else {
                return false;
            }
        }
        return true;
    }

    /**
     * Determine whether this node has a defined child with the given index.  Property node types always contain exactly
     * one
     * value.
     *
     * @param index the index
     *
     * @return {@code true} if there is a node at the given index and its {@link #getType() type} is not {@link
     * ModelType#UNDEFINED}
     */
    @JsIgnore
    public boolean hasDefined(int index) {
        return value.has(index) && get(index).isDefined();
    }

    /**
     * Determine whether this node has a defined child with the given name.  Property node types always contain exactly
     * one
     * value with a key equal to the property name.
     *
     * @param key the name
     *
     * @return {@code true} if there is a node at the given index and its {@link #getType() type} is not {@link
     * ModelType#UNDEFINED}
     */
    public boolean hasDefined(String key) {
        return value.has(key) && get(key).isDefined();
    }

    /**
     * Recursively determine whether this node has defined children with the given names. If any child along the path
     * does not
     * exist or is not defined, return {@code false}.
     *
     * @param names the child names
     *
     * @return {@code true} if a call to {@link #get(String...)} with the given {@code names} would succeed without
     * needing to create any new nodes and without traversing any undefined nodes; {@code false} otherwise
     */
    @JsIgnore
    public boolean hasDefined(String... names) {
        ModelNode current = this;
        for (String part : names) {
            if (current.hasDefined(part)) {
                current = current.get(part);
            } else {
                return false;
            }
        }
        return true;
    }

    /**
     * Get the set of keys contained in this object.  Property node types always contain exactly one value with a key
     * equal to the property name.  Other non-object types will throw an exception.
     *
     * @return the key set
     *
     * @throws IllegalArgumentException if this node's {@link #getType() type} is not {@link ModelType#OBJECT} or {@link
     *                                  ModelType#PROPERTY}
     */
    @JsIgnore
    public Set<String> keys() {
        return value.getKeys();
    }

    /**
     * Get the list of entries contained in this object.  Property node types always contain exactly one entry
     * (itself).
     * Lists will return an unmodifiable view of their contained list.  Objects will return a list of properties
     * corresponding
     * to the mappings within the object.  Other {@link #isDefined()} types will return an empty list.
     *
     * @return the entry list
     *
     * @throws IllegalArgumentException if this node is not {@link #isDefined() defined}
     */
    @JsIgnore
    public List<ModelNode> asList() {
        return value.asList();
    }

    /**
     * Recursively get the children of this node with the given names.  If any child along the path does not exist,
     * create it.  If any node is the path is undefined, it will be initialized to be of type {@link ModelType#OBJECT}.
     *
     * @param names the child names
     *
     * @return the child
     *
     * @throws IllegalArgumentException if a node does not support getting a child with the given name path
     */
    @JsIgnore
    public ModelNode get(String... names) {
        ModelNode current = this;
        for (String part : names) {
            current = current.get(part);
        }
        return current;
    }

    /**
     * Get a human-readable string representation of this model node, formatted nicely (possibly on multiple lines).
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        return value.toString();
    }

    /**
     * Output the DMR string representation of this model node, formatted nicely, if requested to the supplied
     * StringBuilder instance.
     *
     * @param writer  A StringBuilder instance used to output the DMR string.
     * @param compact Flag that indicates whether or not the string should be all on one line (i.e. {@code true}) or
     *                should be printed on multiple lines ({@code false}).
     */
    @JsIgnore
    public void writeString(StringBuilder builder, boolean compact) {
        if (compact) {
            ModelWriter modelWriter = ModelStreamFactory.getInstance(false).newModelWriter(builder);
            value.write(modelWriter);
        } else {
            value.writeString(builder, compact);
        }
    }

    /**
     * Get a JSON string representation of this model node, formatted nicely, if requested.
     *
     * @param compact Flag that indicates whether or not the string should be all on
     *                one line (i.e. {@code true}) or should be printed on multiple lines ({@code false}).
     *
     * @return The JSON string.
     */
    @JsIgnore
    public String toJSONString(boolean compact) {
        return value.toJSONString(compact);
    }

    /**
     * Output the JSON string representation of this model node, formatted nicely, if requested to the supplied
     * StringBuilder instance.
     *
     * @param writer  A StringBuilder instance used to output the JSON string.
     * @param compact Flag that indicates whether or not the string should be all on one line (i.e. {@code true}) or
     *                should be
     *                printed on multiple lines ({@code false}).
     */
    @JsIgnore
    public void writeJSONString(StringBuilder builder, boolean compact) {
        if (compact) {
            ModelWriter modelWriter = ModelStreamFactory.getInstance(true).newModelWriter(builder);
            value.write(modelWriter);
        } else {
            value.writeJSONString(builder, compact);
        }
    }

    final void write(ModelWriter writer) throws ModelException {
        value.write(writer);
    }

    /**
     * Reads base64 data from the passed string,
     * and deserializes the decoded result.
     *
     * @return the decoded model node
     *
     * @throws IOException if the passed stream has an issue
     * @see #toBase64()
     */
    public static ModelNode fromBase64(String encoded) {
        String decoded = Base64.decode(encoded);
        DataInput dataInput = new DataInput(decoded.getBytes());
        ModelNode node = new ModelNode();
        node.readExternal(dataInput);
        return node;
    }

    /**
     * Determine whether this object is equal to another.
     *
     * @param other the other object
     *
     * @return {@code true} if they are equal, {@code false} otherwise
     */
    @Override
    @JsIgnore
    public boolean equals(Object other) {
        return other instanceof ModelNode && equals((ModelNode) other);
    }

    /**
     * Determine whether this object is equal to another.
     *
     * @param other the other object
     *
     * @return {@code true} if they are equal, {@code false} otherwise
     */
    @JsIgnore
    public boolean equals(ModelNode other) {
        return this == other || other != null && other.value.equals(value);
    }

    /**
     * Get the hash code of this node object.  Note that unless the value is {@link #protect()}ed, the hash code may
     * change over time, thus making unprotected nodes unsuitable for use as hash table keys.
     *
     * @return the hash code
     */
    @Override
    @JsIgnore
    public int hashCode() {
        return value.hashCode();
    }

    /**
     * Clone this model node.
     *
     * @return the clone
     */
    @Override
    @JsIgnore
    public ModelNode clone() {
        ModelNode clone = new ModelNode();
        clone.value = value.copy();
        return clone;
    }

    void format(StringBuilder builder, int indent, boolean multiLine) {
        value.format(builder, indent, multiLine);
    }

    void formatAsJSON(StringBuilder builder, int indent, boolean multiLine) {
        value.formatAsJSON(builder, indent, multiLine);
    }

    /**
     * Get the current type of this node.
     *
     * @return the node type
     */
    @JsIgnore
    public ModelType getType() {
        return value.getType();
    }

    /**
     * Write this node's content in binary format to the given target.
     *
     * @param out the target to which the content should be written
     *
     * @throws IOException if an I/O error occurs
     */
    @JsIgnore
    public void writeExternal(DataOutput out) {
        value.writeExternal(out);
    }

    /**
     * Read this node's content in binary format from the given source.
     *
     * @param in the source from which the content should be read
     */
    void readExternal(DataInput in) {
        checkProtect();
        char c = (char) (in.readByte() & 0xff);
        ModelType type = ModelType.forChar(c);
        switch (type) {
            case UNDEFINED:
                value = ModelValue.UNDEFINED;
                return;
            case BIG_DECIMAL:
                value = new BigDecimalModelValue(in);
                return;
            case BIG_INTEGER:
                value = new BigIntegerModelValue(in);
                return;
            case BOOLEAN:
                value = BooleanModelValue.valueOf(in.readBoolean());
                return;
            case BYTES:
                value = new BytesModelValue(in);
                return;
            case DOUBLE:
                value = new DoubleModelValue(in.readDouble());
                return;
            case EXPRESSION:
                value = new ExpressionValue(in.readUTF());
                return;
            case INT:
                value = new IntModelValue(in.readInt());
                return;
            case LIST:
                value = new ListModelValue(in);
                return;
            case LONG:
                value = new LongModelValue(in.readLong());
                return;
            case OBJECT:
                value = new ObjectModelValue(in);
                return;
            case PROPERTY:
                value = new PropertyModelValue(in);
                return;
            case STRING:
                value = new StringModelValue(c, in);
                return;
            case TYPE:
                value = TypeModelValue.of(ModelType.forChar((char) (in.readByte() & 0xff)));
                return;
            default:
                throw new RuntimeException("Invalid type read: " + type);
        }
    }

    /** Encodes the serialized representation in base64 form. */
    public String toBase64() {
        DataOutput out = new DataOutput();
        writeExternal(out);
        return Base64.encode(out.toString());
    }

    @JsIgnore
    private void checkProtect() {
        if (protect) {
            throw new UnsupportedOperationException();
        }
    }

    /** @return {@code true} if this node has an outcome and the outcome does not equal "success" */
    @JsProperty
    public boolean isFailure() {
        return hasDefined(OUTCOME) && !get(OUTCOME).asString().equals(SUCCESS);
    }

    /** @return the failure description or "No failure-description provided" */
    @JsProperty
    public String getFailureDescription() {
        if (hasDefined(FAILURE_DESCRIPTION)) {
            return get(FAILURE_DESCRIPTION).toJSONString(false);
        } else {
            return "No failure-description provided";
        }
    }
}
