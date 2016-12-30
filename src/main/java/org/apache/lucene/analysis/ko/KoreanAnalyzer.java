package org.apache.lucene.analysis.ko;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.standard.ClassicFilter;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.util.StopwordAnalyzerBase;

/**
 * A Korean Analyzer
 */
public class KoreanAnalyzer extends StopwordAnalyzerBase {
  
  /** Default maximum allowed token length */
  public static final int DEFAULT_MAX_TOKEN_LENGTH = 255;

  private int maxTokenLength = DEFAULT_MAX_TOKEN_LENGTH;

  private boolean bigrammable = false;
    
  private boolean hasOrigin = false;
    
  private boolean exactMatch = false;
  private boolean originCNoun = true;
  private boolean queryMode = false;
  private boolean wordSegment = false;
  
  /** An unmodifiable set containing some common words that are usually not useful for searching. */
  public static final CharArraySet STOP_WORDS_SET ;
  static Error err = new Error("Cannot load stop words");
  
  static Reader reader;

  static int data;
 
  static {
      try {
    	reader = new FileReader("/home/forecast/stopwords.txt");
    	STOP_WORDS_SET = loadStopwordSet(reader);
		//STOP_WORDS_SET = loadStopwordSet(false, KoreanAnalyzer.class, "stopwords.txt", "#");
	} catch (IOException e){
		
		/*try {
			reader = new FileReader("./resources/home/forecast/stopwords.txt");
			try {
				STOP_WORDS_SET = loadStopwordSet(reader);
			} catch (IOException e1) {
				throw err;
			}
		} catch (FileNotFoundException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		/*try {
			data = reader.read();
			while(data != -1){
			   char dataChar = (char) data;
			   data = reader.read();
			}
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		 */
		 throw err;
	}
  }
    
  public KoreanAnalyzer() {
    this(STOP_WORDS_SET);
    //File file = new File(".");  
    //System.out.println(file.getAbsolutePath());
  }

  /**
   * ��������� ������ ���������������
   */
  public KoreanAnalyzer(boolean exactMatch) {
    this(STOP_WORDS_SET);
    this.exactMatch = exactMatch;
  }
  
  public KoreanAnalyzer(String[] stopWords) {
    this(StopFilter.makeStopSet(stopWords));
  }

  public KoreanAnalyzer(Path stopwords) throws IOException {
    this(loadStopwordSet(stopwords));
  }

  public KoreanAnalyzer(Path stopwords, String encoding) throws IOException {
    this(loadStopwordSet(stopwords));
  }
    
  public KoreanAnalyzer(Reader stopwords) throws IOException {
    this(loadStopwordSet(stopwords));
  }

  public KoreanAnalyzer(CharArraySet stopWords) {
    super(stopWords);
  }


  @Override
  protected TokenStreamComponents createComponents(String s) {
    final KoreanTokenizer src = new KoreanTokenizer();
    TokenStream tok = new LowerCaseFilter(src);
    tok = new ClassicFilter(tok);
    tok = new KoreanFilter(tok, bigrammable, hasOrigin, exactMatch, originCNoun, queryMode);
    if(wordSegment) tok = new WordSegmentFilter(tok, hasOrigin);
    tok = new HanjaMappingFilter(tok);
    tok = new PunctuationDelimitFilter(tok);
    tok = new StopFilter(tok, stopwords);
    return new TokenStreamComponents(src, tok) {
      @Override
      protected void setReader(final Reader reader) throws IOException {
//        src.setMaxTokenLength(KoreanAnalyzer.this.maxTokenLength);
        super.setReader(reader);
      }
    };
  }

  /**
   * determine whether the bigram index term is returned or not if a input word is failed to analysis
   * If true is set, the bigram index term is returned. If false is set, the bigram index term is not returned.
   */
  public void setBigrammable(boolean is) {
    bigrammable = is;
  }
  
  /**
   * determin whether the original term is returned or not if a input word is analyzed morphically.
   */
  public void setHasOrigin(boolean has) {
    hasOrigin = has;
  }

  /**
   * determin whether the original compound noun is returned or not if a input word is analyzed morphically.
   */
  public void setOriginCNoun(boolean cnoun) {
    originCNoun = cnoun;
  }
  
  /**
   * determin whether the original compound noun is returned or not if a input word is analyzed morphically.
   */
  public void setExactMatch(boolean exact) {
    exactMatch = exact;
  }
  
  /**
   * determin whether the analyzer is running for a query processing
   */
  public void setQueryMode(boolean mode) {
    queryMode = mode;
  }

  /**
   * determin whether word segment analyzer is processing
   */
	public boolean isWordSegment() {
		return wordSegment;
	}
	
	public void setWordSegment(boolean wordSegment) {
		this.wordSegment = wordSegment;
	}


}

