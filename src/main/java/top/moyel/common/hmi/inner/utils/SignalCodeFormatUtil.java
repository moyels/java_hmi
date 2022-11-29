package top.moyel.common.hmi.inner.utils;

import cn.hutool.core.util.StrUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author moyel
 */
public class SignalCodeFormatUtil {
    private static final String SINGLE_PARAM_PATTERN_STR = "\\w+";
    private static final Pattern SINGLE_PARAM_PATTERN = Pattern.compile(SINGLE_PARAM_PATTERN_STR);

    private static final String MULTI_VAL_DELIMITER = ",";

    private static final Map<String, Boolean> SINGLE_SIGNAL_CODE_VALID_MAP = new HashMap<>();
    private static final Map<String, Boolean> MULTI_SIGNAL_CODE_VALID_MAP = new HashMap<>();
    private static final Map<String, String[]> MULTI_SIGNAL_CODE_VAL_MAP = new HashMap<>();

    public static boolean singleValid(String str) {
        if (StrUtil.isBlank(str)) {
            return false;
        }

        String key = str.trim();

        if (!SINGLE_SIGNAL_CODE_VALID_MAP.containsKey(key)) {
            SINGLE_SIGNAL_CODE_VALID_MAP.put(key, SINGLE_PARAM_PATTERN.matcher(key).matches());
        }

        return SINGLE_SIGNAL_CODE_VALID_MAP.get(key);
    }

    public static boolean multiValid(String str) {
        if (StrUtil.isBlank(str)) {
            return false;
        }

        String key = str.trim();

        if (!MULTI_SIGNAL_CODE_VALID_MAP.containsKey(key)) {
            MULTI_SIGNAL_CODE_VALID_MAP.put(key, multiValidReal(key));
        }

        return MULTI_SIGNAL_CODE_VALID_MAP.get(key);
    }

    private static boolean multiValidReal(String key) {
        String[] keys = key.split(MULTI_VAL_DELIMITER);

        for (String item : keys) {
            if (!singleValid(item)) {
                return false;
            }
        }

        MULTI_SIGNAL_CODE_VAL_MAP.put(key, keys);
        return true;
    }

    public static boolean getMultiValid(String str) {
        if (StrUtil.isBlank(str)) {
            return false;
        }

        return MULTI_SIGNAL_CODE_VALID_MAP.getOrDefault(str.trim(), false);
    }

    public static String[] getMultiRes(String str) {
        if (getMultiValid(str)) {
            return MULTI_SIGNAL_CODE_VAL_MAP.get(str.trim());
        }

        return null;
    }
}
