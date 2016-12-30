package cdef.dictionary;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.io.OutputStreamWriter;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.ko.KoreanAnalyzer;
import org.apache.lucene.analysis.ko.morph.AnalysisOutput;
import org.apache.lucene.analysis.ko.morph.MorphAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.rosuda.JRI.REXP;
import org.rosuda.JRI.Rengine;


public class KoAnalyzer {

	// R연동 및 데이터 가공 코드 
	public void  ROpen(String result){

		System.out.println("Creating Rengine (with arguments)");
		 
         Rengine re = new Rengine(null, false, null);

         System.out.println("Rengine created, waiting for R");

         if (!re.waitForR()) {
              System.out.println("Cannot load R");
              return;
         }
     	
        re.assign("Keyword", result);
        
         
        REXP x;
        x = re.eval("Keyword <- gsub(\"[a-z]\",\"\",Keyword)");
        x = re.eval("Keyword <- gsub(\"[A-Z]\",\"\",Keyword)");
        x = re.eval("Keyword <- gsub(\"[[:punct:]]\",\"\",Keyword)");
        x = re.eval("Keyword <- gsub(\"[[:cntrl:]]\",\"\",Keyword)");
        x = re.eval("Keyword <- gsub(\"\\\\d+\",\"\",Keyword)");
        x = re.eval("Keyword <- gsub(\"\",\"\",Keyword)");
        x = re.eval("Keyword <- strsplit(Keyword,\" \")");
        x = re.eval("KeySet <- table(Keyword)");
        x = re.eval("KeySet <- head(sort(unlist(KeySet),decreasing=T),102)");
        x = re.eval("KeySet <- names(KeySet)");
     
        x = re.eval("KeySet");
    
        String rs[] = x.asStringArray();
       
        
        try {
        	 BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("KeywordSet.txt"),"UTF8"));
        	 
            for(int i = 1; i < rs.length; i++){
         //       System.out.println(rs[i]);
	            out.write(rs[i]); out.newLine();
            }
            out.close();
          } catch (IOException e) {
              System.err.println(e); // 에러가 있다면 메시지 출력
              System.exit(1);
          }

         re.end();
     //    System.out.println("Bye.");
	}
	
	// 형태소 분석
	public static String analyze(String szText) {
        
        TokenStream ts = null ;

        String output = "" ;
        
        try {
           KoreanAnalyzer analyzer = new KoreanAnalyzer() ;
           MorphAnalyzer morph = new MorphAnalyzer() ;
           ts = analyzer.tokenStream(null,  new StringReader(szText)) ;
           CharTermAttribute termAtt = ts.addAttribute(CharTermAttribute.class) ;

           ts.reset();
           int jump =1;
           while(ts.incrementToken()) {
              String token = termAtt.toString();
              List<AnalysisOutput> results = morph.analyze(token) ;
              //System.out.println("Term : " + termAtt.toString());

              for(AnalysisOutput o : results){
              
                    String str = o.getStem();
                    boolean isNum = false;
                    
                    // 복합명사인 경우, 복합 명사 원본은 출력되지 않게 건너뛴다.
                    if(o.getCNounList().size() > 0){
                 
                       if(jump == 1){
                          jump = 0;
                          continue;
                       }
                       
                    }else jump = 1;
                 
  
                    if(o.getPos() == 'N') {
              
                       //System.out.println(o.getSource() + " >>" +o.getStem());
                       // 숫자 없애기
                       String clean1 = str.replaceAll("[^0-9]", "");
                 
                       if(clean1.length() > 0){
                          isNum = true;
                          continue;
                       }
                       else{ // 숫자가 존재하지 않고, 단어가 오직 명사로만 이루어져 있다면,
                             if(o.getStem().equals(o.getSource())){
              
                              output += o.getStem() + " "; // 결과값에 포함         
                             }
                       }    
                    }
              }
        
           }
  
           ts.end() ;         
        }
        catch(Exception e) {
           System.out.println(szText + ">> Exception : " + e.getMessage()) ;
        }
        finally {
           if( ts != null ) try { ts.close() ; } catch(Exception ee){}
        }
        
        return output;
	}
}
