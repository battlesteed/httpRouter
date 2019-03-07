package steed.router.processor;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import steed.hibernatemaster.domain.BaseDatabaseDomain;
import steed.hibernatemaster.domain.BaseDomain;
import steed.hibernatemaster.domain.Page;
import steed.hibernatemaster.util.DaoUtil;
import steed.router.HttpRouter;
import steed.util.base.DomainUtil;
import steed.util.base.StringUtil;
import steed.util.reflect.ReflectUtil;
/**
 * 处理器,处理HttpRouter分发过来的http请求<br>
 * 若Processor中的方法返回String,HttpRouter会forward到String对应的jsp页面(若string以.jsp结尾)或直接把string内容返回给客户端,若返回其它类型的对象,则会将对象转成json写到response,
 * 若方法返回null或方法返回值为void则HttpRouter不做任何处理<br><br>
 *  注意:  该类的所有public方法和字段均会被http请求访问到,请注意安全.<b>不要乱用public修饰符!</b>
 *  
 * @author battlesteed
 * @see #steed_forward
 *
 */
public class BaseAdminProcessor<SteedDomain extends BaseDatabaseDomain> extends ModelDrivenProcessor<SteedDomain> {
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
	
	public int getCurrentPage() {
		return currentPage;
	}
	
	public void setCurrentPage(int currentPage) {
		this.currentPage = currentPage;
	}
	/**
	 * 往request中放东西
	 * @param key 键
	 * @param obj 值
	 */
	protected void setRequestAttribute(String key,Object obj){
		getRequest().setAttribute(key, obj);
	}
	/**
	 * 从request中取东西
	 * @param key 键
	 */
	protected Object getRequestAttribute(String key){
		return getRequest().getAttribute(key);
	}
	/**
	 * 获取request中的参数
	 * @param key 键
	 */
	protected String getRequestParameter(String key){
		return getRequest().getParameter(key);
	}
	/**
	 * 获取request中的参数
	 * @param key 键
	 */
	protected boolean isRequestParameterEmpty(String key){
		return StringUtil.isStringEmpty(getRequest().getParameter(key));
	}
	protected String[] getRequestParameters(String key){
		return getRequest().getParameterValues(key);
	}
	/**
	 * 往session中放东西
	 * @param key 键
	 * @param obj 值
	 */
	protected void setSessionAttribute(String key,Object obj){
		getSession().setAttribute(key, obj);
	}
	
	public ServletContext getServletContext() {
		//艹，兼容Servlet2.5只能这样写
		return getRequest().getSession().getServletContext();
	}
	
	public HttpServletResponse getResponse() {
		return HttpRouter.getResponse();
	}
	public HttpServletRequest getRequest() {
		return HttpRouter.getRequest();
	}
	
	protected void afterDomainCreate(SteedDomain domain){}
	
	public HttpSession getSession() {
		return getRequest().getSession();
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
	 * 自动把model作为查询条件查询数据库
		（关于如何定义实体类并用model作为查询条件请看cn.com.beyondstar.domain.people.People）
		并把结果集放到steed.domain.application.Page中并把page
		以"page"为key放到request域中
		你要做的只是从jsp页面读取page中的结果集并显示出来而已
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
	 * @return null
	 */
	public String delete(){
		BaseDatabaseDomain model = (BaseDatabaseDomain) getModel();
		model.delete();
		return null;
	}
	
	/**
	 * 把model保存到数据库，
	 * 并以json格式返回保存状态（成功与否）到前端,
	 * @return null
	 */
	public String save(){
		saveDomain();
		return null;
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
	
	
}
