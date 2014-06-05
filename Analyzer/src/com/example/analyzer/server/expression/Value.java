// Copyright 2010 Anthony F. Stuart - All rights reserved.
//
// This program and the accompanying materials are made available
// under the terms of the GNU General Public License. For other license
// options please contact the copyright owner.
//
// This program is made available on an "as is" basis, without
// warranties or conditions of any kind, either express or implied.

package com.example.analyzer.server.expression;

import java.math.BigDecimal;
import java.util.Date;

/**
 * A typed value class that efficiently wraps primitive values so that they can
 * be accessed repeatedly on large tables.
 */

public final class Value implements Comparable<Value>
{
  public static final Value EMPTY = new Value("", ValueType.STRING);
  public static final Value FALSE = new Value(Boolean.FALSE, ValueType.BOOLEAN);
  public static final Value NULL = new Value(null, ValueType.NULL);
  public static final Value ONE = new Value(Integer.valueOf(1), ValueType.INTEGER);
  public static final Value TRUE = new Value(Boolean.TRUE, ValueType.BOOLEAN);
  public static final Value ZERO = new Value(Integer.valueOf(0), ValueType.INTEGER);

  private Object value;
  private ValueType valueType;

  public Value(Object value, ValueType valueType)
  {
    this.value = value;
    this.valueType = valueType;
  }

  public Value add(Value that)
  {
    if (this.valueType == ValueType.DECIMAL)
    {
      return new Value(this.getDecimal().add(that.promote()), ValueType.DECIMAL);
    }

    if (that.valueType == ValueType.DECIMAL)
    {
      return new Value(this.promote().add(that.getDecimal()), ValueType.DECIMAL);
    }

    return new Value(this.getInteger() + that.getInteger(), ValueType.INTEGER);
  }

  private void checkNullValue()
  {
    if (valueType == ValueType.NULL)
    {
      throw new NullValueException();
    }
  }

  @Override
  public int compareTo(Value that)
  {
    if (this.valueType == ValueType.DECIMAL)
    {
      return this.getDecimal().compareTo(that.promote());
    }

    if (that.valueType == ValueType.DECIMAL)
    {
      return this.promote().compareTo(that.getDecimal());
    }

    if (this.valueType == ValueType.INTEGER && that.valueType == ValueType.INTEGER)
    {
      return this.getInteger().compareTo(that.getInteger());
    }

    if (this.valueType == ValueType.BOOLEAN && that.valueType == ValueType.BOOLEAN)
    {
      return (this.getBoolean() ? 1 : 0) - (that.getBoolean() ? 1 : 0);
    }

    return this.value.toString().compareTo(that.value.toString());
  }

  public Value divide(Value that, int roundingMode)
  {
    if (this.valueType == ValueType.DECIMAL)
    {
      return new Value(this.getDecimal().divide(that.promote(), roundingMode), ValueType.DECIMAL);
    }

    if (that.valueType == ValueType.DECIMAL)
    {
      return new Value(this.promote().divide(that.getDecimal(), Evaluator.defaultScale, roundingMode), ValueType.DECIMAL);
    }

    return new Value(this.promote().divide(that.promote(), Evaluator.defaultScale, roundingMode), ValueType.DECIMAL);
  }

  public Boolean getBoolean()
  {
    checkNullValue();
    return (Boolean)value;
  }

  public Date getDate()
  {
    checkNullValue();
    return (Date)value;
  }

  public final BigDecimal getDecimal()
  {
    checkNullValue();
    return (BigDecimal)value;
  }

  public final Integer getInteger()
  {
    checkNullValue();
    return (Integer)value;
  }

  public String getString()
  {
    checkNullValue();
    return (String)value;
  }

  public ValueType getType()
  {
    return valueType;
  }

  public Object getValue()
  {
    // NB: Do not checkNullValue here, this is the method by which null values are returned
    return value;
  }

  public boolean matchesSymbol(String string)
  {
    return value instanceof String && ((String)value).equals(string);
  }

  public Value multiply(Value that)
  {
    if (this.valueType == ValueType.DECIMAL)
    {
      return new Value(this.getDecimal().multiply(that.promote()), ValueType.DECIMAL);
    }

    if (that.valueType == ValueType.DECIMAL)
    {
      return new Value(this.promote().multiply(that.getDecimal()), ValueType.DECIMAL);
    }

    return new Value(this.getInteger() * that.getInteger(), ValueType.INTEGER);
  }

  public Value negate()
  {
    if (this.valueType == ValueType.DECIMAL)
    {
      return new Value(getDecimal().negate(), ValueType.DECIMAL);
    }

    return new Value(-this.getInteger(), ValueType.INTEGER);
  }

  public BigDecimal promote()
  {
    if (valueType == ValueType.INTEGER)
    {
      return new BigDecimal(getInteger());
    }
    return getDecimal();
  }

  public Value subtract(Value that)
  {
    if (this.valueType == ValueType.DECIMAL)
    {
      return new Value(this.getDecimal().subtract(that.promote()), ValueType.DECIMAL);
    }

    if (that.valueType == ValueType.DECIMAL)
    {
      return new Value(this.promote().subtract(that.getDecimal()), ValueType.DECIMAL);
    }

    return new Value(this.getInteger() - that.getInteger(), ValueType.INTEGER);
  }

  @Override
  public String toString()
  {
    return valueType + ": " + value;
  }

}
