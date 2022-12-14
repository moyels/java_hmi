package top.moyel.common.hmi.inner.item;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import top.moyel.common.hmi.inner.entity.InnerHmiProcessorItemBean;
import top.moyel.common.hmi.processor.BaseHmiItemProcessor;
import top.moyel.common.hmi.utils.CatchUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author moyel
 */
public class HmiExpItemProcessor extends BaseHmiItemProcessor<InnerHmiProcessorItemBean> {
    private static final String OR_COND_DELIMITER = "\\|\\|";
    private static final String AND_COND_DELIMITER = "&&";

    private static final String EXP_ITEM_PATTERN_STR = "(\\w+)([!=]=(.+))?";
    private static final Pattern EXP_ITEM_PATTERN = Pattern.compile(EXP_ITEM_PATTERN_STR);

    private static final String EMPTY_REGEX = "\\s";

    private static final Map<String, Boolean> STRING_BOOLEAN_HASH_MAP = new HashMap<>();

    @Override
    protected boolean check(InnerHmiProcessorItemBean inParam) {
        String signalCode = CatchUtil.catchExcept(() -> inParam.getHmiDetail().getSignalCode());
        String mappingValue = CatchUtil.catchExcept(() -> inParam.getHmiDetail().getMappingValue());
        if (StrUtil.isBlank(signalCode) || StrUtil.isBlank(mappingValue)) {
            return false;
        }

        String signalCodeCompressed = signalCode.replaceAll(EMPTY_REGEX, "");

        if (!STRING_BOOLEAN_HASH_MAP.containsKey(signalCodeCompressed)) {
            String[] exps = signalCodeCompressed.split(OR_COND_DELIMITER);

            STRING_BOOLEAN_HASH_MAP.put(signalCodeCompressed, !(ArrayUtil.isEmpty(exps) || !checkExps(exps)));
        }

        return STRING_BOOLEAN_HASH_MAP.get(signalCodeCompressed);
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

        if (!STRING_BOOLEAN_HASH_MAP.getOrDefault(signalCodeCompressed, false)) {
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

                if (!matcher.find()) {
                    res = false;
                }

                String left = CatchUtil.catchExcept(() -> matcher.group(1));
                String middle = CatchUtil.catchExcept(() -> matcher.group(2));
                String right = CatchUtil.catchExcept(() -> matcher.group(3));

                String value = paramMap.getOrDefault(left, "").toString();
                if (Objects.isNull(right)) {
                    // ?????? right ??????????????????????????????????????????????????? param ????????????????????????????????????
                    res = BooleanUtil.toBoolean(value);
                } else {
                    // ???right??????????????????????????????middle??????????????????
                    if (Objects.nonNull(middle) && StrUtil.isNotBlank(middle) && middle.startsWith("!")) {
                        res = !StrUtil.equals(value, right);
                    } else {
                        res = StrUtil.equals(value, right);
                    }
                }
            }

            // ?????? ??? ??????????????? res ???true????????????????????????true
            if (!and && res) {
                return true;
            }

            // ?????? ??? ??????????????? res ???false????????????????????????false
            if (and && !res) {
                return false;
            }
        }

        // ?????????????????????????????????????????????true??????????????????false
        return and;
    }
}
