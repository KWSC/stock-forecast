package cdef.crawling;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import cdef.db.SessionFactory;
import cdef.grouping.Code;
import cdef.grouping.CodeList;

public class nowStockCrawling
{
	private static String lastDate = null;
    public void startCrawling()
    {
    	CodeList codeList = new CodeList();
		List<Code> crp_cd = codeList.getCodeList();
		Iterator<Code> itrC = crp_cd.iterator();

		lastDate = findLastDate();
    	itrC = crp_cd.iterator();
    	while (itrC.hasNext()) {
        	crawling(itrC.next());
        }
    }
    
	private static void crawling(Code code) {
		SessionFactory sf = new SessionFactory();
		String URL = null;
		
		try {
			URL = "http://finance.naver.com/item/sise_day.nhn?code=" + code.ticker;
			URL url = new URL(URL);
			System.out.println(URL);
			
			Document doc = Jsoup.parse(url.openStream(), "EUC_KR", URL);
			Elements stocks = doc.select("table.type2 tr td");
			Iterator<Element> stock = stocks.iterator();
			String row[] = new String[7];
				
			int i = 0;
			
			while(stock.hasNext()) {
				String item = stock.next().text().replace(".", "").replace(",", "");
				//System.out.println(item);
					
				if (item.equals("") || item.equals(" "))
					continue;

				row[i%7] = item;
					
				if((i+1)%7 == 0) {
					if (Integer.parseInt(lastDate) >= Integer.parseInt(row[0]))
						return;
					for(int j = 0 ; j < 7 ; j++)
						System.out.print(String.valueOf(row[j]) + "\t");
					System.out.println();
					sf.demoCP(code.ticker, row[0], row[1], row[4], row[5]);
				}
				i++;
			}
		} catch (Exception e) {
			e.printStackTrace();
			try {
				FileWriter fw = new FileWriter(new File("StockError.txt"), true);
				fw.write(URL + "\r\n");
				fw.flush();
				fw.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}
	
	private static String findLastDate() {
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
			return null;
		}
		
		String lastDate = session.selectOne("Mapper.find");
		
		session.commit();
		session.close();
		
    	return lastDate;
    }
}
