package steed.router.exception.message;

import steed.router.domain.Message;

public interface MessageExceptionInterface {
	public Message getMsg();
	public void setMsg(Message msg);
}
