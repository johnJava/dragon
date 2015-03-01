/**
 * ��Ʒ�б�ҳjs
 */
$(document).ready(function() {
	// �ж�ҳ��Ԫ���Ƿ����
	if($("#dvMoneyMall").length <= 0){
		return;
	}
	
	// ��ȡ��Ŀ��Ʒ�б�
	var request = {};
	request.currentUrl = (function(){
		var currentUrl = window.location.href;
		// ȥ������Ĳ�������ֹ��̨���������������ַ��������쳣
		if (currentUrl.indexOf("?keyword=") != -1) {
			currentUrl = currentUrl.substring(0, currentUrl.indexOf("?keyword="));
		}
		return currentUrl;
	})();
//	request.currentUrl = "http://s.5173.com/wow-q2jm41-890-943-0-ou5epo-0-0-0-a-a-a-a-a-0-moneyaverageprice_asc-0-0.shtml";
//	request.currentUrl = "http://s.5173.com/bns-0-000txe-jfl1qs-iwys5h-0-0-0-0-a-a-a-a-a-0-0-0-0.shtml";
	
	$.ajax({
		type: "GET",
		url: baseServiceUrl + "services/goods/selectgoods",
		data: request,
		contentType: "application/json; charset=UTF-8",
		dataType:'jsonp',
		jsonp:'callback',
		success: function(resp) {
			var responseStatus = resp.responseStatus;
            var code = responseStatus.code;
            if (code == "00") {
            	var goodsList = resp.goodsList;
            	buildGoodsList(goodsList);
            }
		}
	});
});

// ��װ��Ʒ�б�ҳ��
function buildGoodsList(goodsList){
	if(isNull(goodsList)){
		return;
	}
	
	var html = "<div class='hot_recommen_tt'> <em></em>";
	html += "<div class='hot_buy_title'>";
	html += "<div class='hot_buy_w1'>�����ٶ�</div>";
	html += "<div class='hot_buy_w2'>��Ʒ����</div>";
	html += "<div class='hot_buy_w3'>��������</div>";
	html += "<div class='hot_buy_w4'>������</div>";
	html += "</div><strong><a id='yxbguanggao' href='http://yxbmall.5173.com/applyseller.html' target='_blank'>�����������������פ��</a></strong></div>";
	html += "<div class='hot_recobox'>";
	html += "<ul class='hot_buy_list'>";
	for (var int = 0; int < goodsList.length; int++) {
		var goodsInfo = goodsList[int];
		if(isNull(goodsInfo) || goodsInfo.isDeleted == true){
			continue;
		}
		
		var goodsInfoJson = $.toJSON(goodsInfo);
		goodsInfoJson = goodsInfoJson.replace("\\","/");
		var count = 5000;
		if(goodsInfo.gameName=="����"){
			count = 10000;
			}
		if(goodsInfo.gameName=="ħ������(����)"){
			count = 20000;
			}
		var image = isNull(goodsInfo.imageUrls)?"":buildImageUrl(goodsInfo.imageUrls,"55x55");
		var deliveryTime = isNull(goodsInfo.deliveryTime)?"":goodsInfo.deliveryTime;
		var title = isNull(goodsInfo.title)?"":goodsInfo.title;
		var price = toDecimal2(1/parseFloat(goodsInfo.unitPrice)); // 1Ԫ��Ӧ���ٽ�
		var totalPrice = toDecimal2(parseFloat(goodsInfo.unitPrice) * count);
		var totalPriceArray = totalPrice.toString().split(".");
		var displayGameInfo = goodsInfo.region+"/"+goodsInfo.server+"/"+(isNull(goodsInfo.gameRace)?"":goodsInfo.gameRace);
		var moneyName = isNull(goodsInfo.moneyName)?"��":goodsInfo.moneyName;
		var itemhtml = "<li id='goodsId_"+goodsInfo.id+"' goodsInfo='"+goodsInfoJson+"'>";
		itemhtml += "<div class='hot_buy_detail_w0'><div class='hot_img'><img src='"+image+"' width='55' height='55'></div></div>";
		// itemhtml += "<div class='hot_buy_detail_w1'><p class='hot_time'><strong>"+deliveryTime+"</strong>���ӷ���</p><p class='hot_service'>"+displayGameInfo+"</p></div>";
		itemhtml += "<div class='hot_buy_detail_w1'><p class='hot_time'><strong>"+title+"</strong></p><p class='hot_service'>"+displayGameInfo+"</p></div>";
		itemhtml += "<div class='hot_buy_detail_w2'><p class='hot_price'>1Ԫ=<span>"+price+"</span>"+moneyName+"</p></div>";
		itemhtml += "<div class='hot_buy_detail_w3'><div class='select_list_input' onclick='selectGoldList("+goodsInfo.id+")'>" +
				"<input id='"+goodsInfo.id+"' class='input_select_list_number' value="+count+"><span class='select_list_unit'>"+moneyName+"</span></div></div>";
		itemhtml += "<div class='hot_buy_detail_w4'><span class='hot_price_account_buy'><strong>"+totalPriceArray[0]+"</strong>."+totalPriceArray[1]+"<em>Ԫ</em></span></div>";
		itemhtml += "<div class='hot_buy_detail_w5'><a href='javascript:buynowHotList("+goodsInfo.id+");' class='a_buy_now'>��������</a></div>";
		itemhtml += "</li>";
		
		html += itemhtml;
	}
	
	html += "</ul></div>";
	$("#dvMoneyMall").attr("class","hot_recommend");
	$("#dvMoneyMall").html(html);

	// ��פ�������
	setInterval(function(){changeColor()}, 300);
	
	// ʵʱ��أ�input������ı仯���Լ�ʱ���ɼ۸�
	$('.input_select_list_number').bind('input propertychange', function() {
		// ��ǰ��������
		var currentGoldCount = $(this).val();
		
		if(isNull(currentGoldCount)){
			currentGoldCount = 0;	
		}
		
		if(!isNumber(currentGoldCount)){
			alert("����������");
			return;
		}
		
		// ����������99999999
		if(parseInt(currentGoldCount)>=99999999){
			currentGoldCount = 99999999;
			$(this).val(99999999);
		}
		
		// ������С��0
		if(parseInt(currentGoldCount)<0){
			currentGoldCount = 0;
			$(this).val(0);
		}
		
	    var goodsInfoJson = $("#goodsId_"+$(this).attr("id")).attr("goodsInfo");
		var goodsInfo = $.evalJSON(goodsInfoJson);
		var disCountList = goodsInfo.discountList;
		
		var discount = 1;
		if(!isNull(disCountList)){
			for(var i=0; i<disCountList.length; i++){
				if(currentGoldCount >= disCountList[i].goldCount){
					discount = disCountList[i].discount;
				}
			}
		}
		var totalPrice = toDecimal2(parseFloat(goodsInfo.unitPrice) * parseInt(currentGoldCount) * parseFloat(discount));
		var totalPriceArray = totalPrice.toString().split(".");
		$("#goodsId_"+$(this).attr("id")+" .hot_price_account_buy").html("<strong>"
					+totalPriceArray[0]+"</strong>."+totalPriceArray[1]+"<em>Ԫ</em>");
	});
	
	// �ĵ���ʼ����ʱ��Ĭ����Ʒ�����5000�������¼��������ۿ�
	$(".input_select_list_number").trigger("input");
	
}

// չ���ۿ۽�����б�
function selectGoldList(goodsId){
	// �жϵ�ǰ�ۿ��Ƿ���չ��
	if($("#goodsId_"+goodsId+" .select_list_popup").length > 0){
		// ɾ��ȫ������
		$(".select_list_popup").remove();
		return;
	}
	
	// ��ǰδչ��
	// ɾ������ȫ������
	$(".select_list_popup").remove();
	
	// չ�ֵ�ǰ����
	var goodsInfoJson = $("#goodsId_"+goodsId).attr("goodsInfo");
	var goodsInfo = $.evalJSON(goodsInfoJson);
	var disCountList = goodsInfo.discountList;
	
	// ��Ʒδ�����ۿ���
	if(isNull(disCountList)){
		return;
	}
	
	var html = "<div class='select_list_popup'><ul>";
	
	for(var i=0; i<disCountList.length; i++){
		var disCountInfo = disCountList[i];
		var displayDiscount = toDecimal2(parseFloat(disCountInfo.discount)*10);
		var itemHtml = "<li goldCount="+disCountInfo.goldCount+"><a href='javascript:void(0)'>"+disCountInfo.goldCount+"<span>"
			+displayDiscount+"��</span></a></li>";
		html += itemHtml;
	}
	
	html += "</ul></div>";
	
	$("#goodsId_"+goodsId+" .select_list_unit").after(html);
	
	// ѡ���������¼�
	$(".select_list_popup li").click(function(){
		$("#goodsId_"+goodsId+" .input_select_list_number").val($(this).attr("goldCount"));
		// �����¼�
		$("#goodsId_"+goodsId+" .input_select_list_number").trigger("input");
	});
}

// ��������
function buynowHotList(goodsId){
	// ��ת��������ҳ
	var goldCount = $("#goodsId_"+goodsId+" .input_select_list_number").val();
	if(isNull(goldCount) || goldCount == 0){
		alert("��������Ҫ�������Ϸ������");
		return;
	}
	if(!isNumber(goldCount)){
		alert("����������");
		return;
	}
	var goodsInfoJson = $("#goodsId_"+goodsId).attr("goodsInfo");
	var goodsInfo = $.evalJSON(goodsInfoJson);
	
	if(parseInt($("#goodsId_"+goodsId+" .hot_price_account_buy" + " strong").html()) < 20){
		alert("���׶������20Ԫ");
		return;
	}
	var gameName = escape(goodsInfo.gameName);
	var gameRegion = escape(goodsInfo.region);
	var gameServer = escape(goodsInfo.server);
	var gameRace = isNull(goodsInfo.gameRace)?"":escape(goodsInfo.gameRace);
	var gameId = isNull(goodsInfo.gameId)?"":escape(goodsInfo.gameId);
	var regionId = isNull(goodsInfo.regionId)?"":escape(goodsInfo.regionId);
	var serverId = isNull(goodsInfo.serverId)?"":escape(goodsInfo.serverId);
	var raceId = isNull(goodsInfo.raceId)?"":escape(goodsInfo.raceId);
	var goodsCat = escape(goodsInfo.goodsCat);
	
	window.open(baseHtmlUrl + "createorder.html?gameId="+gameId+"&regionId="+
			regionId+"&serverId="+serverId+"&raceId="+raceId+"&gameName="+gameName+"&gameRegion="+
			gameRegion+"&gameServer="+gameServer+"&gameRace="+gameRace+
				"&goodsCat="+goodsCat+"&goldCount="+goldCount+"&", "_blank");

}

// ������פ������ɫ
var i = 0;
function changeColor(){
	$("#yxbguanggao").css("color",i==0?"#06c":"#f60");
	i==2?i=0:i++;
	
}
