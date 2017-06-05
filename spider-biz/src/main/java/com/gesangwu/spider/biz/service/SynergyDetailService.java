package com.gesangwu.spider.biz.service;

import java.util.List;

import com.gandalf.framework.mybatis.BaseService;
import com.gandalf.framework.mybatis.KeyValue;
import com.gesangwu.spider.biz.dao.model.SynergyDetail;
import com.gesangwu.spider.biz.dao.model.SynergyDetailExample;

public interface SynergyDetailService extends
		BaseService<SynergyDetail, SynergyDetailExample> {
	
	public void insertSynergyDetailBatch(List<SynergyDetail> sdList);
	
	public List<KeyValue<Integer, Integer>> relateDept(List<String> list);
	
}
