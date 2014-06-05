// Copyright 2010 Anthony F. Stuart - All rights reserved.
//
// This program and the accompanying materials are made available
// under the terms of the GNU General Public License. For other license
// options please contact the copyright owner.
//
// This program is made available on an "as is" basis, without
// warranties or conditions of any kind, either express or implied.

package com.example.analyzer.server.expanders.date;

import java.sql.Date;
import java.util.GregorianCalendar;

import com.example.analyzer.server.expanders.DbIdentityColumnExpanderField;


public class DbDateIdentityColumnExpanderField extends DbIdentityColumnExpanderField
{

  public DbDateIdentityColumnExpanderField(int type)
  {
    super(type);
  }

  @Override
  public Object getValue(Object sourceValue)
  {
    // TODO: See if there is a better way to return the "identity" value
    return new Date(((GregorianCalendar)sourceValue).getTimeInMillis());
    //return sourceValue;
  }

  
}
