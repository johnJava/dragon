package test;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
public class TestHttpsLogin {
	private static X509TrustManager myX509TrustManager = new X509TrustManager() { 

	    public X509Certificate[] getAcceptedIssuers() { 
	        return null; 
	    } 

	    public void checkServerTrusted(X509Certificate[] chain, String authType) 
	    throws CertificateException { 
	    } 

	    public void checkClientTrusted(X509Certificate[] chain, String authType) 
	    throws CertificateException { 
	    } 
	};
	
	public static void main(String[] args) {
		
	}
	public static  String SendHttpsPOST(String url, String param, String data) { 
	        String result = null; 
	        

	        //使用此工具可以将键值对编码成"Key=Value&amp;Key2=Value2&amp;Key3=Value3&rdquo;形式的请求参数 
	        String requestParam =param; 
	        try { 
	            //设置SSLContext 
	            SSLContext sslcontext = SSLContext.getInstance("TLS"); 
	            sslcontext.init(null, new TrustManager[]{myX509TrustManager}, null);

	            //打开连接
	        //要发送的POST请求url?Key=Value&amp;Key2=Value2&amp;Key3=Value3的形式 
	           //URL requestUrl = new URL(url + "?" + requestParam); 
	           URL requestUrl = new URL(url); 
	           HttpsURLConnection httpsConn = (HttpsURLConnection)requestUrl.openConnection();

	            //设置套接工厂 
	            httpsConn.setSSLSocketFactory(sslcontext.getSocketFactory());

	            //加入数据 
	            httpsConn.setRequestMethod("get"); 
	            httpsConn.setDoOutput(true); 
//	            DataOutputStream out = new DataOutputStream( 
//	                    httpsConn.getOutputStream()); 
//	            if (data != null) 
//	                out.writeBytes(data); 
//	            
//	            out.flush(); 
//	            out.close();

	            //获取输入流 
	            BufferedReader in = new BufferedReader(new InputStreamReader(httpsConn.getInputStream())); 
	            int code = httpsConn.getResponseCode(); 
	            if (HttpsURLConnection.HTTP_OK == code){ 
	                String temp = in.readLine(); 
	                /*连接成一个字符串*/ 
	                while (temp != null) { 
	                    if (result != null) 
	                        result += temp; 
	                    else 
	                        result = temp; 
	                    temp = in.readLine(); 
	                } 
	            } 
	        } catch (KeyManagementException e) { 
	            e.printStackTrace(); 
	        } catch (NoSuchAlgorithmException e) { 
	            e.printStackTrace(); 
	        } catch (MalformedURLException e) { 
	            e.printStackTrace(); 
	        } catch (ProtocolException e) { 
	            e.printStackTrace(); 
	        } catch (IOException e) { 
	            e.printStackTrace(); 
	        }

	        return result; 
	}
}
