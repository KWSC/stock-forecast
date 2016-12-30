package cdef.forecast;

import java.util.*;

public class JavaTimer{
	static Mediator mediat = new Mediator();
	public static void main(String args[]){
		Timer timer = new Timer();
		// 무엇을, 언제부터, 몇 간격으로 (1000이 1초)
		timer.schedule(new TestTask(), 1000, 30*60*1000);
		timer.schedule(new TrainTask(), 0/*프로그램 시작으로 부터 장 마감 시점 까지 시간 간격 */, 24*60*60*1000);
	}
	
	public static class TestTask extends TimerTask{
	@Override
		public void run() {
			// TODO Auto-generated method stub
			//testing 시간인 경우 
			mediat.testing();	
		}
	}
	
	public static class TrainTask extends TimerTask{
		@Override
		public void run() {
			// TODO Auto-generated method stub
			//training 시간인 경우
			mediat.training();
		}
		
	}
}
