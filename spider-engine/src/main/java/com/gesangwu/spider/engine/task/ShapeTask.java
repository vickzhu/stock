package com.gesangwu.spider.engine.task;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.gandalf.framework.mybatis.KeyValue;
import com.gandalf.framework.util.StringUtil;
import com.gesangwu.spider.biz.dao.model.KLine;
import com.gesangwu.spider.biz.dao.model.KLineExample;
import com.gesangwu.spider.biz.service.KLineService;

/**
 * K线形态，包括多头、空头、阳包阴等等
 * @author zhuxb
 *
 */
@Component
public class ShapeTask {
		
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	
	@Resource
	private KLineService klService;
	
//	@Scheduled(cron = "0 30 15 * * MON-FRI")
	public void execute(){
		execute(null);		
	}
	
	public void execute(String tradeDate){
		if(StringUtil.isBlank(tradeDate)){
			Date now = new Date();
			tradeDate = sdf.format(now);
		}
		List<KLine> klList = klService.selectByShape(tradeDate);
		List<Long> idList = new ArrayList<Long>();
		for (KLine kl : klList) {
			if(isValid(tradeDate, kl)){
				idList.add(kl.getId());
			}
		}
		klService.updateShape(1, idList);
	}
	
	public boolean isValid(String tradeDate, KLine kl){
		KLineExample example = new KLineExample();
		example.setOffset(0);
		example.setRows(10);
		example.setOrderByClause("trade_date desc");
		KLineExample.Criteria criteria = example.createCriteria();
		criteria.andSymbolEqualTo(kl.getSymbol());
		criteria.andTradeDateLessThanOrEqualTo(tradeDate);
		List<KeyValue<String, Double>> cList = klService.selectLastestClose(example);
		KeyValue<String, Double> maxKV = cList.get(0);
		KeyValue<String, Double> minKV = cList.get(cList.size() - 1);
		if(maxKV.getKey().compareTo(minKV.getKey()) < 0){//最高点日期小于最低点日期，说明是上一波的高峰
			return false;
		}
		double close = kl.getClose();
		double max = maxKV.getValue();
		double min = minKV.getValue();
		double h1 = (close - min)/min;
		if(h1 < 0.1){//值越小离底部越近
			return false;
		}
		double h2 = (close - max)/max;
		if(Math.abs(h2) > 0.1){//离最大值大于10%
			return false;
		}
		return true;
	}
	
}
