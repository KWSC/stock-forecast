package cdef.dictionary;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.Analyzer.TokenStreamComponents;
import org.apache.lucene.analysis.core.LowerCaseTokenizer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.ko.KoreanAnalyzer;
import org.apache.lucene.analysis.ko.morph.AnalysisOutput;
import org.apache.lucene.analysis.ko.morph.MorphAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;
import org.rosuda.JRI.RVector;
import org.rosuda.JRI.Rengine;
import org.rosuda.JRI.REXP;
import org.rosuda.REngine.REXPMismatchException;

import cdef.redshift.redShift;


public class DicMain {
	long start = System.currentTimeMillis();
	long early = 0;
	long mid = 0;
	
	KoAnalyzer analyzer = new KoAnalyzer();
	RConnect R = new RConnect();
	
	public void training() {
		String[] origin = null;
		
		int linenum = 0;
		int index = 0;
		int[] today = {0};
		int[] tomorrow = {0};
		LayoutList ll = new LayoutList("training");
		List<Layout> news = ll.getLayoutList();
		Iterator<Layout> IterNews = news.iterator();
		
		linenum = news.size();
		
		String line = null ;
		today = new int[linenum];
		tomorrow = new int[linenum];
		origin = new String[linenum];
		while(IterNews.hasNext()) {
			Layout item = IterNews.next();
			
			line = item.article_content;
			today[index] = Integer.parseInt(item.today_stock.replace(" ", ""));
			tomorrow[index] = Integer.parseInt(item.tomorrow_stock.replace(" ", ""));
			
		//	System.out.println("line # : " + index);
			line = line.replaceAll("\\(", " ");
		    line = line.replaceAll("\\)", " ");
		    line = line.replaceAll("\\-", " ");
		    line = line.replaceAll("\\[(.*?)\\]", "").
					replaceAll("[^\u0030-\u0039\u0041-\u005a\u0061-\u007a\u3130-\u318f\uac00-\ud7af]", " ");
			origin[index] = analyzer.analyze(line);
			
			index++;
		
		}
		R.setOrigin(origin);
		R.setToday(today);
		R.setTomorrow(tomorrow);
		
		R.makeKeyword(); 	// Keyword추출
		R.getKeyword();
		
		String resource = "cdef/sql/mybatis-config.xml";
        
	      Properties props = new Properties();
	            
	      props.put("driver", "com.amazon.redshift.jdbc42.Driver");
	      props.put("url", "jdbc:redshift://priceforecast.cypkj43qbvcd.ap-northeast-2.redshift.amazonaws.com:5439/priceforecast");
	      props.put("username", "cdefinition");
	      props.put("password", "cdForecast1#");
	            
	      SqlSession session = null;
	
	      try {
	 			InputStream inputStream = Resources.getResourceAsStream(resource);
	 			SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream, props);
	 			
	 			session = sqlSessionFactory.openSession(false);
	 		} catch(IOException e) {
	 			e.printStackTrace();
	 			return;
	 		}
	      
	      List<ElasticElement> ela_keyword = session.selectList("Mapper.getEla");
	      R.set_elaKeyword(ela_keyword);
	      for(int i = 0; i < ela_keyword.size(); i++){
	    	  System.out.print(ela_keyword.get(i).getKeyword() + " "); 
	      }
		for(int i = 0; i < origin.length; i++){
			System.out.println(i);
			if(!origin[i].isEmpty()){
				String tmp = analyzer.analyze(origin[i]);
				R.readyData(R.getScore2(tmp,i));
		
			}
			else continue;
		}
			
		R.createFrm();
		early = System.currentTimeMillis();
		R.setTraining(); 
	}
	
	public void testing() {
		String[] origin = null;
		
		int linenum = 0;
		int index = 0;
		int[] today = {0};
		int[] tomorrow = {0};
		LayoutList ll = new LayoutList("testing");
		List<Layout> news = ll.getLayoutList();
		Iterator<Layout> IterNews = news.iterator();
		
		linenum = news.size();
		
		String line = null ;
		today = new int[linenum];
		tomorrow = new int[linenum];
		origin = new String[linenum];
		while(IterNews.hasNext()) {
			Layout item = IterNews.next();
			
			line = item.article_content;
			today[index] = Integer.parseInt(item.today_stock.replace(" ", ""));
			tomorrow[index] = Integer.parseInt(item.tomorrow_stock.replace(" ", ""));
			
		//	System.out.println("line # : " + index);
			line = line.replaceAll("\\(", " ");
		    line = line.replaceAll("\\)", " ");
		    line = line.replaceAll("\\-", " ");
		    line = line.replaceAll("\\[(.*?)\\]", "").
					replaceAll("[^\u0030-\u0039\u0041-\u005a\u0061-\u007a\u3130-\u318f\uac00-\ud7af]", " ");
			origin[index] = analyzer.analyze(line);
			
			index++;
		}
		R.setOrigin(origin);
		R.setToday(today);
		R.setTomorrow(tomorrow);
		
		R.getKeyword();
		
		String resource = "cdef/sql/mybatis-config.xml";
        
	      Properties props = new Properties();
	            
	      props.put("driver", "com.amazon.redshift.jdbc42.Driver");
	      props.put("url", "jdbc:redshift://priceforecast.cypkj43qbvcd.ap-northeast-2.redshift.amazonaws.com:5439/priceforecast");
	      props.put("username", "cdefinition");
	      props.put("password", "cdForecast1#");
	            
	      SqlSession session = null;
	
	      try {
	 			InputStream inputStream = Resources.getResourceAsStream(resource);
	 			SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream, props);
	 			
	 			session = sqlSessionFactory.openSession(false);
	 		} catch(IOException e) {
	 			e.printStackTrace();
	 			return;
	 		}
	      
	      List<ElasticElement> ela_keyword = session.selectList("Mapper.getEla");
	      R.set_elaKeyword(ela_keyword);
	      for(int i = 0; i < ela_keyword.size(); i++){
	    	  System.out.print(ela_keyword.get(i).getKeyword() + " "); 
	      }
		for(int i = 0; i < origin.length; i++){
			System.out.println(i);
			if(!origin[i].isEmpty()){
				String tmp = analyzer.analyze(origin[i]);
				R.readyData(R.getScore2(tmp,i));
		
			}
			else continue;
		}
			
		R.createFrm();
		String rst[] = R.svm();
		
		HashMap<String, String> input = new HashMap<String, String>();
		
		if(rst.length != 0)
			redShift.delete();
		//session.delete("Mapper.lastClear");
		
		for(int i = 0 ; i < rst.length ; i++) {
			input.put("TC", news.get(i).ticker);
			String name = session.selectOne("Mapper.findName", input);
			input.put("NAME", name);
			input.put("CO", news.get(i).article_content);
			input.put("RE", rst[i].substring(0, 3));
			
			redShift.insert(input);
			//session.insert("Mapper.result", input);
			
			System.out.println(input.toString());
		}
		mid = System.currentTimeMillis();
		
		double time = (early - start) / 1000.0;
		double time2 = (mid - start ) / 1000.0;
		System.out.println("training + testing time : " +(mid-start) / 1000.0);
		long end = System.currentTimeMillis();
		 FileWriter fw2;
			try {
				fw2 = new FileWriter("/home/forecast/elasvm_time.txt");
				 fw2.write("학습전 : " + time + " 학습 후 : " + time2);
		           fw2.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		System.out.println((end-start) / 1000.0);
	}
}
