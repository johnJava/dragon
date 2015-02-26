package Q23;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * @author Yi Hong
 * 
 *  CO3090/CO7090 coursework 1  
 *  
 *  NOTE:
 *  
 *  It is NOT necessary to understand the implementation detail in this class
 *  However, you are free to modify this class if you wish. 
 * 
 *  class Helper provides functions to get the html code, extract 
 *  URLs from a given web page and count the number of occurrences of a specific word.
 *  
 *  This class has 3 static methods:
 *  
 *  	 int countNumberOfOccurrences(String word, String text)
 * 		 String getContentFromURL(String URLstring)
 * 		 ArrayList<String> getHyperlinksFromContent(String URLstring, String content)
 * 
 *  
 *    
 */


public class Helper {
	
	/* Regular expressions  */
	public static final Pattern link_pattern=Pattern.compile("(?i)<a([^>]+)>(.+?)</a>");
	public static final Pattern href_pattern=Pattern.compile("\\s*(?i)href\\s*=\\s*(\"([^\"]*\")|'[^']*'|([^'\">\\s]+))");
	
	
	
	
	/**
	 * Method countNumberOfOccurrences returns how many time 
	 * a given word appears in a text (case-insensitive)
	 * 
	 * @param word: a keyword 
	 * @param text  
	 * @return integer
	 * 
	 * Example:
	 * 
	 *   countNumberOfOccurrences("i","I love leicester")  
	 *   should return: 1
	 *      
	 *   countNumberOfOccurrences("Wish","I wish to wish the wish you wish to wish, but if you wish the wish the witch wishes, I won't wish the wish you wish to wish")
     *   should return: 11
	 * 
	 */
	public static int countNumberOfOccurrences(String word, String text){
		
		Matcher m = Pattern.compile("\\b"+word+"\\b", Pattern.CASE_INSENSITIVE).matcher(text);
		int matches = 0;
			while(m.find()){
				matches++;
		}	    
		return matches;	
	}
	
	
	/**
	 * 
	 * getContentFromURL method returns the HTML source code of a given web page
	 * 
	 * @param  URLstring:  URL (address) of a specific website or file on the Internet e.g. http://www2.le.ac.uk/departments/computer-science             
	 * @return HTML source code of the given URL
	 *          
	 */
	public static String getContentFromURL(String URLstring){
		
		 URL url;
		    InputStream is = null;
		    BufferedReader br;
		    String line;

		    try {
		        url = new URL(URLstring);
		        is = url.openStream();  
		        br = new BufferedReader(new InputStreamReader(is));

		        StringBuffer sb=new StringBuffer(); 
		        
		        while ((line = br.readLine()) != null) {
		        	sb.append(line);
		        	sb.append("\n");
		        }
		        
		        String content=sb.toString();
		        return content;
		        
		    }  catch (MalformedURLException mue) {
		    	 System.out.println(mue.toString());
		    	// mue.printStackTrace();
		    } catch (IOException ioe) {
		    	// System.out.println(ioe.toString());
		    	 ioe.printStackTrace();
		    } finally {
		        try {
		            if (is != null) is.close();
		        } catch (IOException ioe) {
		          //  ioe.printStackTrace();
		            System.out.println(ioe.toString());
		        }
		    }
		    return "";
		
	}
	
	/**
	 *  
	 * getHyperlinksFromContent() method extracts all internal 
	 * hyperlinks from the source code of a html document.
	 * suppose http://example.com/some.html has the following HTML source:  
	 * 
	 *  <HTML>
	 *      <b>A sample page with 4 links</b>
	 *      <a href="1.html">link1</a>
	 *      <a href="http://example.com/other.html">
	 *      <a href="http://www.google.com">
	 *      <a href="http://example.com/other.html#bookmark">
	 *  </HTML>
	 *  
	 *  then getHyperlinksFromContent("http://example.com/some.html", html_source) 
	 *  should return 
	 *  
	 *     http://example.com/1.html 
	 *     http://example.com/other.html 
	 *     
	 *     Note: (1) http://www.google.com 
	 *               is not included in the result as it has a different domain
	 *           (2) http://example.com/other.html#bookmark 
	 *               is not included because it is a bookmark
	 *  
	 * @param URLstring: URL (address) of a specific web site or file
	 *         e.g. http://www2.le.ac.uk/departments/computer-science             
	 * @param content: HTML source code 
	 * @return A list of internal hyperlinks on this page (same domain as URLstring)
	 */
	
	
	public static ArrayList<String> getHyperlinksFromContent(String URLstring, String content){
		
		ArrayList<String> ret=new ArrayList<String>();
		
		try{
			URL  url = new URL(URLstring);
			Matcher matcher = link_pattern.matcher(content);
			
        while (matcher.find()) {
        	
        	 String links = matcher.group(1);  
        	 Matcher hrefs = href_pattern.matcher(links);
        	 
             while(hrefs.find()){
            	String link = hrefs.group(1).replaceAll("'", "").replaceAll("\"", "");
            	String absolutePath="";
            	String path=url.getPath();
        	    if ((path == null) || path.equals("") || path.equals("/")){
        	    	absolutePath="";
        	    }
        	    int lastSlashPos = path.lastIndexOf('/');
        	    if (lastSlashPos >= 0){
        	    	absolutePath=path.substring(0, lastSlashPos); //strip off the slash
        	    }else{
        	    	absolutePath= ""; 
        	    }

            	String address="";     	
            	URI uri=new URI(link);  	
            	if(!uri.isAbsolute()){	            	
            		address=url.getProtocol()+"://"+url.getHost()+absolutePath+"/"+link;
            		
            		if(!address.contains("#")){
            			ret.add(address);
            		}
            	
            	}else{
            		if(link.contains(url.getHost())){
            		if(!link.contains("mailto:")){
            			address=link;
            			if(!address.contains("#")){
            				ret.add(address);
            			}
            			
            		}
            		}
            	}
            } 
        
		}
            
		}catch(URISyntaxException e){
			//System.out.println("Ignore mailto"+ e.toString());
		}
		catch(Exception ex){
			System.out.println(ex.toString());
		}
	
        return ret;
		
		} 
	
	

	
	
	public static void main(String args[]){
		
		
		//Usage
		
		String websiteURL="http://www.bbc.co.uk/news/"; 
		String keyword="FTSE";
		
		String content=Helper.getContentFromURL(websiteURL);
		ArrayList<String> urls=Helper.getHyperlinksFromContent(websiteURL, content); 
		
		System.out.println("The word '"+keyword+"' appears " + Helper.countNumberOfOccurrences(keyword,content)+" time(s) on this page");
		System.out.println();
		System.out.println("Hyperlinks on this page:"); 
		System.out.println();
		
		 for(String link:urls){
			 System.out.println(link );
		 }
		 
		 
		 //System.out.println(Helper.countNumberOfOccurrences("i","I love leicester"));		 
		 //System.out.println(Helper.countNumberOfOccurrences("Wish","I wish to wish the wish you wish to wish, but if you wish the wish the witch wishes, I won't wish the wish you wish to wish"));
		 
		 
	}
	
		
	}
		
