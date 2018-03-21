package cache;

import org.apache.commons.lang.ArrayUtils;

public class CacheKeyGenerator {

	public static final String get(CacheVisitor visitor, String... keys) {
		if (ArrayUtils.isEmpty(keys)) {
			return null;
		}

		StringBuilder sb = new StringBuilder('%' + visitor.prefixKey());
		for (String key : keys) {
			sb.append('%');
			sb.append(key);
		}
		return sb.toString();
	}
}
