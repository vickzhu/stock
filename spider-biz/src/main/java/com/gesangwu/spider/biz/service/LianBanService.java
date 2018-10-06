package com.gesangwu.spider.biz.service;

import java.util.List;

import com.gandalf.framework.mybatis.BaseService;
import com.gesangwu.spider.biz.dao.model.LianBan;
import com.gesangwu.spider.biz.dao.model.LianBanExample;

public interface LianBanService extends BaseService<LianBan, LianBanExample> {

	public void batchInsert(List<LianBan> lbList);
	
}