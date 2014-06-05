// Copyright 2010 Anthony F. Stuart - All rights reserved.
//
// This program and the accompanying materials are made available
// under the terms of the GNU General Public License. For other license
// options please contact the copyright owner.
//
// This program is made available on an "as is" basis, without
// warranties or conditions of any kind, either express or implied.

package com.example.analyzer.server.expression;

public class ParserException extends RuntimeException
{
  public ParserException(String expression, int currentOffset, char c)
  {
    super("Invalid character '" + (c == Parser.ETX ? "End of Text" : c) + "' at position " + currentOffset + " in expression " + expression);
  }

  public ParserException(String message)
  {
    super(message);
  }
}
