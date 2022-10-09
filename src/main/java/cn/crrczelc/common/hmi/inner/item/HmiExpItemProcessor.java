package cn.crrczelc.common.hmi.inner.item;

import cn.crrczelc.common.hmi.inner.entity.InnerHmiProcessorItemBean;
import cn.crrczelc.common.hmi.processor.BaseHmiItemProcessor;
import cn.crrczelc.common.hmi.utils.CatchUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author moyel
 */
public class HmiExpItemProcessor extends BaseHmiItemProcessor<InnerHmiProcessorItemBean> {
    private static final String OR_COND_DELIMITER = "||";
    private static final String AND_COND_DELIMITER = "&&";

    private static final String EXP_ITEM_PATTERN_STR = "(\\w+)([!=]=(\\w+))?";
    private static final Pattern EXP_ITEM_PATTERN = Pattern.compile(EXP_ITEM_PATTERN_STR);

    private static final String EMPTY_REGEX = "\\s";

    private static final Map<String, Boolean> signalCodeValidity = new HashMap<>();

    @Override
    protected boolean check(InnerHmiProcessorItemBean inParam) {
        String signalCode = CatchUtil.catchExcept(() -> inParam.getHmiDetail().getSignalCode());
        String mappingValue = CatchUtil.catchExcept(() -> inParam.getHmiDetail().getMappingValue());
        if (StrUtil.isBlank(signalCode) || StrUtil.isBlank(mappingValue)) {
            return false;
        }

        String signalCodeCompressed = signalCode.replaceAll(EMPTY_REGEX, "");

        if (!signalCodeValidity.containsKey(signalCodeCompressed)) {
            String[] exps = signalCodeCompressed.split(OR_COND_DELIMITER);

            signalCodeValidity.put(signalCodeCompressed, !(ArrayUtil.isEmpty(exps) || !checkExps(exps)));
        }

        return signalCodeValidity.get(signalCodeCompressed);
    }

    private boolean checkExps(String[] exps) {
        for (String exp : exps) {
            if (exp.contains(AND_COND_DELIMITER)) {
                for (String childExp : exp.split(AND_COND_DELIMITER)) {
                    if (!EXP_ITEM_PATTERN.matcher(childExp).matches()) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    @Override
    protected Boolean process(InnerHmiProcessorItemBean inParam) {
        String signalCode = inParam.getHmiDetail().getSignalCode();
        String signalCodeCompressed = signalCode.replaceAll(EMPTY_REGEX, StrUtil.EMPTY);

        if (!signalCodeValidity.getOrDefault(signalCodeCompressed, false)) {
            return false;
        }

        return calcExpRes(signalCodeCompressed, inParam.getParamMap());
    }

    private Boolean calcExpRes(String exp, Map<String, Object> paramMap) {
        if (StrUtil.isBlank(exp) || CollectionUtil.isEmpty(paramMap)) {
            return false;
        }

        String[] exps = exp.split(OR_COND_DELIMITER);
        return calcExpRes(exps, paramMap, false);
    }

    private Boolean calcExpRes(String[] exps, Map<String, Object> paramMap, Boolean and) {
        for (String exp : exps) {
            Boolean res = false;
            if (exp.contains(AND_COND_DELIMITER)) {
                res = calcExpRes(exp.split(AND_COND_DELIMITER), paramMap, true);
            } else {
                Matcher matcher = EXP_ITEM_PATTERN.matcher(exp);

                String left = CatchUtil.catchExcept(() -> matcher.group(1));
                String middle = CatchUtil.catchExcept(() -> matcher.group(2));
                String right = CatchUtil.catchExcept(() -> matcher.group(3));

                String value = paramMap.getOrDefault(left, "").toString();
                if (Objects.isNull(right)) {
                    // 如果 right 部分为空，则表示不存在右边，仅依靠 param 的字面值判断是否符合条件
                    res = BooleanUtil.toBoolean(value);
                } else {
                    // 当right不为空时，可以断言，middle不为空，为了
                    if (Objects.nonNull(middle) && StrUtil.isNotBlank(middle) && middle.startsWith("!")) {
                        res = !StrUtil.equals(value, right);
                    } else {
                        res = StrUtil.equals(value, right);
                    }
                }
            }

            // 如果 或 逻辑中出现 res 为true的情况，直接返回true
            if (!and && res) {
                return true;
            }

            // 如果 与 逻辑中出现 res 为false的情况，直接返回false
            if (and && !res) {
                return false;
            }
        }

        // 如果没有提前返回，则与逻辑返回true，或逻辑返回false
        return and;
    }
}
