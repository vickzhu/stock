package com.gesangwu.spider.web.controller;

import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.gandalf.framework.util.StringUtil;
import com.gandalf.framework.web.tool.Page;
import com.gesangwu.spider.biz.dao.model.FiveRangeStatis;
import com.gesangwu.spider.biz.dao.model.FiveRangeStatisExample;
import com.gesangwu.spider.biz.service.FiveRangeStatisService;

@Controller
@RequestMapping("/fiverange")
public class FiveRangeController {
	
	private static final Logger logger = LoggerFactory.getLogger(FiveRangeController.class);
	
	@Resource
	private FiveRangeStatisService frss;
	
	@RequestMapping(value = "/r{range}", method = RequestMethod.GET)
	public ModelAndView list(HttpServletRequest request,String tradeDate, @PathVariable("range")String range){
		double min = 0;
		double max = 50;
		if("1".equals(range)){//一档
			max = 25;
		} else if("2".equals(range)){//二档
			min = 25;
		} else {
			if(!"a".equals(range)){
				range = "a";
			}
		}
		List<FiveRangeStatis> statisList = frss.selectByTradeDate(tradeDate,min,max);
		ModelAndView mav = new ModelAndView("fiveRangeList");
		mav.addObject("list", statisList);
		mav.addObject("amvRange", range);
		if(CollectionUtils.isNotEmpty(statisList)){
			mav.addObject("tradeDate", statisList.get(0).getTradeDate());
		} else {
			mav.addObject("tradeDate", tradeDate);
		}
		return mav;
	}
	
	@RequestMapping(value="/history", method = RequestMethod.GET)
	public ModelAndView history(HttpServletRequest request, String symbol){
		if(StringUtil.isBlank(symbol)){
			logger.debug("No symbol find for largeVol history!");
		}
		String curPageStr = request.getParameter("curPage");
		int curPage = 1;
		if(StringUtil.isNotBlank(curPageStr)){
			curPage = Integer.valueOf(curPageStr);
		}
		Page<FiveRangeStatis> lvsPage = new Page<FiveRangeStatis>(curPage, 20);
		ModelAndView mav = new ModelAndView("fiveRangeHistory");
		FiveRangeStatisExample example = new FiveRangeStatisExample();
		example.setOrderByClause("trade_date desc");
		FiveRangeStatisExample.Criteria criteria = example.createCriteria();
		criteria.andSymbolEqualTo(symbol);
		frss.selectByPagination(example, lvsPage);
		mav.addObject("page", lvsPage);
		mav.addObject("symbol", symbol);
		return mav;
	}
}
