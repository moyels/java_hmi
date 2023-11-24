package top.moyel.common.hmi.fetch;

import top.moyel.common.hmi.base.BaseExpFetch;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author moyel
 */
public class KeyExpFetch extends BaseExpFetch {
    public static final Pattern PATTERN = Pattern.compile("([a-z][A-Z][0-9]_)+");

    @Override
    public boolean support(String exp) {
        return PATTERN.matcher(exp).matches();
    }

    @Override
    public Object fetch(String exp, Map<String, ?> param) {
        return param.getOrDefault(exp, null);
    }
}
