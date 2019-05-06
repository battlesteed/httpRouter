package steed.router.test.api.processor;

import org.junit.Assert;

import steed.ext.util.base.StringUtil;
import steed.hibernatemaster.domain.BaseRelationalDatabaseDomain;
import steed.hibernatemaster.util.DaoUtil;
import steed.router.domain.Message;
import steed.router.processor.ModelDrivenProcessor;

public class BaseApiTestProcessor<T extends BaseRelationalDatabaseDomain> extends ModelDrivenProcessor<T>{
	private String sign;
	public String getSign() {
		return sign;
	}
	public void setSign(String sign) {
		this.sign = sign;
	}

	@Override
	public void afterAction(String methodName) {
		assertNotEmpty(sign);
		super.afterAction(methodName);
		String operation = null;
		switch (methodName) {
		case "save":
			operation = "添加";
			break;
		case "delete":
			operation = "删除";
			break;
		case "update":
			operation = "修改";
			break;
		}
		if (operation != null) {
			writeJson(new Message(operation+"成功!"));
		}
	}
	
	protected void assertNotEmpty(String value) {
		Assert.assertTrue(!StringUtil.isStringEmpty(value));
	}
	
}
 