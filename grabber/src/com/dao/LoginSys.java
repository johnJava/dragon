package com.dao;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.htmlparser.Parser;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.tags.ScriptTag;
import org.htmlparser.util.NodeList;
import org.json.JSONObject;

import com.common.GenericUtil;
import com.common.LogUtil;
import com.common.MyX509TrustManager;
import com.web.EquMessageService;

public class LoginSys {
	private JSONObject loginInfo;
	private HashMap<String, String> cookies;
//	private final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; rv:34.0) Gecko/20100101 Firefox/34.0";
	private final String USER_AGENT = "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Trident/5.0)";
	private final String loginURL = "https://passport.5173.com/";
	private boolean state = false;

	public LoginSys(JSONObject loginInfo) {
		this.loginInfo = loginInfo;
		this.cookies = new HashMap<String, String>();
	}

	public boolean doLogin() throws Exception {
		LogUtil.webPrintf("开始登录...");
		boolean result = true;
		String postParams = getPostParams();
		HttpsURLConnection loginConn = getHttpSConn("POST");
		if (null != this.cookies) {
			//for (String cookie : this.cookies) {
				loginConn.addRequestProperty("Cookie", GenericUtil.cookieFormat(this.cookies));
			//}
		}
		DataOutputStream wr = new DataOutputStream(loginConn.getOutputStream());
		wr.writeBytes(postParams);
		wr.flush();
		wr.close();
		int responseCode = loginConn.getResponseCode();
		LogUtil.debugPrintf("\nSending 'POST' request to URL : " + this.loginURL);
		LogUtil.debugPrintf("Post parameters : " + postParams);
		LogUtil.debugPrintf("Response Code : " + responseCode);
		BufferedReader in = new BufferedReader(new InputStreamReader(loginConn.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
		Map<String, List<String>> header = loginConn.getHeaderFields();
		LogUtil.debugPrintf("conn.getHeaderFields():" + header);
		List<String> cookie = header.get("Set-Cookie");
		String loginInfo;
		if (cookie == null || cookie.size() == 0) {
			result = false;
			LogUtil.webPrintf("登录失败!");
		} else {
			LogUtil.webPrintf("登录成功!");
			LogUtil.debugPrintf("cookie====" + cookie);
			setCookies(cookie);
			putCookies("NTKF_PAGE_MANAGE", "%7B%22m%22%3A%5B%7B%2234233%22%3A874270%7D%2C%7B%2270512%22%3A874264%7D%5D%2C%22t%22%3A%2220%3A03%3A47%22%7D");
		}
		this.state = result;
		return result;
	}

	private String getPostParams() throws Exception {
		HttpsURLConnection tokenConn = getHttpSInitConn();
		Parser parser = new Parser(tokenConn);
		parser.setEncoding("UTF-8");
		NodeList list = parser.parse(new HasAttributeFilter("type", "text/javascript"));
		ScriptTag jstag = null;
		String pwd = "";
		String token = "";
		for (int i = 0; i < list.size(); i++) {
			jstag = (ScriptTag) list.elementAt(i);
			if (jstag == null)
				continue;
			String scrstr = jstag.toPlainTextString();
			if (scrstr.contains("$.fost.LoginParams")) {
				int beginindex = scrstr.indexOf("{");
				int endindex = scrstr.indexOf("};");
				scrstr = scrstr.substring(beginindex, endindex + 1);
				LogUtil.debugPrintf("jstag==" + scrstr);
				JSONObject jsonobj = new JSONObject(scrstr);
				token = (String) jsonobj.get("SecurityToken");
				LogUtil.debugPrintf("SecurityToken==" + jsonobj.get("SecurityToken"));
				pwd = (String) jsonobj.get("PasswordKey");
				LogUtil.debugPrintf("PasswordKey==" + jsonobj.get("PasswordKey"));
			}
		}
		String postParams = "";
		if (!pwd.equals("") && !token.equals("")) {
			pwd = hex_md5(pwd);
			postParams = "&password=" + pwd + "&passpod=&captcha=&category=&" + "__validationToken__=" + token;
		}
		postParams = "userName=" + getLoginInfo().getString("username") + postParams + "&__validationDna__=";
		Map<String, List<String>> header = tokenConn.getHeaderFields();
		LogUtil.debugPrintf("conn.getHeaderFields():" + header);
		List<String> cookie = header.get("Set-Cookie");
		if(null!=cookie){
			LogUtil.debugPrintf("cookie====" + cookie);
			setCookies(cookie);
		}
		return postParams;
	}

	private HttpsURLConnection getHttpSInitConn() throws Exception {
		HttpsURLConnection conn;
		// 创建SSLContext对象，并使用我们指定的信任管理器初始化
		TrustManager[] tm = { new MyX509TrustManager() };
		SSLContext sslContext = SSLContext.getInstance("SSL", "SunJSSE");
		sslContext.init(null, tm, new java.security.SecureRandom());
		// 从上述SSLContext对象中得到SSLSocketFactory对象
		SSLSocketFactory ssf = sslContext.getSocketFactory();
		URL obj = new URL(this.loginURL);
		conn = (HttpsURLConnection) obj.openConnection();
		conn.setSSLSocketFactory(ssf);
		// default is GET
		conn.setRequestMethod("GET");
		// act like a browser
		conn.setRequestProperty("User-Agent", USER_AGENT);
		conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		// conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
		conn.setRequestProperty("Accept-Language", "zh-cn,zh;q=0.8,en-us;q=0.5,en;q=0.3");
		conn.setRequestProperty("Accept-Encoding", "gzip, deflate");
		conn.setRequestProperty("Connection", "keep-alive");
		conn.setRequestProperty("Host", "passport.5173.com");
		return conn;
	}

	private HttpsURLConnection getHttpSConn(String method) throws Exception {
		// 创建SSLContext对象，并使用我们指定的信任管理器初始化
		TrustManager[] tm = { new MyX509TrustManager() };
		SSLContext sslContext = SSLContext.getInstance("SSL", "SunJSSE");
		sslContext.init(null, tm, new java.security.SecureRandom());
		// 从上述SSLContext对象中得到SSLSocketFactory对象
		SSLSocketFactory ssf = sslContext.getSocketFactory();
		// Acts like a browser
		URL obj = new URL(this.loginURL);
		HttpsURLConnection conn;
		conn = (HttpsURLConnection) obj.openConnection();
		conn.setSSLSocketFactory(ssf);
		conn.setRequestMethod(method);
		conn.setRequestProperty("Host", "passport.5173.com");
		conn.setRequestProperty("User-Agent", USER_AGENT);
		conn.setRequestProperty("Accept", "*/*");
		conn.setRequestProperty("Accept-Language", "zh-cn,zh;q=0.8,en-us;q=0.5,en;q=0.3");
		conn.setRequestProperty("Accept-Encoding", "gzip, deflate");
		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
		conn.setRequestProperty("X-Requested-With", "XMLHttpRequest");
		conn.setRequestProperty("Referer", "https://passport.5173.com/");
		conn.setRequestProperty("Connection", "keep-alive");
		conn.setRequestProperty("Pragma", "no-cache");
		conn.setRequestProperty("Cache-Control", "no-cache");
		conn.setDoOutput(true);
		conn.setDoInput(true);
		return conn;
	}

	private String hex_md5(String pwdsrc) throws Exception {
		ScriptEngineManager manager = new ScriptEngineManager();
		ScriptEngine engine = manager.getEngineByName("javascript");
		String jsFileName =this.getClass().getResource("/").getPath()+"\\md5.js"; // 读取js文件
		FileReader reader = new FileReader(jsFileName); // 执行指定脚本
		engine.eval(reader);
		String PasswordKey = pwdsrc;
		String hexpwd = "";
		if (engine instanceof Invocable) {
			Invocable invoke = (Invocable) engine; // 调用merge方法，并传入两个参数
			String c = (String) invoke.invokeFunction("hex_md5", getLoginInfo().getString("password"));
			hexpwd = (String) invoke.invokeFunction("hex_md5", c.substring(8, 24) + PasswordKey);
			LogUtil.debugPrintf("hexpwd = " + hexpwd);
		}
		reader.close();
		return hexpwd;
	}

	private JSONObject getLoginInfo() {
		return loginInfo;
	}

	public void setLoginInfo(JSONObject loginInfo) {
		this.loginInfo = loginInfo;
	}

	public HashMap<String, String> getCookies() {
		return cookies;
	}
	private void putCookies(String key,String value) {
		this.cookies.put(key, value);
	}
	
	private void setCookies(List<String> cookies) {
		for (String cookie : cookies) {
			String[] params = cookie.split(";");
			for (int i = 0; i < params.length; i++) {
				String param = params[i];
				if (param.contains("=")) {
					String[] kv = param.split("=");
					this.cookies.put(kv[0], kv[1]);
				} else {
					this.cookies.put(param, "");
				}
				LogUtil.debugPrintf("add cookie==" + param);
			}
		}
		LogUtil.debugPrintf("this.cookies==" + this.cookies.toString());
	}

}
