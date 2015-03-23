package com.vo;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.common.GenericUtil;
import com.common.UrlType;

public class EquItem {
	private int creditlevel;
	private double minprice;
	private boolean radio;
	private String baseurl="http://s.5173.com/dnf-0-0-0-0-2zkpio-0-0-0-a-a-a-a-a-0-addnewdate_desc-0-0.shtml";
//	private String baseurl="http://s.5173.com/dnf-0-0-0-0-bx1xiv-0-0-0-a-a-a-a-a-0-addnewdate_desc-0-0.shtml";
	private Set<String> creditSet=null;
	private int pageIndex=0;
	
	public EquItem(int creditlevel) {
		this(creditlevel, Double.MAX_VALUE);
	}
	public EquItem(int creditlevel,double minprice) {
		this(creditlevel, minprice, true);
	}
	public EquItem(int creditlevel,double minprice,boolean radio) {
		this.creditlevel=creditlevel;
		this.minprice=minprice;
		this.radio=radio;
		initCreditSet();
	}
	
	private void initCreditSet(){
		creditSet = new HashSet<String>();
		for (int i = 0; i<creditlevel&&i < GenericUtil.creditlist.size(); i++) {
			creditSet.add(GenericUtil.creditlist.get(i));
		}
	}
	public boolean hasCredit(String credit){
		//return this.creditSet.contains(credit);
		
		return true;
	}
	public boolean matchPrice(double price){
		boolean flag=false;
		if(price>this.minprice)flag=true;
		return flag;
	}
	public boolean isPlayed(){
		return this.radio;
	}
	public String getUrl(UrlType type){
		String strurl="";
		strurl=this.baseurl.replaceAll("a-a-a-a-a", "a-a-a-10-"+Math.round(Math.random()*100000+100000));
		switch (type) {
		case HPURL:
			strurl=strurl+getKeyword()+"&rand="+new Date().getTime();
			break;
		case WPURL:
			if(this.pageIndex!=0){
				strurl=strurl.replaceAll("0-0.shtml", "0-"+this.pageIndex+".shtml")+getKeyword()+"&rand="+new Date().getTime();
			}else{
				strurl="";
			}
			break;
		case UPDATEPAGEINDEX:
			strurl=strurl.replaceAll("0-addnewdate_desc-0-0.shtml", "1-addnewdate_desc-0-0.shtml")+getKeyword()+"&rand="+new Date().getTime();
			break;
		}
		return strurl;
	}
	private String getKeyword(){
		return "?keyword="+new SimpleDateFormat("yyyyMMdd").format(System.currentTimeMillis());
	}
	public void setPageIndex(int pageIndex) {
		this.pageIndex = pageIndex;
	}
	public void setRadio(boolean radio) {
		this.radio = radio;
	}
	
}
