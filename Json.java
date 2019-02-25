package JSON;

import java.io.FileWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.Json.Name;
import com.Json.isList;

public class Json {
	
	
	public static String toJson(Object o) throws NoSuchFieldException {
		Class<? extends Object> clazz = o.getClass();
		StringBuilder s = new StringBuilder();

		if (o instanceof List) {
			try {
//				System.out.println("------------");
				s.append(tolistJson((List) o));
			} catch (SecurityException | NoSuchMethodException e) {
				e.printStackTrace();
			}
		} else if (o instanceof HashMap) {
			s.append(toMapJson((HashMap) o));
		} else if (o instanceof String) {
			s.append(toStr(o));
		} else if (o instanceof Integer) {
			s.append(o);
		} else {
			// 看重构的方法
			handleObject(s, clazz, o);

			// 处理json格式
			if (s.toString().endsWith(",")) {
				s.deleteCharAt(s.length() - 1);
			}
			s.append("}");
		}
		// 写入Json文件
		try (FileWriter out = new FileWriter("person01.json")) {
			out.write(s.toString());
		} catch (Exception e) {
			// TODO: handle exception
		}
		// System.out.println(s.toString());
		return s.toString();

	}

	/**
	 * 传入字符串
	 * 
	 * @param o
	 * @return
	 */
	public static Object toStr(Object o) {
		StringBuilder s = new StringBuilder("\"");
		s.append(o);
		s.append("\"");
		return s.toString();
	}

	/**
	 * 重构出来的方法，为了减少代码的重复
	 * 遍历方法，得到字段，再调用方法，获取字段的值，再处理格式，
	 * @param s
	 * @param clazz
	 * @param o
	 * @throws NoSuchFieldException
	 */
	private static void handleObject(StringBuilder s, Class clazz, Object o) throws NoSuchFieldException {
		s.append("{");

		Method[] ms = clazz.getDeclaredMethods();
		// 遍历对象里的方法
		for (Method m : ms) {
//	    		System.out.println(m.getName());
			// 获取get方法
			if (m.getName().startsWith("get")) {
				// 获取键--K
				String key = m.getName().substring(3).toLowerCase();

				Object value = null;
				try {
					// 获取值--V
					//对象调用m方法，获取值
					value = m.invoke(o);
					if (value instanceof List) {
//						System.out.println("------");
						s.append("\"list\":");
						s.append(tolistJson((List) value));
						// 删除第一个字符，确保是json格式
//						s.deleteCharAt(0);
					} else {

						// 获取键对应的属性
						Field field = clazz.getDeclaredField(key);
						// 获取属性的注解
						Annotation[] ans = field.getDeclaredAnnotations();
						Annotation b = null;
						for (Annotation a : ans) {
							// 处理属性有注解的其它类的对象
							if (a != null) {
								s.append(String.format("\"%s\":%s,", key, toJson(value)));
								b = a;
								break;
							}
						}

						// 处理属性没注解的
						if (b == null) {
							if (value instanceof String) {
								s.append(String.format("\"%s\":\"%s\",", key, value));
							} else {
								s.append(String.format("\"%s\":%s,", key, value));
							}

						}
					}
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
						| SecurityException | NoSuchMethodException e) {

					e.printStackTrace();
				}

			}

		}
	}

	/**
	 * 传入map
	 * 
	 * @param map
	 * @return
	 * @throws NoSuchFieldException
	 */
	private static Object toMapJson(HashMap<Object, Object> map) throws NoSuchFieldException {
		StringBuilder s = new StringBuilder("{");
		Class<? extends Set> class1 = map.keySet().getClass();
//		System.out.println(class1.getName());
		for (Object o : map.keySet()) {
//			Class<? extends Object> clazz = o.getClass();
//			System.out.println("----");
//			System.out.println(o.getClass().getName());
			s.append(String.format("\"%s\":%s,", o, toJson(map.get(o))));
//			System.out.println(map.get(o));
		}
		s.append("}");
		s.deleteCharAt(s.length() - 2);
		return s.toString();
	}

	/**
	 * 传入List列表--并且可以处理类中含有其他类的实例的情况--通过注解的方式
	 * 
	 * @param list
	 * @return
	 * @throws NoSuchFieldException
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 */
	private static String tolistJson(List<?> list)
			throws NoSuchFieldException, SecurityException, NoSuchMethodException {
		StringBuilder s = new StringBuilder("[");

		// 遍历列表里的对象
		for (Object o : list) {
			Class<? extends Object> clazz = o.getClass();
			handleObject(s, clazz, o);
			// 处理Json的格式问题
			if (s.toString().endsWith(",")) {
				s.deleteCharAt(s.length() - 1);
			}
			s.append("},");
		}
		if (s.toString().endsWith(",")) {
			s.deleteCharAt(s.length() - 1);
		}
		s.append("]");

		// 写入Json文件
		try (FileWriter out = new FileWriter("person02.json")) {
			out.write(s.toString());
		} catch (Exception e) {
			// TODO: handle exception
		}
		return s.toString();
	}

	/**
	 * 传入对象--可以处理类中含有其他类的实例的情况--通过注解的方式
	 * 
	 * @param o
	 * @return
	 * @throws NoSuchFieldException
	 */
	private static String tooJson(Object o) throws NoSuchFieldException {

		StringBuilder s = new StringBuilder("{");

		Class clazz = o.getClass();
		Method[] ms = clazz.getDeclaredMethods();
		for (Method m : ms) {
			if (m.getName().startsWith("get")) {
				String name = m.getName();
				Class c = m.getReturnType();

				String key = name.substring(3).toLowerCase();

				Object value = null;
				try {
					value = m.invoke(o);
					Field field = clazz.getDeclaredField(key);
					Annotation[] ans = field.getDeclaredAnnotations();
					Annotation b = null;
					for (Annotation a : ans) {
						if (a.equals("name")) {
							s.append(String.format("\"%s\":%s,", key, tooJson(value)));
							b = a;
							break;

						}
						if (a.equals("isMap")) {
							s.append(String.format("\"%s\":%s,", key, tooJson(value)));
							b = a;
							break;
						}
					}

					if (b == null) {
						if (value instanceof String) {
							s.append(String.format("\"%s\":\"%s\",", key, value));
						} else {
							s.append(String.format("\"%s\":%s,", key, value));
						}

					}

				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					e.printStackTrace();
				} catch (SecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		if (s.toString().endsWith(",")) {
			s.deleteCharAt(s.length() - 1);
		}
		s.append("}");
		// System.out.println(s.toString());
		return s.toString();
	}

	/**
	 * 反序列化
	 * 将Json字符串中的内容提取出来，放到一个新的对象中，再返回出来
	 * 通过字段，获取set方法的名字，
	 * @param class1
	 * @param string
	 * @return
	 * @throws ClassNotFoundException
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 */
	public static Object fromJson(Class clazz, String json, int n)
			throws ClassNotFoundException, NoSuchMethodException, SecurityException {
		Object o = null;
//		System.out.println(clazz.getName());
		if (o instanceof HashMap) {
			o = clazz.getDeclaredConstructor();
		} else if (clazz.getSimpleName().equals("List")) {
			o = new ArrayList<>();
//			System.out.println("-------");
		} else {
//			System.out.println("ssssssssssssssssssssssssssssssssssssssssssssss");
			try {
				//创建一个对象
				Constructor c = clazz.getDeclaredConstructor();

				o = c.newInstance();

				Object value;
				Field[] fs = clazz.getDeclaredFields();
				for (Field f : fs) {
					String key = f.getName();
//					 System.out.println("ppp:"+key);

					String name2 = key.substring(0, 1).toUpperCase() + key.substring(1);
//				 System.out.println(f.getType());
					Method m = clazz.getMethod("set" + name2, f.getType());
//				if(json.contains("[")) {
//					String s = listPatt(json);
//					s.replaceAll("\"", "");
//				}
					// System.out.println(m.getName());
//				Annotation[] ans = f.getAnnotations();
//				Annotation b = null;
//				for (Annotation a : ans) {
//					System.out.println(a);
					if (f.isAnnotationPresent(isList.class)) {

					}

					else if (f.isAnnotationPresent(Name.class)) {
//						System.out.println("Address:\t" + f.getName());
//						value = fromJson(f.getClass(), );
//						System.out.println("addrss匹配字符串：" + ObPatt(json, n));
						Class<?> perClazz = Class.forName(f.getType().getName());
//						System.out.println(f.getType());
//						System.out.println("获取类名：" + perClazz.getName());
						value = fromJson(perClazz, ObPatt(json, n));
//						b = a;
						//对象调用m方法给自己赋值
						m.invoke(o, value);
						break;

					} else {
//				}
//				if (b == null) {
						value = patt(key, json, f.getType(), n);
//						System.out.println(value);
						if (value != null) {
							m.invoke(o, value);
//					}
						}
					}
				}

			} catch (NoSuchMethodException | SecurityException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
		return o;
	}

	/**
	 * 反序列化---HashMap
	 * @param map
	 * @param clazz01
	 * @param clazz02
	 * @param json
	 * @return
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	public static <clazz01, clazz02> Object fromJson(Object map, Class clazz01, Class clazz02, String json)
			throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {
//		System.out.println(clazz02);

//		Object[] a = { String.class, Integer.class, long.class };
//		String[] b = { "String", "Integer", "long" };
//		System.out.println(clazz01.getTypeName());
//		for (int i = 0; i < a.length; i++) {
//			if (clazz01.equals(a[i])) {/
//				if (i == 0) {
					HashMap<Object, clazz02> newMap = new HashMap<>();
					createMap(clazz02, json, newMap);
					return newMap;
//				} else if (i == 1) {
//					HashMap<Integer, clazz02> newMap = new HashMap<>();
//// 					createMap(clazz02, json, newMap);
//				}
//			}
//		}
		

	}

	private static <clazz02> void createMap(Class clazz02, String json, HashMap<Object, clazz02> newMap)
			throws NoSuchMethodException {
		try {
			Object p;
			String[] f = { "a", "b", "c" };
			for (int i = 0; i < f.length; i++) {
				p = fromJson(clazz02, json, i);
				newMap.put(f[i], (clazz02) p);
//				System.out.println("p de :" + p);
			}
			for (Object b : newMap.keySet()) {
//				System.out.println("----" + newMap);
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 可以根据变量--键 匹配 --值
	 * 
	 * @param name
	 * @param json
	 * @return
	 */
	private static Object patt(String name, String json, Class c, int n) {
		// (?<=\"name\"(:\"|:))[^(\"|,)]*---获取键--name后面的值---Alice
		// (?<=\""+ name+ "\"(:\"|:))[^(\"|,)]*---拆分之后，可以改变键从而获取不同的值
		String regex = "(?<=\"" + name + "\"(:\"|:))[^(\"|,|})]*";
		Matcher mather = Pattern.compile(regex).matcher(json);
		String sss = null;
		List<Object> str = new ArrayList<>();
		//查找与该模式匹配的字符串
		while (mather.find()) {
			//返回此字符串
			sss = mather.group();
			if (!c.equals(String.class)) {
				str.add(Integer.parseInt(sss));
			} else {
				str.add(sss);
			}
//			System.out.println("~~~~~~~~~~" + sss);
		}
//		System.out.println(str.size());
//		System.out.println("#########" + str.get(n));
		if (c.equals(String.class)) {
			return str.get(2 * n + 1);
		} else {
			return str.get(n);
		}

	}

	/**
	 * 用正则表达式匹配出address--嵌套的
	 * 
	 * @param json
	 * @return
	 */
	private static String ObPatt(String json, int n) {

		String regex = "(?<=\\:\\{)[^}]*";
		Matcher mather = Pattern.compile(regex).matcher(json);
		String sss = null;
		int t = 0;
		while (mather.find()) {
			// System.out.println(mather.group().toString());
			sss = mather.group();
			if (t == n) {
				break;
			}
			t++;
//			System.out.println("11111" + sss);
		}

		return sss;
	}

	/**
	 * 截取出列表里的所有内容
	 * 
	 * @param json
	 * @return
	 */
	private static String listPatt(String json) {

		String regex = "(?<=\\[)[^\\]]*";

		Matcher mather = Pattern.compile(regex).matcher(json);
		String sss = null;
		while (mather.find()) {
			// System.out.println(mather.group().toString());
			sss = mather.group();
		}

		return sss;
	}

	private static String onetoOnePatt(String json) {
		String regex = "(?<=\\[)(\\S+)(?=\\,\\{)";

		Matcher mather = Pattern.compile(regex).matcher(json);
		String sss = null;
		while (mather.find()) {

			sss = mather.group();
		}

		return sss;

	}

	private static String mapPatt(String json) {

//		String regex = "(?<=(\\{\")|(},\"))[^\"\\:\\{]*";
		String regex = "((?<=\").+?(?=\":\\{))|((?<=\").+?(?=\":))";

		Matcher mather = Pattern.compile(regex).matcher(json);
		String sss = "ss";
		while (mather.find()) {

			sss = mather.group();
//			System.out.println(sss);
		}
//		System.out.println(sss);
		return sss;

	}

	public static Object fromJson(Class clazz, String json)
			throws ClassNotFoundException, NoSuchMethodException, SecurityException {
		Object o = null;
//		System.out.println(clazz.getName());
		if (o instanceof HashMap) {
			o = clazz.getDeclaredConstructor();
		} else if (clazz.getSimpleName().equals("List")) {
			o = new ArrayList<>();
//			System.out.println("-------");
		} else {
			try {
				Constructor c = clazz.getDeclaredConstructor();

				o = c.newInstance();

				Object value;
				Field[] fs = clazz.getDeclaredFields();
				for (Field f : fs) {
					String key = f.getName();
//					 System.out.println("ppp:"+key);
                    //将第一个字母大写--因为要用get和set方法
					String name2 = key.substring(0, 1).toUpperCase() + key.substring(1);
//				 System.out.println(f.getType());
					//通过方法的名字，得到并调用方法
					Method m = clazz.getMethod("set" + name2, f.getType());

					if (f.isAnnotationPresent(isList.class)) {

					}

					else if (f.isAnnotationPresent(Name.class)) {
//						System.out.println("Address:\t" + f.getName());
//						value = fromJson(f.getClass(), );
//						System.out.println("addrss匹配字符串：" + ObPatt(json));
						//通过全类名得到反射入口
						Class<?> perClazz = Class.forName(f.getType().getName());
//						System.out.println(f.getType());
//						System.out.println("获取类名：" + perClazz.getName());
						value = fromJson(perClazz, ObPatt(json));
//						b = a;
						m.invoke(o, value);
						break;

					} else {
//				}
//				if (b == null) {
						value = patt(key, json, f.getType());
//						System.out.println(value);
						if (value != null) {
							m.invoke(o, value);
//					}
						}
					}
				}

			} catch (NoSuchMethodException | SecurityException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
		return o;
	}

	private static Object patt(String name, String json, Class c) {
		// (?<=\"name\"(:\"|:))[^(\"|,)]*---获取键--name后面的值---Alice
		// (?<=\""+ name+ "\"(:\"|:))[^(\"|,)]*---拆分之后，可以改变键从而获取不同的值
		String regex = "(?<=\"" + name + "\"(:\"|:))[^(\"|,|})]*";
		Matcher mather = Pattern.compile(regex).matcher(json);
		String sss = null;
		int t = 0;
		int j = 0;
		if (c.equals(String.class)) {
			while (mather.find()) {
				// System.out.println(mather.group().toString());
				sss = mather.group();

//				System.out.println("++++" + sss);

			}
		} else {
			while (mather.find()) {
				// System.out.println(mather.group().toString());
				sss = mather.group();

//				System.out.println("++++" + sss);

			}
			return Integer.parseInt(sss);
		}
		// if(Pattern.compile("[0-9]*").matcher(sss).matches()) {

		return sss;
	}

	/**
	 * 用正则表达式匹配出address--嵌套的
	 * 
	 * @param json
	 * @return
	 */
	private static String ObPatt(String json) {

		String regex = "(?<=\\:\\{)[^}]*";
		Matcher mather = Pattern.compile(regex).matcher(json);
		String sss = null;
		int t = 0;
		while (mather.find()) {
			// System.out.println(mather.group().toString());
			sss = mather.group();

//			System.out.println("11111" + sss);
		}

		return sss;
	}
}
