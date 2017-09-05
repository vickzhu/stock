package com.gesangwu.spider.engine.kshape.task;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.gandalf.framework.util.CalculateUtil;
import com.gesangwu.spider.biz.dao.model.KLine;
import com.gesangwu.spider.biz.dao.model.KLineExample;

@Component
public class CoverNegTask extends ShapeTask {
	
	private static final Logger logger = LoggerFactory.getLogger(CoverNegTask.class);
	
	public void execute(){
		logger.info("CoverNeg task begin...");
		long start = System.currentTimeMillis();
		execute(null);
		long end = System.currentTimeMillis();
		logger.info("CoverNeg task end, used:" + (end-start) + "ms");
	}
	
	public void execute(String tradeDate){
		tradeDate = getTradeDate(tradeDate);
		List<KLine> klList = klService.selectForShape(tradeDate);
		List<Long> idList = new ArrayList<Long>();
		for (KLine kl : klList) {
			if(kl.getClose() < kl.getMa5() || kl.getClose() < kl.getMa10()){
				continue;
			}
			double secondHigh = kl.getOpen() > kl.getClose()?kl.getOpen():kl.getClose();
			double scale = CalculateUtil.div(kl.getHigh(), secondHigh, 3);
			double diff = CalculateUtil.sub(scale, 1, 3);
			if(diff < 0.02){
				continue;
			}
			KLine kLine = getMaxMa5(kl, 30);
			if(kLine == null){
				continue;
			}
			if(kl.getClose() > kLine.getMa5() || kl.getHigh() < kLine.getMa5()){
				continue;
			}
			boolean valid = isValid(kl);
			if(valid){
				System.out.println(kl.getSymbol());
				idList.add(kl.getId());
			}
		}
		if(idList.size() > 0){
//			klService.updateShape(ShapeEnum.MA_ADH, idList);
		}
	}
	
	private KLine getMaxMa5(KLine kl, int days){
		String startDate = subDate(kl.getTradeDate(), days);
		KLineExample example = new KLineExample();
		example.setOrderByClause("ma5 desc");
		example.setOffset(0);
		example.setRows(1);
		KLineExample.Criteria criteria = example.createCriteria();
		criteria.andSymbolEqualTo(kl.getSymbol());
		criteria.andTradeDateGreaterThanOrEqualTo(startDate);
		criteria.andTradeDateLessThan(kl.getTradeDate());
		List<KLine> klList = klService.selectByExample(example);
		return CollectionUtils.isEmpty(klList) ? null : klList.get(0);
	}

	public boolean isValid(KLine kl){
		if(!isAroundTop(kl, 180)){
			return false;
		}
		List<KLine> list = getKLList(kl.getSymbol(), kl.getTradeDate());
		if(!isUpTrend(kl, 30)){
			return false;
		}
		for(int i = 0; i < list.size(); i++){
			KLine curK = list.get(i);
			if(i == 0){
				if(kl.getMa5() < curK.getMa5() || kl.getMa10() < curK.getMa10()){
					return false;
				}
			} else {
				KLine nextK = list.get(i-1);
				if(curK.getMa5() > nextK.getMa5() || curK.getMa10() > nextK.getMa10()){
					return false;
				}
			}
		}
		return true;
	}
	
	private List<KLine> getKLList(String symbol, String tradeDate){
		KLineExample example = new KLineExample();
		example.setOrderByClause("trade_date desc");
		example.setOffset(0);
		example.setRows(2);
		KLineExample.Criteria criteria = example.createCriteria();
		criteria.andSymbolEqualTo(symbol);
		criteria.andTradeDateLessThan(tradeDate);
		return klService.selectByExample(example);
	}
}
