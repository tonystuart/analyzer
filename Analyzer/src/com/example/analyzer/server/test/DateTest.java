// Copyright 2010 Anthony F. Stuart - All rights reserved.
//
// This program and the accompanying materials are made available
// under the terms of the GNU General Public License. For other license
// options please contact the copyright owner.
//
// This program is made available on an "as is" basis, without
// warranties or conditions of any kind, either express or implied.

package com.example.analyzer.server.test;

import java.util.GregorianCalendar;

public class DateTest
{

  public static void main(String[] args)
  {
    GregorianCalendar unixBase = new GregorianCalendar(1970, 1, 1);
    long unixMillis = unixBase.getTimeInMillis();
    
    GregorianCalendar epochBase = new GregorianCalendar(1, 1, 1);
    long epochMillis = epochBase.getTimeInMillis();
    
    GregorianCalendar now = new GregorianCalendar();
    long nowMillis = now.getTimeInMillis();
    
    long deltaMillis = nowMillis - epochMillis;
    
    long millisPerSecond = 1000;
    long millisPerMinute = millisPerSecond * 60;
    long millisPerHour = millisPerMinute * 60;
    long millisPerDay = millisPerHour * 24;
    long millisPerYear = millisPerDay * 365;
    
    System.out.println((double)deltaMillis / millisPerYear);
    System.out.println((double)unixMillis / millisPerYear);
    System.out.println((double)epochMillis / millisPerYear);
    System.out.println((double)nowMillis / millisPerYear);
  }

}
