package top.moyel.common.hmi.inner.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author moyel
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InnerHmiDetail {
    private Long configId;
    private String carriage;
    private String hmiCode;
    private String param;
    private String paramName;
    private String subIndex;
    private String signalCode;
    private String mappingValue;
    private String defaultValue;
    private String unit;
    private String remark;
    private String iteratorClass;
    private Object extraInfo;
}
