package cdef.crawling;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
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

public class nowNewFCrawling{
	public void startCrawling() {
		while(true) {
			crawling();
		}
	}

	private static void crawling() {
		String URL = "http://dart.fss.or.kr/dsac001/mainK.do";
		try {
        	URL url = new URL(URL);
        	
        	Document doc = Jsoup.parse(url.openStream(), "UTF-8", URL);
        	Elements datas = doc.select("table tr td a");
			Iterator<Element> dataIter = datas.iterator();
        	
			while(dataIter.hasNext()) {
				//System.out.println(iterRpt.next().text());
				String name = dataIter.next().text();
				Element title = dataIter.next();
				if(title.text().equals(""))
					title = dataIter.next();
				String titleText = title.text();
				String titleHref = title.attr("href");
				String rcp_no = titleHref.substring(23);
				//System.out.println(text);
				//System.out.println(rcp_no);
				if((titleText.indexOf("신규시설") != -1) && (titleText.indexOf("첨부정정") == -1)) {
					//System.out.println(rcp_no);
					URL = "http://dart.fss.or.kr" + titleHref;
					String ticker = findTicker(name);
					if(titleText.indexOf("기재정정") != -1)
						deleteBefore(URL, ticker);
					URL = findDcmNo(URL, rcp_no);
					//System.out.println(URL)
					
        			if ( URL != null && ticker != null ) 
                		crawlingThePage(URL, ticker, rcp_no.substring(0, 8));
				}
			}
        } catch(Exception e) {
        	e.printStackTrace();
        	try {
				FileWriter fw = new FileWriter(new File("NewFError.txt"), true);
				fw.write(URL + "\r\n");
				fw.flush();
				fw.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
        }
	}

	private static String findDcmNo(String URL, String rcpNo) {
		try {
        	URL url = new URL(URL);
        	
        	BufferedReader bf;
        	String line;
        	
        	bf = new BufferedReader(new InputStreamReader(url.openStream()));
        	
        	while ((line = bf.readLine()) != null) {
        		if((line.indexOf("onclick") != -1) && (line.indexOf("openPdfDownload") != -1)) {
        			int index = line.indexOf("openPdfDownload");
        			String dcmNo = line.substring(index+35, index+42);
        			//System.out.println(dcmNo);
        			return "http://dart.fss.or.kr/report/viewer.do?rcpNo="+rcpNo+
        					"&dcmNo="+dcmNo+"&eleId=0&offset=0&length=0&dtd=HTML";
        		}
        	}
    
        } catch(Exception e) {
        	e.printStackTrace();
        }
		return null;
	}
	
	private static void crawlingThePage(String URL, String ticker, String date) {
		SessionFactory sf = new SessionFactory();
		
		try {
			URL url = new URL(URL);
			Document doc = Jsoup.parse(url.openStream(), "EUC_KR", URL);
			Elements datas = doc.select("table#XFormD1_Form0_Table0 tbody tr td span");
			Iterator<Element> iterElem = datas.iterator();
			
			System.out.println(date);
			while(iterElem.hasNext()) {
				String before = iterElem.toString();
				String next = iterElem.next().text();
				
				if(next.equals("3. 투자목적") && before.equals(next) == false) {
					sf.demoNewF(ticker, date, iterElem.next().text());
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static String findTicker(String name) {
		String resource = "cdef/sql/mybatis-config.xml";
		
		Properties props = new Properties();
		
		props.put("driver", "com.microsoft.sqlserver.jdbc.SQLServerDriver");
		props.put("url", "jdbc:sqlserver://106.249.235.42:1433;databasename=DBSUPER");
		props.put("username", "testing");
		props.put("password", "test1234");
		
		SqlSession session = null;
		
		try {
			InputStream inputStream = Resources.getResourceAsStream(resource);
			SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream, props);
			
			session = sqlSessionFactory.openSession(false);
		} catch(IOException e) {
			e.printStackTrace();
			return null;
		}
		
		String ticker = session.selectOne("Mapper.findTicker");
		
		session.commit();
		session.close();
		
    	return ticker;
	}
	
	private static void deleteBefore(String URL, String TC) {
		String TD = null;
		
		try {
    		URL url = new URL(URL);
    	
    		Document doc = Jsoup.parse(url.openStream(), "UTF-8", URL);
    		Elements datas = doc.select("select#family option[value^=rcpNo]");
    		
    		TD = datas.get(1).text().split(" ")[0].replace(".", "");
    	} catch(Exception e) {
    		e.printStackTrace();
    	}
		
		String resource = "cdef/sql/mybatis-config.xml";
		
		Properties props = new Properties();
		
		props.put("driver", "com.microsoft.sqlserver.jdbc.SQLServerDriver");
		props.put("url", "jdbc:sqlserver://106.249.235.42:1433;databasename=DBSUPER");
		props.put("username", "testing");
		props.put("password", "test1234");
		
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
		
		session.delete("Mapper.delete", input);
		
		session.commit();
		session.close();
	}
}
