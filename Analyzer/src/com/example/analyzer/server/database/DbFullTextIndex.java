// Copyright 2010 Anthony F. Stuart - All rights reserved.
//
// This program and the accompanying materials are made available
// under the terms of the GNU General Public License. For other license
// options please contact the copyright owner.
//
// This program is made available on an "as is" basis, without
// warranties or conditions of any kind, either express or implied.

package com.example.analyzer.server.database;

import java.io.IOException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

public class DbFullTextIndex
{
  public static final String ID = "id";
  public static final String VALUE = "value";

  private RAMDirectory ramDirectory;

  public static byte[] getBytes(int i)
  {
    return new byte[] {
        (byte)((i >> 24) & 0xff),
        (byte)((i >> 16) & 0xff),
        (byte)((i >> 8) & 0xff),
        (byte)(i & 0xff)
    };
  }

  public static int getInt(byte[] buffer)
  {
    return ((buffer[0] & 0xff) << 24) | ((buffer[1] & 0xff) << 16) | ((buffer[2] & 0xff) << 8) | (buffer[3] & 0xff);
  }

  public DbFullTextIndex(DbTable dbTable, int columnOffset)
  {
    try
    {
      long beginTime = System.currentTimeMillis();
      ramDirectory = new RAMDirectory();
      IndexWriter writer = new IndexWriter(ramDirectory, new StandardAnalyzer(Version.LUCENE_30), new MaxFieldLength(50));
      int rowCount = dbTable.getRowCount();
      for (int rowOffset = 0; rowOffset < rowCount; rowOffset++)
      {
        String value = dbTable.coalesce(rowOffset, columnOffset, "").toString();
        byte[] idArray = getBytes(rowOffset);
        Document document = new Document();
        document.add(new Field(ID, idArray, Field.Store.YES));
        document.add(new Field(VALUE, value, Store.YES, Index.ANALYZED)); // TODO: Determine whether we need to store value
        writer.addDocument(document);
      }
      writer.optimize();
      writer.close();
      long endTime = System.currentTimeMillis();
      long elapsedTime = endTime - beginTime;
      System.out.println("created index in " + elapsedTime + " ms");
    }
    catch (CorruptIndexException e)
    {
      throw new RuntimeException(e);
    }
    catch (LockObtainFailedException e)
    {
      throw new RuntimeException(e);
    }
    catch (IOException e)
    {
      throw new RuntimeException(e);
    }
  }

  public void search(String searchText, DbFullTextCollector collector)
  {
    try
    {
      long beginTime = System.currentTimeMillis();
      IndexReader reader = IndexReader.open(ramDirectory, true);
      Searcher searcher = new IndexSearcher(reader);
      collector.setSearcher(searcher);
      QueryParser queryParser = new QueryParser(Version.LUCENE_30, VALUE, new StandardAnalyzer(Version.LUCENE_30));
      Query query = queryParser.parse(searchText);
      searcher.search(query, collector);
      searcher.close();
      long endTime = System.currentTimeMillis();
      long elapsedTime = endTime - beginTime;
      System.out.println("executed query in " + elapsedTime + " ms");
    }
    catch (CorruptIndexException e)
    {
      throw new RuntimeException(e);
    }
    catch (IOException e)
    {
      throw new RuntimeException(e);
    }
    catch (ParseException e)
    {
      throw new RuntimeException(e);
    }
  }

}
