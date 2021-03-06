package com.gesangwu.spider.engine.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;
import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.SimpleNodeIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gandalf.framework.constant.SymbolConstant;
import com.gandalf.framework.net.HttpTool;
import com.gandalf.framework.spring.ContextHolder;
import com.gandalf.framework.util.StringUtil;
import com.gesangwu.spider.biz.common.DecimalUtil;
import com.gesangwu.spider.biz.common.StockUtil;
import com.gesangwu.spider.biz.dao.cache.LongHuTypeCache;
import com.gesangwu.spider.biz.dao.model.LongHu;
import com.gesangwu.spider.biz.dao.model.LongHuDetail;
import com.gesangwu.spider.biz.dao.model.LongHuType;
import com.gesangwu.spider.biz.dao.model.SecDept;
import com.gesangwu.spider.biz.service.LongHuTypeService;
import com.gesangwu.spider.biz.service.SecDeptService;

public class WangYiLongHuTool {
	
	private static final Logger logger = LoggerFactory.getLogger(WangYiLongHuTool.class);
	
	private static final String r = "\"SYMBOL\"\\:\"([0-9]{6})\",\"SNAME\"\\:\"([^\"]*)\",\"TCLOSE\"\\:\"([0-9\\.]*)\",\"PCHG\"\\:\"([0-9\\.\\-]*)\",\"SMEBTSTOCK1\"\\:\"([^\"]*)\",\"VOTURNOVER\"\\:[0-9\\.]*,\"VATURNOVER\"\\:[0-9\\.]*,\"TDATE\"\\:\"[0-9\\-]*\",\"EXCHANGE\"\\:\"[0|1]\",\"SCSTC27\"\\:\"([0-9\\.\\%]*)\",\"SMEBTSTOCK11\"\\:\"([0-9]*)\"";
	private static final Pattern p = Pattern.compile(r);

	public static Map<String,List<String>> getLongHuType(String tradeDate){
		String url = buildUrl(tradeDate);
		String result = HttpTool.get(url);
		Map<String,List<String>> longHuMap = new HashMap<String,List<String>>();
		Matcher m = p.matcher(result);
		while(m.find()){
			String code = m.group(1);
			String type = m.group(7);
			List<String> list = longHuMap.get(code);
			if(list == null){
				list = new ArrayList<String>();
				longHuMap.put(code, list);
			}
			list.add(type);
		}
		return longHuMap;
	}
	
	public static List<LongHu> getLongHuList(String tradeDate){
		List<LongHu> longHuList = new ArrayList<LongHu>();
		Map<String, LongHu> longHuMap = getLongHu(tradeDate);
		for(Map.Entry<String, LongHu> entry:longHuMap.entrySet()){
			longHuList.add(entry.getValue());
		}
		return longHuList;
	}
	
	private static Map<String, LongHu> getLongHu(String tradeDate){
		String url = buildUrl(tradeDate);
		String result = HttpTool.get(url);
		Map<String, LongHu> longHuMap = new HashMap<String, LongHu>();
		Matcher m = p.matcher(result);
		Date now = new Date();
		while(m.find()){
			String code = m.group(1);
			String symbol = StockUtil.code2Symbol(code);
			LongHu longHu = longHuMap.get(symbol);
			if(longHu == null){
				longHu = new LongHu();
				double close = Double.valueOf(m.group(3)); 
				double chgPercent = Double.valueOf(m.group(4)); 
				String turnoverStr = m.group(6);
				turnoverStr = turnoverStr.replaceAll("\\%", StringUtil.EMPTY);
				double turnover = Double.valueOf(turnoverStr);
				String stockName = UnicodeUtil.decodeUnicode(m.group(2));
				longHu.setSymbol(symbol);
				longHu.setStockName(stockName);
				longHu.setTradeDate(tradeDate);
				longHu.setPrice(close);
				longHu.setChgPercent(chgPercent);
				longHu.setTurnover(turnover);
				longHu.setGmtCreate(now);
				longHuMap.put(symbol, longHu);
			}
			String type = m.group(7);
			LongHuType lhType = LongHuTypeCache.get(type);
			if(lhType == null){
				lhType = new LongHuType();
				String typeDesc = UnicodeUtil.decodeUnicode(m.group(5));
				int dateType = getDateType(typeDesc);
				lhType.setDateType(dateType);
				lhType.setLhDesc(typeDesc);
				lhType.setLhType(type);
				ContextHolder.getContext().getBean(LongHuTypeService.class).insert(lhType);
			}
			if(lhType.getDateType() == 1){
				String yrType = longHu.getYrType();
				if(StringUtil.isBlank(yrType)){
					longHu.setYrType(lhType.getLhType());
				} else {
					longHu.setYrType(longHu.getYrType()+SymbolConstant.COMMA+lhType.getLhType());
				}
			} else if(lhType.getDateType() == 3){
				String srType = longHu.getSrType();
				if(StringUtil.isBlank(srType)){
					longHu.setSrType(lhType.getLhType());
				} else {
					longHu.setSrType(longHu.getSrType()+SymbolConstant.COMMA+lhType.getLhType());
				}
			} else if(lhType.getDateType() ==2){
				String erType = longHu.getErType();
				if(StringUtil.isBlank(erType)){
					longHu.setErType(lhType.getLhType());
				} else {
					longHu.setErType(longHu.getErType()+SymbolConstant.COMMA+lhType.getLhType());
				}
			}
		}
		return longHuMap;
	}
	
	private static int getDateType(String typeDesc){
		int dateType = 1;
		if(typeDesc.indexOf("三个")>0){
			dateType = 3;
		}else if(typeDesc.indexOf("2个")>0){
			dateType = 2;
		}
		return dateType;
	}
	
	
	
	private static String buildUrl(String tradeDate){
		StringBuilder sb = new StringBuilder();
		sb.append("http://quotes.money.163.com/hs/marketdata/service/lhb.php?page=0&sort=TDATE&order=desc&count=150&type=query&callback=cb&query=start:");
		sb.append(tradeDate);
		sb.append(";end:");
		sb.append(tradeDate);
		return sb.toString();
	}
	
	public static List<LongHuDetail> fetchDetail(String code, String tradeDate, String lhType){
		String url = buildDetailUrl(code, tradeDate, lhType);
		try {
            //生成一个解析器对象，用网页的 url 作为参数
//			Parser parser = new Parser(url);
			String result = HttpTool.get(url);
			Parser parser = Parser.createParser(result, "UTF-8");
			//设置网页的编码,这里只是请求了一个 UTF-8 编码网页
			parser.setEncoding("UTF-8");
			//迭代所有节点, null 表示不使用 NodeFilter
			NodeList list = parser.parse(null);
            //从初始的节点列表迭代所有的节点
			return processNodeList(list);
		} catch (Exception e) {
			logger.error("Parse LongHu detail from 163 failed!",e);
		}
		return null;
	}
	
	private static List<LongHuDetail> processNodeList(NodeList list) {
		List<LongHuDetail> detailList = new ArrayList<LongHuDetail>();
		//迭代开始
		SimpleNodeIterator iterator = list.elements();
		while (iterator.hasMoreNodes()) {
			Node node = iterator.nextNode();
			if(node instanceof TagNode){
				TagNode tn = (TagNode)node;
				if("A".equals(tn.getTagName())){
					String href = tn.getAttribute("href");
					if(href.indexOf("agencylist") > 0){
						String deptCode = href.substring(href.indexOf("_")+1, href.indexOf("."));
						String deptName = tn.getFirstChild().getText();
						souDept(deptCode, deptName);
						Node buyNode = tn.getParent().getNextSibling();
						String buyAmtStr = buyNode.getFirstChild().getText().trim();
						Node sellNode = buyNode.getNextSibling().getNextSibling().getNextSibling().getNextSibling();
						String sellAmtStr = sellNode.getFirstChild().getText().trim();
						BigDecimal buyAmt = DecimalUtil.parse(buyAmtStr);
						BigDecimal sellAmt = DecimalUtil.parse(sellAmtStr);
						BigDecimal netBuy = buyAmt.subtract(sellAmt);
						LongHuDetail lhd = new LongHuDetail();
						lhd.setBuyAmt(buyAmt);
						lhd.setNetBuy(netBuy);
						lhd.setSecDeptCode(deptCode);
						lhd.setSellAmt(sellAmt);
						detailList.add(lhd);
					}
				}
			}
			//得到该节点的子节点列表
			NodeList childList = node.getChildren();
			if(null != childList){
				List<LongHuDetail> tmpList = processNodeList(childList);
				if(CollectionUtils.isNotEmpty(tmpList)){
					detailList.addAll(tmpList);
				}
			}
		}//end while
		return detailList;
	}
	
	/**
	 * 更新营业部信息
	 * @param deptCode
	 * @param deptName
	 */
	private static void souDept(String deptCode, String deptName){
		SecDeptService deptService = ContextHolder.getContext().getBean(SecDeptService.class);
		SecDept dept = deptService.selectByCode(deptCode);
		if(dept == null){
			dept = new SecDept();
			dept.setCode(deptCode);
			dept.setDeptAddr(deptName);
			dept.setGmtCreate(new Date());
			deptService.insert(dept);
		} else {
			dept.setDeptAddr(deptName);
			dept.setGmtUpdate(new Date());
			deptService.updateByPrimaryKey(dept);
		}
	}
	
	private static String buildDetailUrl(String code, String tradeDate, String lhType){
		StringBuilder sb = new StringBuilder();
		sb.append("http://quotes.money.163.com/hs/marketdata/mrlhbSub.php?symbol=");
		sb.append(code);
		sb.append("&type=");
		sb.append(lhType);
		sb.append("&date=");
		sb.append(tradeDate);
		return sb.toString();
	}
	
	public static void main(String[] args){
		String tradeDate = "2016-11-28";
		Map<String,List<String>> typeMap = getLongHuType(tradeDate);
		for(Map.Entry<String,List<String>> entry : typeMap.entrySet()){
			System.out.println(entry.getKey()+":"+entry.getValue());
		}
	}
}
