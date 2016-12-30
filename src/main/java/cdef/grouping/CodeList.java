package cdef.grouping;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

public class CodeList {
	private static List<Code> codeList = new ArrayList<Code>();
	private Code code = null;
	private int index = 0;
	
	public CodeList() {
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
		
		codeList = session.selectList("Mapper.list");
		
		session.commit();
		session.close();
	}
	
	public List<Code> getCodeList() {
		return codeList;
	}
	
	public synchronized Code getCode() {
		try {
			code = codeList.get(index);
			index++;
		} catch(IndexOutOfBoundsException e) {
			code = null;
		}
		//System.out.println(index);
		return code;
	}
	
	public synchronized int getIndex() {
		return index;
	}
}

