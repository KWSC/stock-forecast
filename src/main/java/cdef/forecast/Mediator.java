package cdef.forecast;

import cdef.crawling.nowNewFCrawling;
import cdef.crawling.nowNewsCrawling;
import cdef.crawling.nowStockCrawling;
import cdef.dictionary.DicMain;
import cdef.grouping.Sub;

public class Mediator {
	//Crawling Class 객체 선언
	nowNewFCrawling fc = new nowNewFCrawling();
	nowNewsCrawling nc = new nowNewsCrawling();
	nowStockCrawling sc = new nowStockCrawling();
	
	DicMain rmain = new DicMain();	// Parsing
	Sub sub = new Sub();
	
	public void training() {
		//sc.startCrawling();
		rmain.training();		// Algorithm
		sub.startGrouping();	//Grouping 과정 method 호출
	}
	
	public void testing() {
		nc.startCrawling();
		rmain.testing();		// Algorithm
	}
}