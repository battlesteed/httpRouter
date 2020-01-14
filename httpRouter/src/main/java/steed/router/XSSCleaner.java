package steed.router;

public interface XSSCleaner {
	public String[] clean(String[] param,String name);

	String cleanXss(String str);
}
