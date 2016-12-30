package cdef.crawling;

import java.io.File;
import java.io.FileWriter;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import cdef.db.SessionFactory;
import cdef.grouping.CodeList;
import cdef.grouping.Code;

public class nowNewsCrawling{
	private static String lastHref = "null";
	public void startCrawling()
    {
    	CodeList codeList = new CodeList();
    	List<Code> crp_cd = codeList.getCodeList();
    	Iterator<Code> itrC = crp_cd.iterator();

    	itrC = crp_cd.iterator();
    	while (itrC.hasNext()) {
        	crawling(itrC.next());
        }
    }
	
	private static void crawling(Code code) {
    	SessionFactory sf = new SessionFactory();
    	String urlstr = null;
    	
    	try {
    		Date d = new Date();
    		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
    		String today = sdf.format(d);
    		int flag = 0;
    
	        ArrayList<String> hrefList = new ArrayList<String>();
        	ArrayList<String> dateList = new ArrayList<String>();
        	ArrayList<String> officeList = new ArrayList<String>();
	        urlstr = "http://finance.naver.com/item/news_news.nhn?code="+ code.ticker;
	        //System.out.println(urlstr);
	        URL url = new URL(urlstr);
	        Document doc = Jsoup.parse(url.openStream(), "EUC_KR", urlstr); 	
	        	
			Elements datas = doc.select("table.type2 tr td");
			Iterator<Element> iterElem = datas.iterator();
				
			while(iterElem.hasNext()) {
				Element data = iterElem.next();
				if(data.text().equals("") || data.text().equals(" "))
					continue;
				String date = data.text().split(" ")[0].replace(".", "");
				
				Element title = iterElem.next();
				String titleText = title.text();
				String titleHref = title.select("a").attr("href");
				String office = iterElem.next().text();
				
				if(titleText.indexOf(code.name) == -1) {
					flag++;
					//System.out.println(flag);
					if(flag >=  10)
						return;
					continue;
				}
				
				dateList.add(date);
				hrefList.add(titleHref);
				officeList.add(office);
			}
				
			Iterator<String> hrefIter = hrefList.iterator();
			Iterator<String> dateIter = dateList.iterator();
			
			while(hrefIter.hasNext()) {
				String date = dateIter.next();
				String href = hrefIter.next();
				if (Integer.parseInt(today) > Integer.parseInt(date))
					return;
				if (lastHref.equals(href)) 
					return;
				
				urlstr = "http://finance.naver.com" + href;
				//System.out.println(urlstr);
				url = new URL(urlstr);
				doc = Jsoup.parse(url.openStream(), "EUC_KR", urlstr); 
				Element data = doc.select("div#news_read").first();
				data.select("a").remove();
				data.select("h3").remove();
				
				String title = doc.select("strong.c.p15").text();
				String content = data.text();
	        	
				//System.out.println(date);
				//System.out.println(content);
				sf.demoNEWS(code.ticker, date, content, title);
				lastHref = href;
			}
        } catch(Exception e) {
        	e.printStackTrace();
        	try {
				FileWriter fw = new FileWriter(new File("NewsError.txt"), true);
				fw.write(urlstr + "\r\n");
				fw.flush();
				fw.close();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
        }
    }
}
