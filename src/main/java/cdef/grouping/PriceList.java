package cdef.grouping;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

public class PriceList {
	private static List<HashMap<String, String>> priceList = new ArrayList<HashMap<String, String>>();
	
	public PriceList(String ticker, String date) {
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
		input.put("TC", ticker);
		input.put("TD", date);

		priceList = session.selectList("Mapper.pricelist", input);
		
		session.commit();
		session.close();
	}
	
	public List<HashMap<String, String>> getPriceList() {
		return priceList;
	}
}
