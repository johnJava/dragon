package com.common;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.htmlparser.Parser;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

public class GenericUtil {
	public static SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static final String MUSIC_FILE = new GenericUtil().getClass().getResource("/").getPath() + "\\alarm.wav";
	private static final String endDate = "2015-03-31";
	private static final String DATEURL = "http://www.sina.com.cn/";
	//private static final String DATEURL = "http://open.baidu.com/special/time/";

	public static void main(String[] args) {
		System.out.println(isEndDate());
	}

	@SuppressWarnings("serial")
	public static List<String> creditlist = new ArrayList<String>() {

		{
			add("ico_xx_1");
			add("ico_xx_2");
			add("ico_xx_3");
			add("ico_xx_4");
			add("ico_xx_5");
		}
	};

	public static String cookieFormat(HashMap<String, String> cookieMap) {
		if (cookieMap.size() == 0)
			return "";
		StringBuffer buff = new StringBuffer(2000);
		for (Map.Entry<String, String> entry : cookieMap.entrySet()) {
			String key = entry.getKey().trim();
			LogUtil.debugPrintf("begin==" + key);
			if (key.equalsIgnoreCase("domain") || key.equalsIgnoreCase("HttpOnly") || key.equalsIgnoreCase("path")) {
				LogUtil.debugPrintf("ignore==" + key);
				continue;
			}
			buff.append(entry.toString() + ";");
		}
		buff.deleteCharAt(buff.lastIndexOf(";"));
		return buff.toString();
	}

	public static void playAlarm() throws LineUnavailableException, UnsupportedAudioFileException, IOException {
		// 获取音频输入流a
		AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(MUSIC_FILE));
		// 获取音频编码对象
		AudioFormat audioFormat = audioInputStream.getFormat();

		// 设置数据输入
		DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, audioFormat, AudioSystem.NOT_SPECIFIED);
		SourceDataLine sourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
		sourceDataLine.open(audioFormat);
		sourceDataLine.start();

		/*
		 * 从输入流中读取数据发送到混音器
		 */
		int count;
		byte tempBuffer[] = new byte[1024];
		while ((count = audioInputStream.read(tempBuffer, 0, tempBuffer.length)) != -1) {
			if (count > 0) {
				sourceDataLine.write(tempBuffer, 0, count);
			}
		}

		// 清空数据缓冲,并关闭输入
		sourceDataLine.drain();
		sourceDataLine.close();
	}

//	public static boolean isEndDate() {
//		boolean flag = false;
//		String dateStr=getDateStr(DATEURL);
//		int num;
//		if(!dateStr.equalsIgnoreCase("")){
//			LogUtil.singleDebugPrintf("dateStr:"+dateStr);
//			num =  compare_date(endDate, dateStr);
//		}else{
//			LogUtil.singleDebugPrintf("endDate");
//			num =  compare_date(endDate, new SimpleDateFormat("yyyy-MM-dd").format(System.currentTimeMillis()));
//		}
//		if (num == -1)
//			flag = true;
//		return flag;
//	}
	public static boolean isEndDate() {
		boolean flag = false;
	    int num;
	    	num =  compare_date(endDate, new SimpleDateFormat("yyyy-MM-dd").format(System.currentTimeMillis()));
		if (num == -1)
			flag = true;
		return flag;
	}

	public static int compare_date(String DATE1, String DATE2) {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		try {
			Date dt1 = df.parse(DATE1);
			Date dt2 = df.parse(DATE2);
			if (dt1.getTime() > dt2.getTime()) {
				return 1;
			} else if (dt1.getTime() < dt2.getTime()) {
				return -1;
			} else {
				return 0;
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return 0;
	}

	public static String getDateStr(String url) {
		String html="";
		try {
			Parser parser = getParser(DATEURL);
			long startTime = System.currentTimeMillis();
			LogUtil.debugPrintf("抽取[" + url + "]开始[" + GenericUtil.dateformat.format(startTime) + "]");
			NodeList worldTimeNow = parser.extractAllNodesThatMatch(new HasAttributeFilter("class", "t-guide"));
			html=worldTimeNow.toHtml();
			LogUtil.singleDebugPrintf("htmldate1:" + html);
//			html=html.substring(html.indexOf("<b id=\"worldTimeNow\">")+"<b id=\"worldTimeNow\">".length(),html.indexOf("</b>")-4);
//			html=html.replace("年", "-");
//			html=html.replace("月", "-");
//			LogUtil.singleDebugPrintf("htmldate:" + html);
			long endTime = System.currentTimeMillis();
			LogUtil.debugPrintf("抽取[" + url + "]结束[" + GenericUtil.dateformat.format(endTime) + "]耗时[" + (endTime - startTime) + "毫秒]");
		} catch (Exception e) {
			html="";
			e.printStackTrace();
		}
		return html;
	}

	public static Parser getParser(String url) throws ParserException, IOException {
		LogUtil.singleDebugPrintf("获取时间。。。。");
		long startTime = System.currentTimeMillis();
		LogUtil.debugPrintf("请求[" + url + "]开始[" + GenericUtil.dateformat.format(startTime) + "]");
		URL urlPage;
		HttpURLConnection conn = null;
		try {
			urlPage = new URL(url);
			conn = (HttpURLConnection) urlPage.openConnection();
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		try {
			conn.setRequestMethod("GET");
		} catch (ProtocolException e1) {
			e1.printStackTrace();
		}
		conn.setRequestProperty("User-Agent", "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Trident/5.0)");
		conn.setRequestProperty("If-Modified-Since", "0");
		Parser parser = new Parser(conn);
		parser.setEncoding("UTF-8");
		long endTime = System.currentTimeMillis();
		LogUtil.infoPrintf("请求结束[" + GenericUtil.dateformat.format(endTime) + "]耗时[" + (endTime - startTime) + "毫秒]");
		return parser;
	}
}
