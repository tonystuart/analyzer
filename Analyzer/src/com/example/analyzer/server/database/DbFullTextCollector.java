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

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Searcher;

public abstract class DbFullTextCollector extends Collector
{
  protected Searcher searcher;

  public Searcher getSearcher()
  {
    return searcher;
  }

  public void setSearcher(Searcher searcher)
  {
    this.searcher = searcher;
  }

  @Override
  public void setScorer(Scorer scorer) throws IOException
  {
  }

  @Override
  public void setNextReader(IndexReader reader, int docBase) throws IOException
  {
  }

  @Override
  public boolean acceptsDocsOutOfOrder()
  {
    return true;
  }

}
