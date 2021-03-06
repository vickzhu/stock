package com.gesangwu.spider.biz.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import com.gandalf.framework.mybatis.BaseMapper;
import com.gandalf.framework.mybatis.BaseServiceImpl;
import com.gandalf.framework.mybatis.KeyValue;
import com.gandalf.framework.web.tool.Page;
import com.gesangwu.spider.biz.common.ShapeEnum;
import com.gesangwu.spider.biz.dao.mapper.KLineMapper;
import com.gesangwu.spider.biz.dao.model.KLine;
import com.gesangwu.spider.biz.dao.model.KLineExample;
import com.gesangwu.spider.biz.service.KLineService;

@Service
public class KLineServiceImpl extends BaseServiceImpl<KLine, KLineExample> implements
		KLineService {
	
	@Resource
	private KLineMapper mapper;

	@Override
	protected BaseMapper<KLine, KLineExample> getMapper() {
		return mapper;
	}

	@Override
	public void batchInsert(List<KLine> kLineList) {
		mapper.batchInsert(kLineList);
	}

	@Override
	public void selectByPagination(KLineExample example, Page<KLine> page) {
		example.setOffset(page.getOffset());
        example.setRows(page.getPageSize());
        int totalCounts = mapper.countByExample(example);
        page.setTotalCounts(totalCounts);
        List<KLine> list = mapper.selectByExample(example);
        page.setRecords(list);
	}

	@Override
	public String selectLatestDate() {
		return mapper.selectLatestDate();
	}

	@Override
	public List<KLine> selectForShape(String tradeDate) {
		return mapper.selectForShape(tradeDate);
	}

	@Override
	public void updateShape(ShapeEnum shape, List<Long> idList) {
		if(CollectionUtils.isEmpty(idList)){
			return;
		}
		mapper.updateShape(shape.getCode(), idList);		
	}

	@Override
	public List<KeyValue<String, Double>> selectLastestClose(KLineExample example) {
		return mapper.selectLastestClose(example);
	}

	@Override
	public List<KLine> selectByPositive(String tradeDate) {
		return mapper.selectByPositive(tradeDate);
	}

	@Override
	public List<KLine> selectCoverNeg(String tradeDate) {
		return mapper.selectCoverNeg(tradeDate);
	}

	@Override
	public List<KLine> selectForYiZi(String tradeDate) {
		return mapper.selectForYiZi(tradeDate);
	}

	@Override
	public KLine selectByDate(String symbol, String tradeDate) {
		KLineExample example = new KLineExample();
		KLineExample.Criteria criteria = example.createCriteria();
		criteria.andSymbolEqualTo(symbol);
		criteria.andTradeDateEqualTo(tradeDate);
		List<KLine> klList = mapper.selectByExample(example);
		return CollectionUtils.isNotEmpty(klList) ? klList.get(0) : null;
	}

}
