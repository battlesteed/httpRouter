package steed.router.api;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Random;

import steed.ext.util.reflect.ReflectUtil;

public class RandomField {
	private Random random = new Random();
	
	public Random getRandom() {
		return random;
	}

	public void setRandom(Random random) {
		this.random = random;
	}

	/**
	 * 给为空的字段注入随机值
	 */
	public void injectRandomField(Object target) {
		List<Field> allFields = ReflectUtil.getAllFields(target);
		allFields.forEach((field)->{
			boolean classBaseData = ReflectUtil.isClassBaseData(field.getType());
			if (classBaseData) {
				field.setAccessible(true);
				/*
				 * try { field.set(target, getRandomValue(field.getType())); } catch
				 * (IllegalArgumentException | IllegalAccessException e) { e.printStackTrace();
				 * }
				 */
			}
		});
	}

	/*
	 * private Object getRandomValue(Class<?> type) { clazz == Integer.class ||
	 * clazz == int.class || clazz == Float.class || clazz == float.class || clazz
	 * == Boolean.class || clazz == boolean.class || clazz == Character.class ||
	 * clazz == char.class || clazz == Double.class || clazz == double.class ||
	 * clazz == Long.class || clazz == long.class; if (type == Integer.class || type
	 * == int.class) { return random.nextInt(); } return null; }
	 */
}
