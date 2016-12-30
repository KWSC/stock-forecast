package cdef.grouping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class MakeStockIndex {
	
	//종가 & MACD & 최고가,최저가(for 스토캐스틱) 데이터
	ArrayList<Integer> closedPrice = new ArrayList<Integer>();  //종가 
	ArrayList<Integer> highPrice = new ArrayList<Integer>();    //최고가
	ArrayList<Integer> lowPrice = new ArrayList<Integer>();     //최저가
	ArrayList<Double> MACD = new ArrayList<Double>();  //MACD
	//double highPrice, lowPrice;  //15일동안의 최고가, 최저가
		
	//보조지표 만족하는지 flag
	int isMA=0, isMACD=0, isSignal9=0, isStocastic = 0;
		
	public MakeStockIndex(Iterator<HashMap<String, String>> itr){
		HashMap<String, String> price;
		while(itr.hasNext()){
			price = itr.next();
			closedPrice.add(Integer.parseInt(price.get("close_price").replaceAll(" ", "")));
			highPrice.add(Integer.parseInt(price.get("high_price").replaceAll(" ", "")));
			lowPrice.add(Integer.parseInt(price.get("low_price").replaceAll(" ", "")));
			//System.out.println(price.get("TRADE_DATE") + " " + closedPrice +" " + highPrice + " " + lowPrice);
		}

	}
	
	//5일선, 20일선 계산
	public void movingAverage(){
			ArrayList<Double> MA5 = new ArrayList<Double>();  //5일선
			ArrayList<Double> MA20 = new ArrayList<Double>();  //20일선
			double todayMA20=0, todayMA5 = 0;
			//boolean isGoldenCross = false;  //골든크로스 존재 여부
			
			for(int index = closedPrice.size()-20; index >= 0; index--){


				//5일선 계산
				todayMA5 = (closedPrice.get(index)
						+closedPrice.get(index+1)
						+closedPrice.get(index+2)
						+closedPrice.get(index+3)
						+closedPrice.get(index+4))/5;
				
				//20일선 계산
				todayMA20 = (closedPrice.get(index)+closedPrice.get(index+1)
				+closedPrice.get(index+2)+closedPrice.get(index+3)
				+closedPrice.get(index+4)+closedPrice.get(index+5)
				+closedPrice.get(index+6)+closedPrice.get(index+7)
				+closedPrice.get(index+8)+closedPrice.get(index+9)
				+closedPrice.get(index+10)+closedPrice.get(index+11)
				+closedPrice.get(index+12)+closedPrice.get(index+13)
				+closedPrice.get(index+14)+closedPrice.get(index+15)
				+closedPrice.get(index+16)+closedPrice.get(index+17)
				+closedPrice.get(index+18)+closedPrice.get(index+19))/20;
				
				//계산값 저장
				MA5.add(todayMA5);
				MA20.add(todayMA20);
											
				//골든크로스, 데드크로스 체크
				/* 5일선이 20일선 상승돌파 구간이 있다 & 골든 크로스 후 5일선>20일선 유지 */			
				//처음부터 말고,1일차 이상에서 골든크로스 존재
				// !!!!!!! 5일선 == 20일선 처리 !!!!!!
				if(index>0 && todayMA5>=todayMA20) {
					//isGoldenCross = true;
					isMA = 1;
				}
				else if(index>0 && todayMA5<todayMA20)                   
					isMA = 0;
			}
			/*
			 //일선 확인용 출력문
			for(int index =0; index<MA20.size(); index++){
				System.out.println(index + " : "+MA5.get(index) + "   " + MA20.get(index));
			}
			*/
			/*
			if(isMA==1)
				System.out.println("이동평균선");*/
		}
		
	//12일, 26일 지수이동평균선 계산 & MACD 계산
	public void MACD(){
			ArrayList<Double> EMA12 = new ArrayList<Double>();  //12일 지수이동평균
			ArrayList<Double> EMA26 = new ArrayList<Double>();  //26일 지수이동평균
			//ArrayList<Double> MACD = new ArrayList<>();  //MACD
			
			double avg12=0, avg26=0;
			//int timePeriod12 = 12;
			//int timePeriod26 = 26;
			
			for(int index=closedPrice.size()-1; index>=0; index--){

				if(index>=closedPrice.size() - 12){ //12,26일치 그냥 더하기
					avg12 += closedPrice.get(index);
					avg26 += closedPrice.get(index);				
					//System.out.println(closedPrice.get(index)+ " : " + avg12 + "  " + avg26);
				}
				else if(closedPrice.size() - 26<=index && index<closedPrice.size() - 12){ //12 지.수. 이동평균 계산
					if(index==closedPrice.size() - 12-1) EMA12.add(avg12/12);
					double lastEMA12 = EMA12.get(0);
					EMA12.add(0,(closedPrice.get(index)*2/13) + (lastEMA12*11/13));
					avg26 += closedPrice.get(index);
					
					//System.out.println(closedPrice.get(index)+ " :: " + EMA12.get(0)+"/"+ (closedPrice.get(index)*2/13)+ " + "+(lastEMA12*(1-(2/13))) + avg26);
				}
				else if(index<closedPrice.size() - 12){ //12, 26 지.수. 이동평균 계산
					if(index==closedPrice.size() - 26 - 1) EMA26.add(avg26/26);
					EMA12.add(0,(closedPrice.get(index)*2/13) + (EMA12.get(0)*11/13));
					EMA26.add(0,(closedPrice.get(index)*2/27) + (EMA26.get(0)*25/27));
					MACD.add(0,EMA12.get(0) - EMA26.get(0));
					//System.out.println(closedPrice.get(index)+ " ::: " + EMA12.get(0)+"/"+EMA12.get(1) + "  " + EMA26.get(0));
					
					//MACD > 0  확인
					if(MACD.get(0) >= 0) isMACD = 1;
					else if(MACD.get(0) < 0) isMACD = 0;
				}
			}
			/*
			//12,26 확인용 출력문
			for(int index =0; index<EMA26.size(); index++){
				System.out.println(index + " : "+EMA12.get(index)+"   "+EMA26.get(index));
			}*/
			/*
			 //MACD 확인용 출력문
			for(int index =0; index<MACD.size(); index++){
				System.out.println(index + " : "+MACD.get(index));
			}
			*/
			//MACD > 0 확인용 출력문
			/*
			if(isMACD == 1)
				System.out.println("MACD");
			*/
			//signal9 호출
			signal9();
		}
		
	//signal 9 계산
	private void signal9(){
			ArrayList<Double> signal9 = new ArrayList<Double>();  //signal 9
			
			int count=0;
			double avg = 0;
			
			for(int index=MACD.size()-1; index>=0 ; index--){
				count++;
				if(count<9){
					avg+=MACD.get(index);
				}
				else if(count==9){
					signal9.add(avg/9);
					signal9.add(0, (MACD.get(index)*2/10) + (signal9.get(0)*8/10));
					//System.out.println(MACD.get(index) + " : " + signal9.get(0));
				}
				else if(count>9){
					signal9.add(0, (MACD.get(index)*2/10) + (signal9.get(0)*8/10));
					//System.out.println(MACD.get(index) + " : " + signal9.get(0));
					
					//signal9 > 0 확인
					if(signal9.get(0) >= 0)        isSignal9 = 1;
					else if( signal9.get(0) < 0)  isSignal9 = 0;
		     	}
			}
			/*
			 //signal 9 확인용 출력문
			for(int index =0; index<signal9.size(); index++){
				System.out.println(index + " : "+signal9.get(index));
			}*/
			//signal 9 > 0 확인용 출력문
			/*
			if(isSignal9==1)
				System.out.println("Signal9");
				*/
		}
		
	//스토캐스틱 계산
	public void stocastic(){
			ArrayList<Double> stocastic = new ArrayList<Double>();
			
			double lowestPrice=0, highestPrice=0;
			int count=0;
			
			for(int index=closedPrice.size()-1; index>=0; index--){
				count++;
				/*
				if(highPrice < closedPrice.get(index))
					highPrice = closedPrice.get(index);
				else if(lowPrice > closedPrice.get(index))
					lowPrice = closedPrice.get(index);
				*/
				
				if(count>=15){	
					lowestPrice=lowPrice.get(index);
					highestPrice=highPrice.get(index);
					for(int index2 = index; index2<index+15; index2++){
						if(highestPrice < highPrice.get(index2))
							highestPrice = highPrice.get(index2);
						
						if(lowestPrice > lowPrice.get(index2))
							lowestPrice = lowPrice.get(index2);
					}
					//System.out.println(index+" low : "+lowPrice+", high : "+highPrice+", now = "+closedPrice.get(index));
					stocastic.add(0,100*((closedPrice.get(index)-lowestPrice) /(highestPrice - lowestPrice)));		
					
					//stocastic > 80 확인
					if(stocastic.get(0) >= 80) 		isStocastic = 1;
					else if(stocastic.get(0) < 80)  isStocastic = 0;
				}

			}
			/*
			 //스토캐스틱 확인용 출력문
			for(int index =0; index<stocastic.size(); index++){
				System.out.println(index + " : "+stocastic.get(index));
			}
			*/
			//stocastic > 80 확인용 출력문
			/*
			if(isStocastic==1)
				System.out.println("스토캐스틱");
				*/
		}
		
	//그룹핑
	public void grouping(Code nameCode, TestList tl){
			
			if(isMA + isMACD + isSignal9 + isStocastic == 4){
				//Great.add(nameCode);
				// class하나 만들어서 set(add) -> thread 때문
				tl.add(nameCode);
				System.out.println(nameCode + " : 4개 만족, 상");
			}
			else if(isMA + isMACD + isSignal9 + isStocastic == 3){
				//Best.add(nameCode);
				tl.add(nameCode);
				System.out.println(nameCode + " : 3개 만족, 중");
			}
			else if(isMA + isMACD + isSignal9 + isStocastic == 2){
				//Good.add(nameCode);
				tl.add(nameCode);
				System.out.println(nameCode + " : 2개 만족, 하");
			}
			else
				System.out.println(nameCode + " : 해당 없음");
		}

}
