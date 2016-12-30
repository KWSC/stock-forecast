package cdef.dictionary;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.rosuda.JRI.RVector;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngine;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;

//ElasticNet's result set Class
//later this element sets are in ArrayList(elasticList) to insert DB
class ElasticElement{
	private String keyword;
	private String value;

	
	public ElasticElement(String keyword, String value){
	   this.keyword = keyword;
	   this.value = value;
	}
	public String getKeyword(){
	   return keyword;
	}
	public String getValue(){
	   return value;
	}
}
public class RConnect {

	private String[] origin;
	private RConnection connection;
	REXP x;
	public String[] Keyword;
	int rowN; 
	private int[] today;
	private int[] tomorrow;
	private String[] ela_K;
	private double[] ela_Kvalue;
	public String what;
	
	/* Create a connection to Rserve instance running
     * on default port 6311
     */	
	public void setOrigin(String[] input) {
		this.origin = input;
	}
	public RConnect(){;
			try {
				connection = new RConnection();
	
				System.out.println("Creating Rengine (with arguments)");	         
		         connection.setStringEncoding("utf8");
			} catch (RserveException e) {
				System.out.println("Fail creating Rengine:" +e.getMessage());
			}
	}
	public void makeKeyword(){
		
		// Keyword 뽑아내기 용 코드
	    try {
			
	    	// Keyword라는 변수에 origin이라는 String 넣기
	         try {
				connection.assign("Keyword", origin);
			} catch (REngineException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	         //x = connection.eval("Keyword <- \"" + origin + "\"");
	         x = connection.eval("Keyword <- gsub(\"[a-z]\",\"\",Keyword)");
	         x = connection.eval("Keyword <- gsub(\"[A-Z]\",\"\",Keyword)");
	         x = connection.eval("Keyword <- gsub(\"[[:punct:]]\",\"\",Keyword)");
	         x = connection.eval("Keyword <- gsub(\"[[:cntrl:]]\",\"\",Keyword)");
	         x = connection.eval("Keyword <- gsub(\"\\\\d+\",\"\",Keyword)");
	         x = connection.eval("Keyword <- gsub(\"\",\"\",Keyword)");
	         x = connection.eval("Keyword <- unlist(strsplit(Keyword,\" \"))");
	         x = connection.eval("KeySet <- Filter(function(x){ nchar(x) >= 2} , Keyword)");
	         x = connection.eval("KeySet <- table(Keyword[which(Keyword != \"\")]) ");//
	         //x = connection.eval("KeySet <- table(Keyword)");
	         x = connection.eval("KeySet <- head(sort(unlist(KeySet),decreasing=T),100)");
	         x = connection.eval("KeySet <- names(KeySet)");
	         x = connection.eval("KeySet[length(KeySet) + 1] <- \"result\"");
            
	         x = connection.eval("KeySet");
	       Keyword = connection.eval("KeySet").asStrings();
	      
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
	 		
	 		String value = "";
	 		for(String i : Keyword) {
	 			value += "(\'" + i + "\'" + ",\'0\'),";
	 		}
	 		
	 		value = value.substring(0, value.length() - 1);
//	 		System.out.println(value);
	 		HashMap<String, String> input = new HashMap<String, String>();
			input.put("VAL", value);
			
			//System.out.println(input);
			
			session.delete("Mapper.clear");
			int result = session.insert("Mapper.keyword", input);
			
			if (result > 0) {
				session.commit();
				session.close();
			}
			else {
				session.rollback();
				System.out.println("추가실패");
			}
       
	    } catch (RserveException e) {
	          e.printStackTrace();
	    } catch (REXPMismatchException e) {
	          e.printStackTrace();
	    }
	    
		try {
			connection.assign("ready", "KeySet");
		} catch (RserveException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String[] getKeyword() {
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
		
 		List<String> tmp =  session.selectList("Mapper.getKeyword");
 		Keyword = tmp.toArray(new String[tmp.size()]);
 		
 		session.commit();
 		session.close();
 		
 		try {
			connection.assign("ready", "KeySet");
		} catch (RserveException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
 		
		return Keyword;
	}
	public String[] getScore (String sentence, int index){
		
		// data frame만들기 위한 normalize단계
		
		String[] isExt = null;
		 try {
			 
			 connection.assign("sentence", sentence);
			connection.assign("KeySet", Keyword);
			 
			 x = connection.eval("sentence <- \"" + sentence + "\"");
			 x = connection.eval("sentence <- gsub(\"[a-z]\",\"\",sentence)");
			 x = connection.eval("sentence <- gsub(\"[A-z]\",\"\",sentence)");
			 x = connection.eval("sentence <- gsub(\"[[:punct:]]\",\"\",sentence)");
	         x = connection.eval("sentence <- gsub(\"[[:cntrl:]]\",\"\",sentence)");
	         x = connection.eval("sentence <- gsub(\"\\\\d+\",\"\",sentence)");
	         x = connection.eval("sentence <- unlist(strsplit(sentence,\" \"))");
	         
	         x = connection.eval("sentences <- Filter(function(x){ nchar(x) >= 2} , sentence)");
	         //x = connection.eval("sentences <- table(sentences)");
	         x = connection.eval("sentences <- table(sentences[which(sentences != \"\")])");
	         //table(Keyword[which(Keyword != \"\")])
	         x = connection.eval("sentences <- sort(unlist(sentences),decreasing=T)");
	         x = connection.eval("sentences <- names(sentences)");
		
		     String[] str;
		        		
				try {
					str = connection.eval("sentences").asStrings();

				        		
				    // 입력된 문장에 Keyword가 있으면 0이상의 숫자, 없으면 0
				   	x = connection.eval("score <- match(KeySet, sentences ,nomatch=0)");
				   	x = connection.eval("score");
				 
				   	// keyword가 나타났는지 안나타났는지 저장되어 있는 int형 배열을 String형 배열로 변환
				   	int[] LScore = connection.eval("score").asIntegers();
				   	isExt = new String[LScore.length];
				   				
				   	int idx = LScore.length;
				   	
				   	
				   	for(int i = 0; i < LScore.length; i++){
				   				
				   		if(LScore[i] > 0 && i < idx-1 ) { // 만약 키워드가 나타났고, (긍정 Keyword라면 / 부정 Keyword라면)
				   			isExt[i] = String.valueOf(1);
				   		}
				   		else if( i == idx-1){
				   			double score = ((today[index] * 0.3) + (tomorrow[index] * 0.7) + 1) / 2;
				   			isExt[i] = String.valueOf(score);
				   		}
				   		else{ // 
				   			isExt[i] =  String.valueOf(0);
				   		}
				   		//	System.out.print(isExt[i] + " ");
				   	}
				   				
				   	//		System.out.println(" ");
					} catch (REXPMismatchException e) {
								// TODO Auto-generated catch block
								//e.printStackTrace();
				}
			} catch (RserveException e) {
	   			// TODO Auto-generated catch block
	   			e.printStackTrace();
	   		} catch (REngineException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	   		 return isExt;
	         
	}
public String[] getScore2 (String sentence, int index){
		
		// data frame만들기 위한 normalize단계
		ela_K[ela_K.length-1] = "result";
		
		String[] isExt = null;
		 try {
			 
			 connection.assign("sentence", sentence);
			connection.assign("KeySet", ela_K);
			 
			 x = connection.eval("sentence <- \"" + sentence + "\"");
			 x = connection.eval("sentence <- gsub(\"[a-z]\",\"\",sentence)");
			 x = connection.eval("sentence <- gsub(\"[A-z]\",\"\",sentence)");
			 x = connection.eval("sentence <- gsub(\"[[:punct:]]\",\"\",sentence)");
	         x = connection.eval("sentence <- gsub(\"[[:cntrl:]]\",\"\",sentence)");
	         x = connection.eval("sentence <- gsub(\"\\\\d+\",\"\",sentence)");
	         x = connection.eval("sentence <- unlist(strsplit(sentence,\" \"))");
	         
	         x = connection.eval("sentences <- Filter(function(x){ nchar(x) >= 2} , sentence)");
	         //x = connection.eval("sentences <- table(sentences)");
	         x = connection.eval("sentences <- table(sentences[which(sentences != \"\")])");
	         //table(Keyword[which(Keyword != \"\")])
	         x = connection.eval("sentences <- sort(unlist(sentences),decreasing=T)");
	         x = connection.eval("sentences <- names(sentences)");
		        		
				try {
		
				    // 입력된 문장에 Keyword가 있으면 0이상의 숫자, 없으면 0
				   	x = connection.eval("score <- match(KeySet, sentences ,nomatch=0)");
				   	x = connection.eval("score");
				 
				   	// keyword가 나타났는지 안나타났는지 저장되어 있는 int형 배열을 String형 배열로 변환
				   	int[] LScore = connection.eval("score").asIntegers();
				   	isExt = new String[LScore.length];
				   				
				   	int idx = LScore.length;
				   	
				   	
				   	for(int i = 0; i < LScore.length; i++){
				   				
				   		if(LScore[i] > 0 && i < idx-1 ) { // 만약 키워드가 나타났고, (긍정 Keyword라면 / 부정 Keyword라면)
				   			isExt[i] = String.valueOf(1);
				   		}
				   		else if( i == idx-1){
				   			double score = ((today[index] * 0.3) + (tomorrow[index] * 0.7) + 1) / 2;
				   			isExt[i] = String.valueOf(score);
				   		}
				   		else{
				   			isExt[i] =  String.valueOf(0);
				   		}
				   			//System.out.print(isExt[i] + " ");
				   	}
				   				
				   	//		System.out.println(" ");
					} catch (REXPMismatchException e) {
								// TODO Auto-generated catch block
								//e.printStackTrace();
				}
			} catch (RserveException e) {
	   			// TODO Auto-generated catch block
	   			e.printStackTrace();
	   		} catch (REngineException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	   		 return isExt;
	         
	}
	public void readyData(String[] str){
		
		if(str != null){
			rowN++;
			try {
				connection.assign("tmpReady", str);
				x = connection.eval("ready <- append(ready, tmpReady, after=length(ready))");
			} catch (REngineException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void createFrm(){
		int size = ela_K.length;
		try {
			// Keyword 개수만큼 열을 가진 
			x = connection.eval("ready <- ready[which(ready != \"KeySet\")]");
			//x = connection.eval("tmp <- matrix(unlist(ready),byrow=T,ncol=" + size + ")");
			x = connection.eval("tmp <- data.frame(matrix(ready, ncol = "+ size +", byrow = TRUE))");
			x = connection.eval("colnames(tmp) <- KeySet");
			x = connection.eval("library(\"data.table\")");
			x = connection.eval("tp <- data.frame(tmp)");
			x = connection.eval("n <- KeySet");
			x = connection.eval("unlist(n)");
			String[] n;
			try {
				n = x.asStrings();
			} catch (REXPMismatchException e) {
				// TODO Auto-generated catch block
			//	System.out.println(">>>>error");
				e.printStackTrace();
			}
			
			x = connection.eval("data1 <- data.frame(tp[1:" + rowN + ",1:" + size + " ])");
			x = connection.eval("colnames(data1) <- n");
			x = connection.eval("data <- data.table(data1,keep.rownames = FALSE)");
			x = connection.eval("capture.output(data, file = \"/home/forecast/data.txt\", append = FALSE)");

		} catch (RserveException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	 /*
     * Training Set 
     */
    public void setTraining(){
    	int size = origin.length;
          System.out.println("Size>>" + size);
    		 try {
				x = connection.eval("unlist(data)");
				x = connection.eval("svm_data <- data");
	 	        x = connection.eval("svm_data_train <- svm_data[1:"+ size +",]");
	 	        
	 	       x = connection.eval("library(e1071)");
				x = connection.eval("Sclassifier <- svm(svm_data_train, svm_data_train$result, scale = FALSE, kernel=\"sigmoid\")");
			} catch (RserveException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    }
	
	/* 
	 * SVM R로 수행하는 함수
	 *
	 * 기존의 뉴스데이터를 _train에 넣고 학습시킨 후
	 * 새로운 뉴스를 _test에 넣고 예측을 하면,
	 * 새로운 뉴스에 대한 주가 상승/하락을 결과로 나타냄
	 */
	public String[] svm(){
		int size = origin.length;
		String[] rst = null;
		try{
			x = connection.eval("unlist(data)");
			x = connection.eval("svm_data <- data");
			x = connection.eval("svm_data_test <- svm_data[1:"+size +",]");
			
			x = connection.eval("library(e1071)");
			x = connection.eval("Sresult<-unlist(predict(Sclassifier,svm_data_test))");
			rst = connection.eval("Sresult").asStrings();
		} catch (RserveException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (REXPMismatchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return rst;
	}
	
	public void elasticNet(){
	      String resource = "DataBase/mybatis-config.xml";
	         
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
	      ArrayList<ElasticElement> elasticList = null;
	      
	      try {
	    	  x = connection.eval("result <- \"'\"");
	         x = connection.eval("library(elasticnet)");
	         x = connection.eval("y <- subset(data, select=c(result))");
	         x = connection.eval("capture.output(y, file = \"/home/forecast/y.txt\", append = FALSE)");
	         x = connection.eval("x <- tmp[,1:100]");
	         

	         x = connection.eval("capture.output(x, file = \"/home/forecast/x.txt\", append = FALSE)");
	         x = connection.eval("y <- as.numeric(unlist(y))");
	         x = connection.eval("x <- do.call(cbind, x)");
	         
	         // calculating the keyword's coefficients
	         x = connection.eval("object <- enet(x, y, lambda=0, max.steps=50, trace=FALSE, normalize=TRUE, intercept=FALSE, eps=FALSE)");
	         
	         // runnig predict 
	         x = connection.eval("coef.45 <- predict(object, s=0.45, type=\"coef\", mode=\"fraction\")");
	         
	         // print out to result file for each attribute
	         x = connection.eval("capture.output(coef.45, file = \"/home/forecast/Elastic_result_coef.45.txt\", append = FALSE)");
	      } catch (RserveException e) {
	         // TODO Auto-generated catch block
	         e.printStackTrace();
	      }
	      
	      // data handling and saving to elasticList for DB
	      try {
	         // prepare to read the elasticNet's result file
	         FileInputStream fis = new FileInputStream(new File("/home/forecast/Elastic_result_coef.45.txt"));
	           InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
	           BufferedReader br = new BufferedReader(isr);

	         // elasticList is the DBList
	         elasticList = new ArrayList<ElasticElement>();

	         // line : keyword line AND nextLine : value line
	         String line = "";
	         String nextLine = "";
	         boolean flag = false;   // Check if the keyword set line start
	         
	         // read file
	         while((line = br.readLine()) != null) {
	            if(line.contains("coeffi")) {
	               flag = true;
	               continue;
	            }
	            
	            if(flag == true && (nextLine = br.readLine()) != null) {
	               line = line.replaceAll("( )+", " ");
	               nextLine = nextLine.replaceAll("( )+", " ");
	               
	               String[] tmp = line.split(" ");
	               String[] coeffi = nextLine.split(" ");
	               
	               // i is for keyword line index AND j is for value line index
	               for(int i = 0, j = 0; j < coeffi.length ; i++, j++) {
	                  if(tmp[i].equals(""))
	                     i++;
	                  if(coeffi[j].equals(""))
	                     j++;
	                  
	                  // add to List
	                  elasticList.add(new ElasticElement(tmp[i], coeffi[j]));
	               }
	            }
	         }
	         br.close();
	         
	      } catch (Exception e1) {
	         // TODO Auto-generated catch block
	         e1.printStackTrace();
	      }
	      
	      String value="";
	      
	      for(int i=0; i<elasticList.size(); i++){
	         value += "(\'" + elasticList.get(i).getKeyword() + "\',\'" + elasticList.get(i).getValue() + "\'),";
	      }
	      value = value.substring(0, value.length()-1);
	      
	      System.out.println("value : " + value);
	      input.put("VAL", value);
	      session.delete("Mapper.elaClear");
	      
	      int result = session.insert("Mapper.elaKeyword", input);
	         
	      if (result > 0) {
	         session.commit();
	         session.close();
	      }
	      else {
	         session.rollback();
	         System.out.println("추가실패");
	      }
	      
	      
	}
	
	/////////////////////// prd _ela 쓸거면 +1 없애고 prd_ela에서 size 값에서 -1 한거 없애 주어야 함 //////////////////
	public void set_elaKeyword(List<ElasticElement> ela_keyword){
		ela_K = new String[ela_keyword.size()+1]; 
		ela_Kvalue = new double[ela_keyword.size()];
	      for(int i = 0; i < ela_keyword.size(); i++){
	    	  ela_K[i] = ela_keyword.get(i).getKeyword();
	    	  ela_Kvalue[i] = Double.parseDouble(ela_keyword.get(i).getValue());
	      }
	      
	}
	
	public double prd_ela(String newsApublic, int index) throws REXPMismatchException{

		//double[] total_result = new double[origin.length];
		double ela_result = 0.0;
		double[] Elavalue;

	      int i = 0;
		                    
	       try {
	            connection.assign("nAp" , newsApublic);
	            connection.assign("ela",ela_K);

				 x = connection.eval("nAp <- gsub(\"[a-z]\",\"\",nAp)");
				 x = connection.eval("nAp <- gsub(\"[A-z]\",\"\",nAp)");
				 x = connection.eval("nAp <- gsub(\"[[:punct:]]\",\"\",nAp)");
		         x = connection.eval("nAp <- gsub(\"[[:cntrl:]]\",\"\",nAp)");
		         x = connection.eval("nAp <- gsub(\"\\\\d+\",\"\",nAp)");
		         x = connection.eval("nAp <- unlist(strsplit(nAp,\" \"))");	         
		         x = connection.eval("nAps <- Filter(function(x){ nchar(x) >= 2} , nAp)");
		         x = connection.eval("nAps <- table(nAp[which(nAps != \"\")])");
		         x = connection.eval("nAps <- sort(unlist(nAps),decreasing=T)");
		         x = connection.eval("nAps <- names(nAps)");
			
			     String[] str;
			     int size = ela_K.length-1;
					try {
						str = connection.eval("nAp").asStrings();

	                // 입력된 문장에 Keyword가 있으면 0이상의 숫자, 없으면 0
	                  x = connection.eval("score <- match(ela, nAps ,nomatch=0)");
	                  x = connection.eval("score");
	             
	                  // keyword가 나타났는지 안나타났는지 저장되어 있는 int형 배열을 String형 배열로 변환
	                  int[] LScore = connection.eval("score").asIntegers();
	                  Elavalue = new double[size];
	                           
	                  for(int idx = 0;idx < size; idx++){
	                	  	 Elavalue[idx] = 0.0;
	                  }
	                  int idx = LScore.length;
	               // 엘라스틱넷 키워드가 출현했는지 안했는지 나타내는 줄
	            	  for(i = 0; i < idx; i++){
	           
	            		  if(LScore[i] > 0 && i < idx-1 ) { // elastic net 결과로 얻은 keyword가 존재하는 경우,
	                  
	                        Elavalue[i] += ela_Kvalue[i];
	                        ela_result += Elavalue[i];
	                     }
	                     else{ // 존재하지 않는 경우
	                        // nothing to do
	                     }
	                  }
	          	  double origin_result =  ((today[index] * 0.3) + (tomorrow[index] * 0.7) + 1) / 2;
	                  if(origin_result > 0.5 && ela_result > 0.5 ) { // 만약 결과값이 일치하였다면
	                       what = "PP";
	                  }
	                  else if(origin_result > 0.5 && ela_result < 0.5){
	                		what = "PN";
	                	  }else if(origin_result < 0.5 && ela_result > 0.5){
	                		 what = "NP";
	                	  }else if(origin_result < 0.5 && ela_result < 0.5){
	                		  what = "NN";
	                	  }
	                	  else{ // 0.5낀경우
	                			 if(origin_result > 0.5 && ela_result == 0.5)  what = "PC";
	                    		 else if(origin_result == 0.5 && ela_result< 0.5)  what = "CN";
	                    		 else if( origin_result == 0.5 && ela_result > 0.5) what = "CP";
	                    		 else if(origin_result < 0.5 && ela_result == 0.5) what = "NC";
	                    		 else{
	                    			 //ha.add(match[i]); hi.add(rst[i]);
	                    		 }
	                	  }   
	                  
	               
					}catch (REXPMismatchException e) {
						// TODO Auto-generated catch block
						//e.printStackTrace();
					}
	                       
	               } catch (RserveException e) {
	               // TODO Auto-generated catch block
	               e.printStackTrace();
	            } catch (REngineException e1) {
	            // TODO Auto-generated catch block
	            e1.printStackTrace();
	         }
	       return ela_result;
	}
	public int[] getToday() {
		return today;
	}
	public void setToday(int[] today) {
		this.today = today;
	}
	public int[] getTomorrow() {
		return tomorrow;
	}
	public void setTomorrow(int[] tomorrow) {
		this.tomorrow = tomorrow;
	}
}