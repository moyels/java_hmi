package cn.crrczelc.common.hmi.inner.entity;

import cn.crrczelc.common.hmi.inner.consts.DelimiterConsts;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * @author moyel
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InnerHmiResult {
    private String hmiCode;
    private String param;
    private String paramName;
    private String subIndex;
    private Object value;
    private String unit;
    private String remark;
    private Object extraInfo;


    /**
     * 携带 isDefault 参数方法的简化，默认 isDefault 为 false
     * 默认当执行此方法时构造结果已经确认，则value为空时将自动使用默认值，而不会返回空，是否是默认值参数只是作参考
     *
     * @param hmiDetail hmi 内容
     * @param value     计算值
     *
     * @return hmi 结果
     */
    public static InnerHmiResult create(InnerHmiDetail hmiDetail, Object value) {
        return create(hmiDetail, value, false);
    }

    /**
     * 默认当执行此方法时构造结果已经确认，则value为空时将自动使用默认值，而不会返回空，是否是默认值参数只是作remark的判断依据
     * 对结果不会造成影响
     *
     * @param hmiDetail hmi 内容
     * @param value     计算值
     * @param isDefault 是否是默认值
     *
     * @return hmi 结果
     */
    public static InnerHmiResult create(InnerHmiDetail hmiDetail, Object value, boolean isDefault) {
        Object finalValue = judgeValue(hmiDetail, value, isDefault);

        return new InnerHmiResult(
                hmiDetail.getHmiCode(),
                hmiDetail.getParam(),
                hmiDetail.getParamName(),
                hmiDetail.getSubIndex(),
                finalValue,
                hmiDetail.getUnit(),
                getRemark(hmiDetail.getRemark(), isDefault),
                hmiDetail.getExtraInfo()
        );
    }

    /**
     * 判断 value 的最终结果
     *
     * @param hmiDetail hmi项
     * @param value     计算的value值
     * @param isDefault 是否确认是默认值
     *
     * @return 最终放在hmi结果中的value
     */
    private static Object judgeValue(InnerHmiDetail hmiDetail, Object value, boolean isDefault) {
        if (Objects.isNull(value)) {
            return hmiDetail.getDefaultValue();
        }

        // 如果 value 为布尔类型，且mapping value不为空
        if (BooleanUtil.isBoolean(value.getClass()) && StrUtil.isNotBlank(hmiDetail.getMappingValue())) {
            // 如果 布尔值 为 true，则返回mapping value的值（正常情况的判定下，当value为布尔值时，运行至此时应当确定为true，但为了避免异常，此处进行判断）
            if (((Boolean) value)) {
                return hmiDetail.getMappingValue();
            }

            // 如果 布尔值 为 false，则返回默认值
            return hmiDetail.getDefaultValue();
        }

        // 如果以上情况都不符合，则应当是直接使用value的值
        return value;
    }

    private static String getRemark(String remark, boolean isDefault) {
        if (StrUtil.isBlank(remark)) {
            return null;
        }

        String[] remarks = remark.split(DelimiterConsts.REMARK_DELIMITER);
        return remark.length() < 2 ? remark : isDefault ? remarks[1] : remarks[0];
    }
}
