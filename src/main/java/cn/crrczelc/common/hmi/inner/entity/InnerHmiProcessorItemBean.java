package cn.crrczelc.common.hmi.inner.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 在 hmi processor 中使用 作为入参的类
 *
 * @author moyel
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InnerHmiProcessorItemBean {
    private InnerHmiDetail hmiDetail;
    private Map<String, Object> paramMap;
}
