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
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import com.example.analyzer.server.database.DbTable;
import com.example.analyzer.server.expression.functions.Coalesce;
import com.example.analyzer.server.expression.functions.Concatenate;
import com.example.analyzer.server.expression.functions.Days;
import com.example.analyzer.server.expression.functions.Length;
import com.example.analyzer.server.expression.functions.Locate;
import com.example.analyzer.server.expression.functions.Lower;
import com.example.analyzer.server.expression.functions.Months;
import com.example.analyzer.server.expression.functions.Quarters;
import com.example.analyzer.server.expression.functions.Substr;
import com.example.analyzer.server.expression.functions.Today;
import com.example.analyzer.server.expression.functions.Trim;
import com.example.analyzer.server.expression.functions.Upper;
import com.example.analyzer.server.expression.functions.Weeks;
import com.example.analyzer.server.expression.functions.When;
import com.example.analyzer.server.expression.functions.Years;
import com.example.analyzer.server.expression.operators.Add;
import com.example.analyzer.server.expression.operators.And;
import com.example.analyzer.server.expression.operators.Assign;
import com.example.analyzer.server.expression.operators.Divide;
import com.example.analyzer.server.expression.operators.EqualTo;
import com.example.analyzer.server.expression.operators.GreaterThan;
import com.example.analyzer.server.expression.operators.GreaterThanOrEqualTo;
import com.example.analyzer.server.expression.operators.LessThan;
import com.example.analyzer.server.expression.operators.LessThanOrEqualTo;
import com.example.analyzer.server.expression.operators.Multiply;
import com.example.analyzer.server.expression.operators.NotEqualTo;
import com.example.analyzer.server.expression.operators.Or;
import com.example.analyzer.server.expression.operators.Subtract;
import com.example.analyzer.shared.DbFunction;

public class Evaluator
{
  static int defaultScale = 4;

  protected String expression;
  private HashMap<String, Function> functions = new HashMap<String, Function>();

  public Operator opAdd = new Add(this);
  public Operator opAnd = new And(this);
  public Operator opAssign = new Assign(this);
  public Operator opDivide = new Divide(this);
  public Operator opEqualTo = new EqualTo(this);
  public Operator opGreaterThan = new GreaterThan(this);
  public Operator opGreaterThanOrEqualTo = new GreaterThanOrEqualTo(this);
  public Operator opLessThan = new LessThan(this);
  public Operator opLessThanOrEqualTo = new LessThanOrEqualTo(this);
  public Operator opMultiply = new Multiply(this);
  public Operator opNotEqualTo = new NotEqualTo(this);
  public Operator opOr = new Or(this);
  public Operator opSubtract = new Subtract(this);

  private int rowOffset;
  private DbTable table;
  private int tokenOffset;
  private ArrayList<Value> values;

  public Evaluator(DbTable table, String expression)
  {
    this.table = table;

    // TODO: Dynamically load functions using an approach similar to column expanders
    functions.put("COALESCE", new Coalesce(this));
    functions.put("CONCATENATE", new Concatenate(this));
    functions.put("DAYS", new Days(this));
    functions.put("LENGTH", new Length(this));
    functions.put("LOCATE", new Locate(this));
    functions.put("LOWER", new Lower(this));
    functions.put("MONTHS", new Months(this));
    functions.put("QUARTERS", new Quarters(this));
    functions.put("SUBSTR", new Substr(this));
    functions.put("TODAY", new Today(this));
    functions.put("TRIM", new Trim(this));
    functions.put("UPPER", new Upper(this));
    functions.put("WEEKS", new Weeks(this));
    functions.put("WHEN", new When(this));
    functions.put("YEARS", new Years(this));
    
    assert(functions.size() == DbFunction.values().length);

    HashMap<String, Variable> variables = new HashMap<String, Variable>();
    Parser expressionParser = new Parser(table, variables, functions);
    values = expressionParser.parse(expression);
    dumpTokens();
  }

  private void dumpTokens()
  {
    for (Value value : values)
    {
      System.out.println("DbExpression.dumpTokens: token=" + value + ", class=" + value.getValue().getClass().getName());
    }
  }

  public Value evaluateAt(int rowOffset)
  {
    this.rowOffset = rowOffset;
    tokenOffset = 0;
    Value result;
    try
    {
      result = getValue(evaluatePrecedenceA());
      if (tokenOffset < values.size())
      {
        throw new EvaluatorException("Expected end of expression, received " + getToken());
      }
    }
    catch (NullValueException e)
    {
      result = Value.NULL;
    }
    return result;
  }

  private Value evaluateFunction(Value functionValue)
  {
    Value argumentValue;
    ArrayList<Value> argumentValues = new ArrayList<Value>();
    while ((argumentValue = evaluatePrecedenceA()) != null && !argumentValue.matchesSymbol(")"))
    {
      if (!argumentValue.matchesSymbol(","))
      {
        argumentValues.add(argumentValue);
      }
    }

    Function function = (Function)functionValue.getValue();
    int suppliedArgumentCount = argumentValues.size();
    int minimumArgumentCount = function.getMinimumArgumentCount();
    if (suppliedArgumentCount < minimumArgumentCount)
    {
      throw new EvaluatorException("Expected " + minimumArgumentCount + " argument(s), received " + suppliedArgumentCount);
    }
    int maximumArgumentCount = function.getMaximumArgumentCount();
    if (suppliedArgumentCount > maximumArgumentCount)
    {
      throw new EvaluatorException("Expected " + maximumArgumentCount + " argument(s), received " + suppliedArgumentCount);
    }
    Value result = function.evaluate(argumentValues);
    return result;
  }
  
  // TODO: Replace separate recursive descent methods with a single method and an operator precedence stack

  public Value evaluatePrecedenceA()
  {
    Operator operator;
    Value left = evaluatePrecedenceB();
    if (left.getType() != ValueType.PUNCTUATION) // comma and right paren in argument list
    {
      if ((operator = getOperator(opAssign)) != null)
      {
        Value right = evaluatePrecedenceB();
        left = operator.evaluate(left, right);
      }
    }
    return left;
  }

  public Value evaluatePrecedenceB()
  {
    Operator operator;
    Value left = evaluatePrecedenceC();
    if (left.getType() != ValueType.PUNCTUATION)
    {
      while ((operator = getOperator(opAnd, opOr)) != null)
      {
        Value right = evaluatePrecedenceC();
        left = operator.evaluate(left, right);
      }
    }
    return left;
  }

  public Value evaluatePrecedenceC()
  {
    Operator operator;
    Value left = evaluatePrecedenceD();
    if (left.getType() != ValueType.PUNCTUATION)
    {
      while ((operator = getOperator(opLessThan, opLessThanOrEqualTo, opEqualTo, opGreaterThanOrEqualTo, opGreaterThan, opNotEqualTo)) != null)
      {
        Value right = evaluatePrecedenceD();
        left = operator.evaluate(left, right);
      }
    }
    return left;
  }

  public Value evaluatePrecedenceD()
  {
    Operator operator;
    Value left = evaluatePrecedenceE();
    if (left.getType() != ValueType.PUNCTUATION)
    {
      while ((operator = getOperator(opAdd, opSubtract)) != null)
      {
        Value right = evaluatePrecedenceE();
        left = operator.evaluate(left, right);
      }
    }
    return left;
  }

  public Value evaluatePrecedenceE()
  {
    Operator operator;
    Value left = evaluateTerm();
    if (left.getType() != ValueType.PUNCTUATION)
    {
      while ((operator = getOperator(opMultiply, opDivide)) != null)
      {
        Value right = evaluateTerm();
        left = operator.evaluate(left, right);
      }
    }
    return left;
  }

  public Value evaluateTerm()
  {
    Value value = getToken();
    if (value == null)
    {
      throw new EvaluatorException("Expected COMMA or RIGHT PARENTHESIS, received 'End of Text'");
    }

    if (value.matchesSymbol("-"))
    {
      return toNumber(evaluatePrecedenceA()).negate();
    }

    if (value.matchesSymbol("+"))
    {
      return evaluatePrecedenceA();
    }

    if (value.matchesSymbol("!"))
    {
      return (!getBoolean(evaluatePrecedenceA()).getBoolean()) ? Value.TRUE : Value.FALSE;
    }

    if (value.matchesSymbol("("))
    {
      Value expression = evaluatePrecedenceA();

      if (!(value = getToken()).matchesSymbol(")"))
      {
        throw new EvaluatorException("Expected right parenthesis, received " + value);
      }

      return expression;
    }

    if (value.getType() == ValueType.FUNCTION)
    {
      return evaluateFunction(value);
    }

    return value;
  }

  public Value getBoolean(Value reference)
  {
    Value booleanValue;
    Value value = getValue(reference);
    ValueType valueType = value.getType();
    switch (valueType)
    {
      case BOOLEAN:
        booleanValue = value;
        break;
      case DECIMAL:
        booleanValue = value.getDecimal().intValue() == 0 ? Value.FALSE : Value.TRUE;
        break;
      case INTEGER:
        booleanValue = value.getInteger().intValue() == 0 ? Value.FALSE : Value.TRUE;
        break;
      default:
        throw new EvaluatorException("Expected BOOLEAN, DECIMAL or INTEGER, received " + valueType);
    }
    return booleanValue;
  }

  public int getColumnType(ValueType valueType)
  {
    int type;
    switch (valueType)
    {
      case BOOLEAN:
        type = Types.BOOLEAN;
        break;
      case DATE:
        type = Types.DATE;
        break;
      case DECIMAL:
        type = Types.DECIMAL;
        break;
      case INTEGER:
        type = Types.INTEGER;
        break;
      case NULL:
        type = Types.VARCHAR;
        break;
      case STRING:
        type = Types.VARCHAR;
        break;
      default:
        throw new UnsupportedOperationException("Expected a supported type, received " + valueType);
    }
    return type;
  }

  public Value getColumnValue(Value reference)
  {
    Value columnValue;
    int columnOffset = (Integer)reference.getValue();
    Object rawValue = table.get(rowOffset, columnOffset);
    if (rawValue == null)
    {
      columnValue = Value.NULL;
    }
    else if (rawValue instanceof Boolean)
    {
      columnValue = ((Boolean)rawValue).booleanValue() ? Value.TRUE : Value.FALSE;
    }
    else if (rawValue instanceof BigDecimal)
    {
      columnValue = new Value(rawValue, ValueType.DECIMAL);
    }
    else if (rawValue instanceof Byte)
    {
      columnValue = new Value(Integer.valueOf((Byte)rawValue), ValueType.INTEGER);
    }
    else if (rawValue instanceof Character)
    {
      columnValue = new Value(Integer.valueOf(((Character)rawValue).charValue()), ValueType.INTEGER);
    }
    else if (rawValue instanceof Double)
    {
      columnValue = new Value(new BigDecimal(rawValue.toString()), ValueType.DECIMAL); // See javadoc for BigDecimal(double val)
    }
    else if (rawValue instanceof Float)
    {
      columnValue = new Value(new BigDecimal(rawValue.toString()), ValueType.DECIMAL); // See javadoc for BigDecimal(double val)
    }
    else if (rawValue instanceof Date)
    {
      columnValue = new Value(rawValue, ValueType.DATE);
    }
    else if (rawValue instanceof Integer)
    {
      columnValue = new Value(rawValue, ValueType.INTEGER);
    }
    else if (rawValue instanceof Long)
    {
      columnValue = new Value(new BigDecimal((Long)rawValue), ValueType.DECIMAL);
    }
    else if (rawValue instanceof String)
    {
      columnValue = new Value(rawValue, ValueType.STRING);
    }
    else
    {
      String string = rawValue.toString();
      System.out.println("Evaluator.getColumnValue: performing STRING conversion on " + rawValue.getClass().getName() + ", result is " + string);
      columnValue = new Value(string, ValueType.STRING);
    }
    return columnValue;
  }

  public Operator getOperator(Operator... operators)
  {
    Value value = getToken();
    if (value != null)
    {
      if (value.getType() == ValueType.OPERATOR)
      {
        for (Operator operator : operators)
        {
          if (value.matchesSymbol(operator.getOperatorName()))
          {
            return operator;
          }
        }
      }
      retract();
    }
    return null;
  }

  private Value getToken()
  {
    Value value;
    if (tokenOffset == values.size())
    {
      value = null;
    }
    else
    {
      value = values.get(tokenOffset++);
    }
    return value;
  }

  public Value getValue(Value reference)
  {
    Value value;
    switch (reference.getType())
    {
      case COLUMN:
        value = getColumnValue(reference);
        break;
      case VARIABLE:
        value = getVariableValue(reference);
        break;
      default:
        value = reference;
        break;
    }
    return value;
  }

  public final Value getVariableValue(Value reference)
  {
    Variable variable = (Variable)reference.getValue();
    Value value = variable.getValue();
    return value;
  }

  private void retract()
  {
    tokenOffset--;
  }

  public Value toDate(Value reference)
  {
    Value date;
    Value value = getValue(reference);
    ValueType valueType = value.getType();
    switch (valueType)
    {
      case DATE:
        date = value;
        break;
      case STRING:
        date = new Value(new Date(java.sql.Date.valueOf((String)value.getValue()).getTime()), ValueType.DATE);
        break;
      case NULL:
        throw new NullValueException();
      default:
        throw new EvaluatorException("Expected DATE or STRING, received " + valueType);
    }
    return date;
  }

  public Value toNumber(Value reference)
  {
    Value number;
    Value value = getValue(reference);
    ValueType valueType = value.getType();
    switch (valueType)
    {
      case BOOLEAN:
        number = value.getBoolean() ? Value.ONE : Value.ZERO;
        break;
      case DECIMAL:
      case INTEGER:
        number = value;
        break;
      case NULL:
        throw new NullValueException();
      default:
        throw new EvaluatorException("Expected BOOLEAN, DECIMAL or INTEGER, received " + valueType);
    }
    if (number.getType() == ValueType.NULL && reference.getType() == ValueType.VARIABLE)
    {
      // For the purposes of arithmetic, uninitialized variables are coalesced to zero
      number = Value.ZERO;
    }
    return number;
  }

  public Value toString(Value reference)
  {
    Value string;
    Value value = getValue(reference);
    ValueType valueType = value.getType();
    switch (valueType)
    {
      case STRING:
        string = value;
        break;
      case NULL:
        throw new NullValueException();
      default:
        string = new Value(value.getValue().toString(), ValueType.STRING);
        break;
    }
    return string;
  }

}
