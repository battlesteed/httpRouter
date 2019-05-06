package steed.router.processor;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import steed.ext.util.base.DomainUtil;
import steed.ext.util.base.StringUtil;
import steed.ext.util.reflect.ReflectUtil;
import steed.hibernatemaster.domain.BaseDatabaseDomain;
import steed.hibernatemaster.domain.BaseDomain;
import steed.hibernatemaster.domain.Page;
import steed.hibernatemaster.util.DaoUtil;
import steed.router.domain.Message;

public class BaseCRUDProcessor<SteedDomain extends BaseDatabaseDomain> extends ModelDrivenProcessor<SteedDomain> {
	private static final long serialVersionUID = 7774350640186420795L;
	
	/**
	 * 默认分页大小
	 */
	public static final int defaultPageSize = 10;
	
	/**
	 * 当前分页页码,从1开始,没有0
	 */
	protected int currentPage = 1;
	
	/**
	 * 分页大小
	 */
	protected int pageSize = defaultPageSize;
	
	/**
	 * 操作名称,"修改","删除","添加"等,若事务提交成功,则会自动返回 operationName + "成功!"给客户端,否则返回 operationName + "失败!"
	 */
	protected String operationName;
	
	public void setCurrentPage(int currentPage) {
		this.currentPage = currentPage;
	}
	
	/**
	 * 往request中放page，key是page
	 * @param page
	 */
	protected void setRequestPage(Page page){
		setRequestAttribute("page", page);
	}
	
	protected void setRequestDomainList(List list){
		setRequestAttribute("domainList", list);
	}
	
	protected void setRequestDomain(Object obj){
		setRequestAttribute("domain", obj);
	}
	
	@SuppressWarnings("unchecked")
	protected Page<SteedDomain> getRequestPage(){
		Object requestAttribute = getRequestAttribute("page");
		if (requestAttribute == null) {
			Page<SteedDomain> page = new Page<SteedDomain>(); 
			page.setCurrentPage(currentPage);
			page.setPageSize(pageSize);
			setRequestAttribute("page", page);
			return page;
		}
		return (Page<SteedDomain>) requestAttribute;
	}

	/**
	 * 
	 * 
	 * 
	 * @return steed_forward
	 */
	public String index(){
		return index(null, null);
	}
	
	public String index(List<String> desc,List<String> asc){
		BaseDomain model = getModel();
		DomainUtil.fuzzyQueryInitialize(model);
		setRequestPage(DaoUtil.listObj(getModel().getClass(),pageSize,currentPage, getModelQueryMapByRequestParam(),desc,asc));
		return steed_forward;
	}
	
	/**
	 * 将传过来的request参数根据model字段转换成的map(如果参数中有查询字段,但是model没有,返回的map一样会带有查询字段),
	 * 解决早期框架添加大于,小于这样的查询操作符需要在实体类加字段的尴尬情况
	 * @return
	 */
	protected Map<String, Object> getRequetParamQueryMap(BaseDomain model,String paramPrefixName){
		Map<String, Object> putField2Map = DaoUtil.putField2Map(model);
		Enumeration<String> parameterNames = getRequest().getParameterNames();
		while(parameterNames.hasMoreElements()){
			String nextElement = parameterNames.nextElement().replace(paramPrefixName, "");
			int selectIndex = DaoUtil.isSelectIndex(nextElement);
			if (!putField2Map.containsKey(nextElement) && selectIndex > 0) {
				String requestParameter = getRequestParameter(nextElement);
				if (StringUtil.isStringEmpty(requestParameter)) {
					continue;
				}
				String fileName = nextElement.substring(0, nextElement.length()-selectIndex);
				String subName = nextElement.substring(nextElement.length()-selectIndex);
				try {
					Field declaredField = ReflectUtil.getDeclaredField(model.getClass(), fileName);
					//前端乱传字段名,日了哮天犬
					if (declaredField == null) {
						continue;
					}
					Class<?> type = declaredField.getType();
					if ("_not_null".equals(subName)) {
						type = Boolean.class;
					}
					Serializable convertFromString = ReflectUtil.convertFromString(type, requestParameter);
					if (convertFromString == null) {
						continue;
					}
					putField2Map.put(nextElement, convertFromString);
				} catch (SecurityException e) {
					e.printStackTrace();
				} 
			}
		}
		return putField2Map;
	}
	
	protected Map<String, Object> getModelQueryMapByRequestParam(){
		return getRequetParamQueryMap(getModel(), "");
	}
	
 
	protected String add(){
		return steed_forward;
	}
	protected String lookOver(){
		getDomainAndSet2Request();
		return steed_forward;
	}
	/**
	 * 以model字段名做为key，把Daoutil.get(modelID)获得的的实体类放到request域
	 */
	protected void getDomainAndSet2Request() {
		setDomain2Request(getModelFromDatabase());
	}
	
	protected SteedDomain getModelFromDatabase() {
		return getModel().smartLoad();
	}
	
	/**
	 * 把查询到的domain以domain为key放到request域，常用于跳转到edit页面时
	 * @param domain
	 */
	protected void setDomain2Request(BaseDomain domain){
		setRequestAttribute("domain", domain);
	}
	/**
	 * 获取request域中的domain
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected SteedDomain getRequestDomain(){
		return (SteedDomain) getRequestAttribute("domain");
	}
	
	/**
	 * 通用delete方法
	 * 
	 * 	删除model对应的表中主键为DomainUtil.getDomainId(model)的记录
	 * 	并以json格式返回删除状态（成功与否）到前端，
	 *
	 */
	public void delete(){
		BaseDatabaseDomain model = (BaseDatabaseDomain) getModel();
		model.delete();
	}
	
	/**
	 * 把model保存到数据库，
	 * 并以json格式返回保存状态（成功与否）到前端,
	 */
	public void save(){
		 saveDomain();
	}
	
	
	protected boolean saveDomain() {
		BaseDatabaseDomain model = getModel();
		return model.save();
	}

	/**
	 * 	查询model对应的表中主键为DomainUtil.getDomainId(model)的记录
	 * 	并以"domain"为key保存到request域，
	 *	你只需在jsp页面读取domain中的数据并显示出来让用户编辑即可
	 * @return steed_forward
	 */
	public String edit(){
		getDomainAndSet2Request();
		return steed_forward;
	}
	/**
	 * 将model更新到数据库，并以json格式返回更新状态（成功与否）到前端
	 * 你可能需要steed.action.BaseAction.updateNotNullField()
	 * 
	 * @see #updateNotNullField
	 */
	public String update(){
		((BaseDatabaseDomain)getModel()).update();
		return null;
	}
	/**
	 * 将model不为空的字段更新到数据库，并以json格式返回更新状态（成功与否）到前端
	 * 你可能需要steed.action.BaseAction.update()
	 * @return null
	 */
	public String updateNotNullField(){
		return updateNotNullField(null);
	}
	
	protected String updateNotNullField(List<String> updateEvenNull){
		updateNotNullField(updateEvenNull, true);
		return null;
	}
	
	protected String updateNotNullField(List<String> updateEvenNull,boolean strictlyMode){
		BaseDatabaseDomain model = (BaseDatabaseDomain) getModel();
		model.updateNotNullField(updateEvenNull,strictlyMode);
		return null;
	}
	
	@Override
	public void afterAction(String methodName) {
		super.afterAction(methodName);
		String operation = getOperationName(methodName);
		if (operation != null) {
			boolean managTransaction = DaoUtil.managTransaction();
			if (managTransaction) {
				writeJson(new Message(operation+"成功!"));
			}else {
				writeJson(new Message(Message.statusCode_UnknownError,operation+"失败"));
			}
		}
		
	}
	
	protected String getOperationName(String methodName) {
		if (!StringUtil.isStringEmpty(operationName)) {
			return operationName;
		}
		switch (methodName) {
		case "save":
			return "添加";
		case "delete":
			return "删除";
		case "update":
			return "修改";
		}
		return "";
	}
	
	
}
