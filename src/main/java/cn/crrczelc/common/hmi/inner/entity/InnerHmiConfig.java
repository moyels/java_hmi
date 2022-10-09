package cn.crrczelc.common.hmi.inner.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author moyel
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InnerHmiConfig {
    private Long id;
    private String hmiCode;
    private String hmiName;
    private Integer status;
}
