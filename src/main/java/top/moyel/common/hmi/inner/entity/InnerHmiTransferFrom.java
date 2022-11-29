package top.moyel.common.hmi.inner.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * inner hmi transfer的入参类型
 *
 * @author moyel
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InnerHmiTransferFrom {
    private String lineCode;
    private String hmiCode;
    private Map<String, List<InnerHmiDetail>> hmiDetailMap;
    private Map<String, Object> paramMap;
    private Map<String, Object> extraParam;
}
