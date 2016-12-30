package cdef.grouping;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class Sub{
	
	public void startGrouping()
    {
		CodeList codeList = new CodeList();
		Code code = null;
		TestList tl = new TestList();
    	
    	while((code = codeList.getCode()) != null){
    		//System.out.println(id);
    		Date d = new Date();
  	      	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
  	      	String TD = sdf.format(d);
    		PriceList pl = new PriceList(code.ticker, TD);
    		List<HashMap<String, String>> crp_pl = pl.getPriceList();
        	Iterator<HashMap<String, String>> itrP = crp_pl.iterator();
        	/*
        	 * 계산하는 함수 호출
        	 */
        	MakeStockIndex makeStockIndex = new MakeStockIndex(itrP);
 
        	//데이터 정형화
        	
        	//5일선, 20일선 계산
    		makeStockIndex.movingAverage();
    		//12일, 26일 지수이동평균선 계산 & MACD 계산
    		makeStockIndex.MACD();		
    		//signal 9 계산
    		//makeData.signal9();  //MACD 계산 끝나고 해야 해서 MACD()에서 호출
    		//스토캐스틱 계산
    		makeStockIndex.stocastic();
    		//그룹핑
    		makeStockIndex.grouping(code, tl);
    		
    		//if(code.name.equals("에스앤씨엔진그룹"))
    		 //  break;
        	
    	}
        
        
    }

}
