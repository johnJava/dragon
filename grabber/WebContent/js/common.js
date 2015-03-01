//��Ϸ�ҷ��ʷ���Url
var baseServiceUrl = "http://yxbmall.5173.com/gamegold-facade-frontend/";
//��Ϸ�ҷ���html
var baseHtmlUrl = "http://yxbmall.5173.com/";
//�ļ�ͼƬ�ȷ���Url
var imageServiceUrl = "http://yxbmall.5173.com";
//�û���½url(returnUrl:��¼�ɹ�������ַ),�����encode��Ӧpassport�������ε�����
var loginUrl = "https://passport.5173.com/?undecode=1&returnUrl="+escape(window.location.href);

////��Ϸ�ҷ��ʷ���Url
//var baseServiceUrl = "http://localhost:8080/gamegold-facade-frontend/";
////��Ϸ�ҷ���html
//var baseHtmlUrl = "http://localhost:8080/gamegold-facade-frontend/";
////�ļ�ͼƬ�ȷ���Url
//var imageServiceUrl = "http://www.wzitech.com";
////�û���½url(returnUrl:��¼�ɹ�������ַ)
//var loginUrl = "https://passport.5173.com:8887/?undecode=1&returnUrl="+escape(window.location.href);

// �û�����
var UserType = {
	"CustomerService":1, // �ͷ�
	"NomalManager":2,    // ����Ա
	"SystemManager":3,   // ϵͳ����Ա
	
	1:"�ͷ�",
	2:"����Ա",
	3:"ϵͳ����Ա",
	
	getText: function(value) {
		for(var p in ComplaintState){ 
    		if(value == p){
    			return ComplaintState[p];
    		}
        } 
		return "";
	}
}

// �������״̬
var CheckState = {
	"UnAudited":0,    // δ���
	"PassAudited":1,  // ���ͨ��
	"UnPassAudited":2 // ��˲�ͨ��
}

//��������
var TradeType = {
	"NoDivid":1, // ���潻��
	"Divided":2 // ��Ϸ���ʼ�
}

// ����״̬
var OrderState = {
	"WaitPayment":1, // ������
	"Paid":2,        // �Ѹ���
	"WaitDelivery":3,// ������
	"Delivery":4,    // �ѷ���
	"Statement":5,   // �ᵥ
	"Refund":6,      // ���˿�
	"Cancelled":7,   // ��ȡ��
	"Receive": 8,
	
	1:"������",
	2:"�Ѹ���",
	3:"������",
	4:"�ѷ���",
	5:"�ᵥ",
	6:"���˿�",
	7:"��ȡ��",
	8:"���ջ�",
	
	getText: function(value) {
		for(var p in OrderState){ 
    		if(value == p){
    			return OrderState[p];
    		}
        } 
		return "";
	}
}

/**
 * ����ʱ��ʾ
 * @param payTime ֧��ʱ��(�ѱ����л�)
 * @param delayTime (����ʱ���ڿ��Ե�������λ����)
 */
function countdownTime(payTime, delayTime){
	if(orderInfo_orderState.orderState == OrderState.Paid || 
			orderInfo_orderState.orderState == OrderState.WaitDelivery){
		// ��ǰʱ��
        if(!window.nowTime){
            window.nowTime = new Date().getTime();
        }

		window.nowTime = window.nowTime + 1 * 1000;

		// ����ʱ��
		var deadTime = payTime + delayTime * 60 * 1000;
		
		if(deadTime >= window.nowTime){
			// ��ʾ����ʱ
			var diffTime = deadTime - window.nowTime;
			var minutes = parseInt(diffTime/60/1000);
			var seconds = parseInt((diffTime - minutes*60*1000)/1000);
			
			// ��̬��ʾ
			$("#countdownTime").html(minutes+"����"+seconds+"��");
		}else {
			if(orderInfo_orderState.isDelay != true){
                if(window.currentcountdown) {
                    window.clearInterval(parseInt(window.currentcountdown));
                }
				
				delayOrder(orderInfo_orderState.orderId);
			}
		}
	}
}

/**
 * �ѷ������ʼķ�ʽʱ����ʾʲôʱ��ȡ��
 * ��ǰʱ��+�ʼ�ʱ��
 * @param mailTime
 */
function getMailTimeOnDelivery(sendTime, mailTime){
	if(isNull(sendTime) || isNull(mailTime)){
		return "0ʱ0�� ";
	}
	var laterTime = sendTime + mailTime * 60 * 1000;
	
	var date = new Date(laterTime);
	
	return " "+date.getHours() + "ʱ" + date.getMinutes() + "�� ";
}

// ��ȡURL��Ӧ����ֵ
function getUrlParam(name){
	var reg = new RegExp("(^|&)"+ name +"=([^&]*)(&|$)"); //����һ������Ŀ�������������ʽ����
	var r = window.location.search.substr(1).match(reg);  //ƥ��Ŀ�����
	if (r!=null) return unescape(r[2]); return null; //���ز���ֵ
}

// ��ȡcookie
function getAuthkey(){
//	return "5173auth";
	return $.cookie(".5173auth");
}

// ajax���ý���
$(document).ajaxComplete(function( event, xhr, settings ) {
	if (xhr.responseText.indexOf('{') === 0) {
		var response = $.evalJSON(xhr.responseText);
		if(isNull(response.responseStatus.code)){
			return;
		}
		// �Ƿ����û�auth
		if(response.responseStatus.code == "B1003"){
			// �����û���½
			window.location.href = loginUrl;
		}else {
			if(response.responseStatus.code != "00" && response.responseStatus.code != "11"){
				alert(response.responseStatus.message);
			}
		}
			
	}
});

var DEBUG = false;
if (typeof window.console === "undefined" || typeof  window.console.log === "undefined") {
    window.console = {};
    if (DEBUG) {
        console.log = function(msg) {
             alert(msg);
        };
    } else {
        console.log = function() {};
    }
}

/**
 * ����ͼƬurl
 * @param oriUrl ͼƬԭʼ·��
 * @param size  ���ɵ�ͼƬ��С
 */
function buildImageUrl(oriUrl,size){
	var geneUrl = "";
	if(isNull(oriUrl)){
		return geneUrl;
	}
	var rootUrl = oriUrl.replace(".jpg","");
	geneUrl = imageServiceUrl+rootUrl+"_"+size+".jpg";
	return geneUrl;
}

// �ж��Ƿ�Ϊ��
function isNull(value){
	if(jQuery.type(value) === "undefined" || jQuery.type(value) === "" 
    	|| jQuery.type(value) === "null" || value == "null" || value == null 
    	|| value == "" || value == "undefined"){
		return true;
	}else {
		return false;
	}
}

function isNumber(value){
	var reg = new RegExp("^[0-9]*$");
	if(!reg.test(value)){
        return false;
    }
	return true;
}

// ������λС��   
// ���ܣ����������������룬ȡС�����2λ  
function toDecimal(x) {  
    var f = parseFloat(x);  
    if (isNaN(f)) {  
        return;  
    }  
    f = Math.round(x*100)/100;  
    return f;  
}  

// ǿ�Ʊ���2λС�����磺2������2���油��00.��2.00  
function toDecimal2(x) {  
    var f = parseFloat(x);  
    if (isNaN(f)) {  
        return false;  
    }  
    var f = Math.round(x*100)/100;  
    var s = f.toString();  
    var rs = s.indexOf('.');  
    if (rs < 0) {  
        rs = s.length;  
        s += '.';  
    }  
    while (s.length <= rs + 2) {  
        s += '0';  
    }  
    return s;  
}
