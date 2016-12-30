package cdef.dictionary;
public class Layout {
	String ticker;
	String article_content;
	String today_stock;
	String tomorrow_stock;
	
	public Layout(String ticker, String content, String today, String tomorrow) {
		this.ticker = ticker;
		this.article_content = content;
		this.today_stock = today;
		this.tomorrow_stock = tomorrow;
	}
}
