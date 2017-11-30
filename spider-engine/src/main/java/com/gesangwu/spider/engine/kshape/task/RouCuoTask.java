package com.gesangwu.spider.engine.kshape.task;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.gandalf.framework.web.tool.Page;
import com.gesangwu.spider.biz.common.ShapeEnum;
import com.gesangwu.spider.biz.dao.model.KLine;
import com.gesangwu.spider.biz.dao.model.KLineExample;

/**
 * 揉搓线
 * <ol>
 * <li>多头形态，连续两天相对于前一天缩量明显</li>
 * <li>前一天必须是阳线，成交额大于1亿</li>
 * <li>本轮上涨必须有明显的涨幅，如果本轮涨幅只有一天，则涨幅须大于5%。如果本轮涨幅有两天，且第二天为小于2%的缩量上涨，则两条的涨幅必须累计10%以上</li>
 * <li>第一日阴线，第二日真阴线</li>
 * <li>第二日跳空低开</li>
 * <li>收盘价低于五日线</li>
 * </ol>
 * @author zhuxb
 *
 */
@Component
public class RouCuoTask extends ShapeTask {
	
	private static final Logger logger = LoggerFactory.getLogger(RouCuoTask.class);

	@Scheduled(cron="0 21 15 * * MON-FRI")
	public void execute(){
		logger.info("Rou Cuo task begin...");
		long start = System.currentTimeMillis();
		execute(null);
		long end = System.currentTimeMillis();
		logger.info("Rou Cuo task end, used:" + (end-start) + "ms");
	}
	
	public void execute(String tradeDate){
		tradeDate = getTradeDate(tradeDate);
		List<Long> idList = new ArrayList<Long>();
		execute(1, tradeDate, idList);
		if(CollectionUtils.isNotEmpty(idList)){
			klService.updateShape(ShapeEnum.ROU_CUO, idList);
		}
	}
	
	public void execute(int curPage, String tradeDate, List<Long> idList){
		KLineExample example = new KLineExample();
		KLineExample.Criteria criteria = example.createCriteria();
		criteria.andTradeDateEqualTo(tradeDate);
		criteria.andPercentLessThan(0d);
		criteria.andPercentGreaterThan(-5d);
		criteria.andMa5IsNotNull();
		criteria.andMa10IsNotNull();
		Page<KLine> page = new Page<KLine>(curPage, 500);
		klService.selectByPagination(example, page);
		List<KLine> klList = page.getRecords();
		judge(klList, idList);
		int totalPage = page.getTotalPages();
		if(totalPage == curPage){
			return;
		} else {
			curPage++;
			execute(curPage, tradeDate, idList);
		}
	}
	
	public void judge(List<KLine> klList, List<Long> idList){
		for (KLine kl : klList) {
			if(kl.getMa5() < kl.getMa10()){//FIXME:这里不满足也是可以的
				continue;
			}
			if(kl.getOpen() < kl.getClose()){
				continue;
			}
			if(kl.getOpen() >= kl.getYesterdayClose()){
				continue;
			}
			List<KLine> preKlList = listByTradeDateDesc(kl.getSymbol(), kl.getTradeDate(), 2);
			KLine pre1 = preKlList.get(0);
			if(pre1.getPercent() > 0){
				continue;
			}
			if(pre1.getClose() < pre1.getMa5()){
				continue;
			}
			if(pre1.getOpen() < pre1.getClose()){
				continue;
			}
			KLine pre2 = preKlList.get(1);
//			if(pre2.getAmount() < 100000000){
//				continue;
//			}
			if(pre2.getClose() < pre2.getMa5()){
				continue;
			}
			if(pre2.getOpen() > pre2.getClose()){
				continue;
			}
			if(kl.getVolume() > pre2.getVolume() || pre1.getVolume() > pre2.getVolume()){
				continue;
			}
			idList.add(kl.getId());
		}
	}
	
}
