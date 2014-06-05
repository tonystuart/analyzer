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
import java.util.ArrayList;
import java.util.HashMap;

import com.example.analyzer.server.database.DbTable;

public class Parser
{
  static final char ETX = 0x3;
  private static final String OPERATOR_CHARACTERS = "+-*/<>=!:";
  private static final String PUNCTUATION_CHARACTERS = "(),";

  private int currentOffset;
  private StringBuilder currentToken = new StringBuilder();
  private String expression;
  private int expressionLength;
  private HashMap<String, Function> functions;
  private DbTable table;
  private ArrayList<Value> values = new ArrayList<Value>();
  private HashMap<String, Variable> variables;

  public Parser(DbTable table, HashMap<String, Variable> variables, HashMap<String, Function> functions)
  {
    this.table = table;
    this.variables = variables;
    this.functions = functions;
  }

  private void addToken(char c, State state)
  {
    addToken(String.valueOf(c), state);
  }

  private void addToken(String string, State state)
  {
    Value value = createToken(string, state);
    values.add(value);
    currentToken.setLength(0);
  }

  private void addToken(StringBuilder stringBuilder, State state)
  {
    addToken(stringBuilder.toString(), state);
  }

  private Value createToken(String string, State state)
  {
    Value value;
    int columnOffset;
    switch (state)
    {
      case IDENTIFIER:
        columnOffset = table.getColumnOffsetNoCase(string);
        if (columnOffset != -1)
        {
          value = new Value(columnOffset, ValueType.COLUMN);
        }
        else if (string.equalsIgnoreCase("false"))
        {
          value = new Value(Boolean.FALSE, ValueType.BOOLEAN);
        }
        else if (string.equalsIgnoreCase("true"))
        {
          value = new Value(Boolean.TRUE, ValueType.BOOLEAN);
        }
        else if (string.equalsIgnoreCase("and"))
        {
          value = new Value("&&", ValueType.OPERATOR);
        }
        else if (string.equalsIgnoreCase("or"))
        {
          value = new Value("||", ValueType.OPERATOR);
        }
        else if (string.equalsIgnoreCase("not"))
        {
          value = new Value("!", ValueType.OPERATOR);
        }
        else
        {
          value = new Value(getVariable(string), ValueType.VARIABLE);
        }
        break;
      case FUNCTION:
        value = new Value(getFunction(string), ValueType.FUNCTION);
        break;
      case LITERAL_NUMERIC_DECIMAL:
        value = new Value(new BigDecimal(string), ValueType.DECIMAL);
        break;
      case LITERAL_NUMERIC_INTEGER:
        value = new Value(Integer.valueOf(string), ValueType.INTEGER);
        break;
      case LITERAL_STRING_DQUOTE:
        columnOffset = table.getColumnOffset(string);
        if (columnOffset != -1)
        {
          value = new Value(columnOffset, ValueType.COLUMN);
        }
        else
        {
          value = new Value(string, ValueType.STRING);
        }
        break;
      case LITERAL_STRING_SQUOTE:
        value = new Value(string, ValueType.STRING);
        break;
      case OPERATOR:
        value = new Value(string, ValueType.OPERATOR);
        break;
      case PUNCTUATION:
        value = new Value(string, ValueType.PUNCTUATION);
        break;
      default:
        throw new UnsupportedOperationException();
    }
    return value;
  }

  public Function getFunction(String name)
  {
    Function function = functions.get(name.toUpperCase());
    if (function == null)
    {
      throw new ParserException("Invalid function " + name);
    }
    return function;
  }

  public char getNext()
  {
    char c;
    if (currentOffset < expressionLength)
    {
      c = expression.charAt(currentOffset++);
    }
    else
    {
      c = ETX;
    }
    return c;
  }

  public Variable getVariable(String name)
  {
    Variable variable = variables.get(name);
    if (variable == null)
    {
      variable = new Variable();
      variables.put(name, variable);
    }
    return variable;
  }

  private boolean isDigit(char c)
  {
    return c != ETX && Character.isDigit(c);
  }

  private boolean isIdentifierPart(char c)
  {
    return c != ETX && Character.isJavaIdentifierPart(c) || c == '.';
  }

  private boolean isIdentifierStart(char c)
  {
    return c != ETX && Character.isJavaIdentifierStart(c);
  }

  private boolean isOperatorCharacter(char c)
  {
    return c != ETX && OPERATOR_CHARACTERS.indexOf(c) != -1;
  }

  private boolean isPunctationCharacter(char c)
  {
    return c != ETX && PUNCTUATION_CHARACTERS.indexOf(c) != -1;
  }

  private boolean isWhitespace(char c)
  {
    return Character.isWhitespace(c);
  }

  private boolean lookahead(char target)
  {
    char c = ETX;
    while (currentOffset < expressionLength && isWhitespace(c = expression.charAt(currentOffset++)))
    {
    }
    return c == target;
  }

  public ArrayList<Value> parse(String expression)
  {
    State state = State.INITIAL;

    this.expression = expression;
    this.currentOffset = 0;
    this.expressionLength = expression.length();

    while (state != State.FINAL)
    {
      char c = getNext();

      switch (state)
      {
        case INITIAL:
          if (isIdentifierStart(c))
          {
            currentToken.append(c);
            state = State.IDENTIFIER;
          }
          else if (isDigit(c))
          {
            currentToken.append(c);
            state = State.LITERAL_NUMERIC_INTEGER;
          }
          else if (isPunctationCharacter(c))
          {
            addToken(c, State.PUNCTUATION);
            state = State.INITIAL;
          }
          else if (isOperatorCharacter(c))
          {
            currentToken.append(c);
            state = State.OPERATOR;
          }
          else if (isWhitespace(c))
          {
            state = State.INITIAL;
          }
          else if (c == '"')
          {
            state = State.LITERAL_STRING_DQUOTE;
          }
          else if (c == '\'')
          {
            state = State.LITERAL_STRING_SQUOTE;
          }
          else if (c == '.')
          {
            currentToken.append(c);
            state = State.LEADING_DOT;
          }
          else if (c == ETX)
          {
            state = State.FINAL;
          }
          else
          {
            throw new ParserException(expression, currentOffset, c);
          }
          break;
        case IDENTIFIER:
          if (isIdentifierPart(c))
          {
            currentToken.append(c);
            state = State.IDENTIFIER;
          }
          else if (c == '(')
          {
            addToken(currentToken, State.FUNCTION);
            state = State.INITIAL;
          }
          else if (isPunctationCharacter(c))
          {
            addToken(currentToken, state);
            addToken(c, State.PUNCTUATION);
            state = State.INITIAL;
          }
          else if (isOperatorCharacter(c))
          {
            addToken(currentToken, state);
            currentToken.append(c);
            state = State.OPERATOR;
          }
          else if (isWhitespace(c))
          {
            if (lookahead('('))
            {
              addToken(currentToken, State.FUNCTION);
            }
            else
            {
              addToken(currentToken, state);
              currentOffset--;
            }
            state = State.INITIAL;
          }
          else if (c == ETX)
          {
            addToken(currentToken, state);
            state = State.FINAL;
          }
          else
          {
            throw new ParserException(expression, currentOffset, c);
          }
          break;
        case LITERAL_NUMERIC_INTEGER:
          if (isDigit(c))
          {
            currentToken.append(c);
            state = State.LITERAL_NUMERIC_INTEGER;
          }
          else if (c == '.')
          {
            currentToken.append(c);
            state = State.LITERAL_NUMERIC_DECIMAL;
          }
          else if (isPunctationCharacter(c))
          {
            addToken(currentToken, state);

            addToken(c, State.PUNCTUATION);
            state = State.INITIAL;
          }
          else if (isOperatorCharacter(c))
          {
            addToken(currentToken, state);
            currentToken.append(c);
            state = State.OPERATOR;
          }
          else if (isWhitespace(c))
          {
            addToken(currentToken, state);
            state = State.INITIAL;
          }
          else if (c == ETX)
          {
            addToken(currentToken, state);
            state = State.FINAL;
          }
          else
          {
            throw new ParserException(expression, currentOffset, c);
          }
          break;
        case LITERAL_NUMERIC_DECIMAL:
          if (isDigit(c))
          {
            currentToken.append(c);
            state = State.LITERAL_NUMERIC_DECIMAL;
          }
          else if (isPunctationCharacter(c))
          {
            addToken(currentToken, state);
            addToken(c, State.PUNCTUATION);
            state = State.INITIAL;
          }
          else if (isOperatorCharacter(c))
          {
            addToken(currentToken, state);
            currentToken.append(c);
            state = State.OPERATOR;
          }
          else if (isWhitespace(c))
          {
            addToken(currentToken, state);
            state = State.INITIAL;
          }
          else if (c == ETX)
          {
            addToken(currentToken, state);
            state = State.FINAL;
          }
          else
          {
            throw new ParserException(expression, currentOffset, c);
          }
          break;
        case LEADING_DOT:
          if (isDigit(c))
          {
            currentToken.append(c);
            state = State.LITERAL_NUMERIC_DECIMAL;
          }
          else
          {
            throw new ParserException(expression, currentOffset, c);
          }
          break;
        case OPERATOR:
          if (isOperatorCharacter(c))
          {
            currentToken.append(c);
            state = State.OPERATOR;
          }
          else
          {
            addToken(currentToken, state);
            state = State.INITIAL;
            currentOffset--;
          }
          break;
        case LITERAL_STRING_DQUOTE:
          if (c == '"')
          {
            addToken(currentToken, state);
            state = State.INITIAL;
          }
          else if (c != ETX)
          {
            currentToken.append(c);
            state = State.LITERAL_STRING_DQUOTE;
          }
          else
          {
            throw new ParserException(expression, currentOffset, c);
          }
          break;
        case LITERAL_STRING_SQUOTE:
          if (c == '\'')
          {
            addToken(currentToken, state);
            state = State.INITIAL;
          }
          else if (c != ETX)
          {
            currentToken.append(c);
            state = State.LITERAL_STRING_SQUOTE;
          }
          else
          {
            throw new ParserException(expression, currentOffset, c);
          }
          break;
      }
    }
    return values;
  }

  private enum State
  {
    FINAL, FUNCTION, IDENTIFIER, INITIAL, LEADING_DOT, LITERAL_NUMERIC_DECIMAL, LITERAL_NUMERIC_INTEGER, LITERAL_STRING_DQUOTE, LITERAL_STRING_SQUOTE, OPERATOR, PUNCTUATION,
  }

}
