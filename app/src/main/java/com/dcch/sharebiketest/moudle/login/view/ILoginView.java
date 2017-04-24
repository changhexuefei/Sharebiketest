package com.dcch.sharebiketest.moudle.login.view;

/**
 * Created by kaede on 2015/5/18.
 */
public interface ILoginView {
	/**
	 * 登录成功
	 */
	void onLoginSuccess();

	/**
	 * 登录失败
	 *
	 * @param error
	 */
	void onLoginFailed(String error);
}
