package com.gesangwu.spider.biz.common;

public enum ShapeEnum {
	
	DI_BU(11, "底部企稳"), FIRST_NEG(12, "龙头首阴"), MA_ADH(13, "均线粘合"), DOUBLE_BREAK(14, "双突破"), COVER_NEG(15, "阳包阴");

	private int code;
	private String desc;
	
	private ShapeEnum(int code, String desc){
		this.code = code;
		this.desc = desc;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}
	
}
