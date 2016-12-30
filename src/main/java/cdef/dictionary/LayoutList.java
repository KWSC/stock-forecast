package cdef.dictionary;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import cdef.grouping.Code;
import cdef.grouping.TestList;


public class LayoutList {
	private List<Layout> layoutList = new ArrayList<Layout>();
	
	public LayoutList(String flag) {
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
	      
	      Date d = new Date();
	      SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
	      String TD = sdf.format(d);
	      
	      HashMap<String, String> input = new HashMap<String, String>();
	      input.put("TD", TD);
	      
	      if (flag.equals("training"))
	    	  layoutList = session.selectList("Mapper.articleList");
	      else {
		      List<Code> testList = TestList.getTestList();
		      String value = "ticker=";
		      
		      //System.out.println(testList.size());
		      for(int i = 0 ; i < testList.size() ; i++) {
		    	value += "\'" + testList.get(i).ticker + "\'or ticker=";
		      }
		      value = value.substring(0, value.length()-10);
		      System.out.println(value);
		      input.put("value", value);
	    	  layoutList = session.selectList("Mapper.todayArticle", input);
	      }
	      
	      session.commit();
	      session.close();
	}
	
	public List<Layout> getLayoutList() {
		return layoutList;
	}
}
