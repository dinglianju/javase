package com.dlj.bean;

import java.io.Serializable;

/**
 * 封装控制器返回给前台的信息内容，只要springMvc中的控制器或其他以该对象为返回值的公共方法，都会触发在配置文件中配置的myAop配置，
 * 在该方法调用之前和之后进行请求执行时间统计，异常处理及日志记录的操作。
 * @author zhxg
 *
 * @param <T>
 */
public class ResultBean<T> implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final int NO_LOGIN = -1;
	
	public static final int SUCCESS = 0;
	
	public static final int CHECK_FAIL = 1;
	
	public static final int NO_PERMISSION = 2;
	
	public static final int UNKNOWN_EXCEPTION = -99;
	
	public static final int FAIL = 3; // 操作失败
	
	/*
	 * 返回的信息（主要出错的时候使用）
	 */
	private String msg = "success";
	
	/*
	 * 接口返回码， 0表示成功，其他看对应的定义
	 * 推荐做法：
	 * 0	： 成功
	 * >0	： 表示已知的异常（例如提示错误等，需要调用地方单独处理）
	 * <0	： 表示未知的异常（不需要单独处理，调用方统一处理）
	 */
	private int code = SUCCESS;
	
	/*
	 * 返回的数据
	 */
	private T data;
	
	public ResultBean() {
		super();
	}
	
	public ResultBean(T data) {
		super();
		this.data = data;
	}
	
	public ResultBean(int code, T data) {
		super();
		this.code = code;
		this.data = data;
	}
	
	public ResultBean(Throwable e) {
		super();
		this.msg = e.toString();
		this.code = UNKNOWN_EXCEPTION;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}

	@Override
	public String toString() {
		return "ResultBean [msg=" + msg + ", code=" + code + ", data=" + data + "]";
	}
	
	
}
