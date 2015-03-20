package com.web;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URLEncoder;
import java.nio.CharBuffer;
import java.text.SimpleDateFormat;

import org.apache.catalina.websocket.StreamInbound;
import org.apache.catalina.websocket.WsOutbound;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import com.common.GenericUtil;
import com.common.LogUtil;
import com.dao.EquSnatcher;
import com.dao.LoginSys;
import com.dao.ShopThread;
import com.vo.EquItem;

@SuppressWarnings({ "unused", "deprecation" })
public class EquMessageInbound extends StreamInbound {
	private LoginSys login = null;
	private JSONObject common_params = null;
	private EquItem equitem = null;
	// private String sessionId;
	// private String PREFIX = "ws_";
	private EquSnatcher snatcher = null;

	private String sessionId = "";

	public EquMessageInbound(int id, String _sessionId) {
		// this.sessionId = _sessionId;
		// System.out.println(sessionId);
		// setPREFIX("ws_" + id);
		if (!sessionId.equals("") && !_sessionId.equals(sessionId)) {
			this.doStop();
		}
		this.setSessionId(_sessionId);
	}

	@Override
	protected void onBinaryData(InputStream arg0) throws IOException {

	}

	@Override
	protected void onTextData(Reader reader) throws IOException {
		char[] chArr = new char[1024];
		int len = reader.read(chArr);
		try {
			receive(String.copyValueOf(chArr, 0, len));
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onClose(int status) {
		LogUtil.infoPrintf("连接已关闭");
		super.onClose(status);
	}

	@Override
	protected void onOpen(WsOutbound outbound) {
		LogUtil.infoPrintf("连接已开启");
		super.onOpen(outbound);
	}

	public void send(String message) throws IOException {
		LogUtil.debugPrintf("send-->" + message);
		getWsOutbound().writeTextMessage(CharBuffer.wrap(message));
	}

	public void receive(String msg) throws JSONException, Exception {
		if(check()){
			LogUtil.webPrintf("软件已经过期，请联系供应商！");
		}else{
			LogUtil.debugPrintf("receive-->" + msg);
			JSONObject jsonobj = new JSONObject(msg);
			String action = (String) jsonobj.get("action");
			if (action.equalsIgnoreCase("login")) {
				doLogin((JSONObject) jsonobj.get("msg"));
			} else if (action.equalsIgnoreCase("start")) {
				doStart((JSONObject) jsonobj.get("msg"));
			} else if (action.equalsIgnoreCase("shop")) {
				doShop((JSONObject) jsonobj.get("msg"));
			} else if (action.equalsIgnoreCase("stop")) {
				doStop();
			} else if (action.equalsIgnoreCase("play")) {
				doPlay((JSONObject) jsonobj.get("msg"));
			}
		}
	}

	private boolean check(){
		String datestr = new SimpleDateFormat("yyyy-MM-dd").format(System.currentTimeMillis());
		return GenericUtil.isEndDate();
	}
	private void doPlay(JSONObject item) {
		equitem.setRadio(item.getBoolean("radio"));
	}

	private void doStart(JSONObject item) {
		if (snatcher != null && snatcher.getState().equalsIgnoreCase("running"))
			return;
		int creditlevel = Integer.parseInt((String) item.get("creditlevel"));
		String priceStr = (String) item.get("minprice");
		double minprice = (priceStr.equals("")) ? 0 : Double.parseDouble(priceStr);
		equitem = new EquItem(creditlevel, minprice,item.getBoolean("radio"));
		snatcher = new EquSnatcher(equitem);
		snatcher.start();
	}

	private void doStop() {
		snatcher.concel();
	}

	private void doShop(JSONObject params) throws IOException {
		if (this.login == null) {
			this.send("请先登陆！");
			return;
		}
		String price = (String) params.get("price");
		String orderNum = (String) params.get("orderNum");
		int type;
		if (orderNum.contains("DB")) {
			type = ShopThread.PTTYPE;
			price += "00";
		} else {
			type = ShopThread.JSTYPE;
		}
		common_params.put("orderNum", orderNum);
		common_params.put("price", price);
		ShopThread st = new ShopThread(this.login, common_params, orderNum,
				type);
		new Thread(st).start();
	}
	private void doLogin(JSONObject params) throws Exception {
		common_params = params;
		LogUtil.debugPrintf(params.toString());
		String username = (String) common_params.remove("username");
		String password = (String) common_params.remove("password");
		// JSONObject loginInfo = new
		// JSONObject("{username:'wlg7766',password:'54054110'}");
		JSONObject loginInfo = new JSONObject("{username:'" + username
				+ "',password:'" + password + "'}");
		LoginSys loginInstance = new LoginSys(loginInfo);
		loginInstance.doLogin();
		this.login = loginInstance;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
		LogUtil.infoPrintf("sessionid-->" + this.sessionId);
	}
	// public void setPREFIX(String pREFIX) {
	// PREFIX = pREFIX;
	// LogUtil.infoPrintf("PREFIX-->" + PREFIX);
	// }

}
