package com.dcch.sharebiketest.moudle.login.presenter;

import com.dcch.sharebiketest.moudle.login.bean.User;
import com.dcch.sharebiketest.moudle.login.model.UserModel;
import com.dcch.sharebiketest.moudle.login.view.ILoginView;
import com.dcch.sharebiketest.utils.UIHandler;


public class LoginPresenter {
	private final ILoginView userView;
	private final UserModel userMode;

	public LoginPresenter(ILoginView userView) {
		this.userView = userView;
		this.userMode = new UserModel();
	}

	/**
	 * 登录
	 *
	 * @param user
	 */
	public void login(final User user) {
		new Thread() {
			@Override
			public void run() {
				final String res = userMode.login(user);
				UIHandler.get().post(new Runnable() {
					@Override
					public void run() {
						if ("true".equals(res)) {
							userView.onLoginSuccess();
						} else {
							userView.onLoginFailed(res);
						}
					}
				});
			}
		}.start();
	}
}
