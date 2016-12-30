package cdef.db;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

public class SessionFactory {
	public void demoCP(String TC, String TD, String CP, String HP, String LP) throws IOException {
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
		HashMap<String, String> input = new HashMap<String, String>();
		input.put("TC", TC);
		input.put("TD", TD);
		input.put("CP", CP);
		input.put("HP", HP);
		input.put("LP", LP);

		int result = session.insert("Mapper.addCP", input);
		
		if (result > 0) {
			session.commit();
			session.close();
		}
		else {
			session.rollback();
			System.out.println("추가실패");
		}
	}
	
	public synchronized void demoNEWS(String TC, String TD, String CN, String title) {
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
		HashMap<String, String> input = new HashMap<String, String>();

		input.put("TC", TC);
		input.put("TD", TD);
		input.put("CN", CN + title);
		input.put("TDS", "0");
		input.put("TMS", "0");
		
		int result = session.insert("Mapper.addNews", input);
		System.out.println(TC + "\t" + TD + "\t" + CN + "\t0\t0");
		
		if (result > 0) {
			session.commit();
			session.close();
		}
		else {
			session.rollback();
			System.out.println("추가실패");
		}
		return;
	}
	
	public void demoNewF(String TC, String TD, String add) {
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
		
		String outputYesterday = null;
		String outputToday = null;;
		String outputTomorrow = null;
		
		HashMap<String, String> input = new HashMap<String, String>();
		input.put("TC", TC);
		input.put("TD", TD);

		outputYesterday = session.selectOne("Mapper.yesterday", input);
		outputToday = session.selectOne("Mapper.today", input);
		outputTomorrow = session.selectOne("Mapper.tomorrow", input);
		//System.out.println(IC + "\t" + TD + "\t" + add + "\t" + outputYesterday + "\t" + outputToday);
		
		String todayStock = null;
		String tomorrowStock = null;
		
		if (outputYesterday != null && outputToday != null && outputTomorrow != null) {
			outputYesterday = outputYesterday.replaceAll(" ", "");
			outputToday = outputToday.replaceAll(" ", "");
			outputTomorrow = outputTomorrow.replaceAll(" ", "");
			
			if (Integer.parseInt(outputYesterday) < Integer.parseInt(outputToday))
				todayStock = "1";
			else if (Integer.parseInt(outputYesterday) == Integer.parseInt(outputToday))
				todayStock = "0";
			else
				todayStock = "-1";
			
			if (Integer.parseInt(outputToday) < Integer.parseInt(outputTomorrow))
				tomorrowStock = "1";
			else if (Integer.parseInt(outputToday) == Integer.parseInt(outputTomorrow))
				tomorrowStock = "0";
			else
				tomorrowStock = "-1";
			
			input = new HashMap<String, String>();
			input.put("TC", TC);
			input.put("TD", TD);
			input.put("IO", add);
			input.put("TDS", todayStock);
			input.put("TMS", tomorrowStock);
			
			int result = session.insert("Mapper.addNewF", input);
			System.out.println(TC + "\t" + TD + "\t" + add + "\t" + todayStock + "\t" + tomorrowStock);
			
			if (result > 0) {
				session.commit();
				session.close();
			}
			else {
				session.rollback();
				System.out.println("추가실패");
			}
			return;
		}
	}
}
