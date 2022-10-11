package cn.crrczelc.common.hmi.inner.processors;

import cn.crrczelc.common.hmi.inner.entity.InnerHmiDetail;
import cn.crrczelc.common.hmi.inner.entity.InnerHmiProcessorBean;
import cn.crrczelc.common.hmi.processor.BaseHmiProcessor;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author moyel
 */
public class CarriageHmiProcessor extends BaseHmiProcessor<InnerHmiProcessorBean, InnerHmiProcessorBean> {
    @Override
    protected boolean check(InnerHmiProcessorBean inParam) {
        List<InnerHmiDetail> hmiDetails = inParam.getHmiDetails();
        if (CollectionUtil.isEmpty(hmiDetails)) {
            return false;
        }

        for (InnerHmiDetail hmiDetail : hmiDetails) {
            if (StrUtil.isBlank(hmiDetail.getCarriage())) {
                return false;
            }
        }

        return true;
    }

    @Override
    protected Object process(InnerHmiProcessorBean inParam) {
        Map<String, Object> carriageParamMap = new LinkedHashMap<>();

        List<InnerHmiDetail> hmiDetails = inParam.getHmiDetails();
        Map<String, List<InnerHmiDetail>> carriageDetailsMap = hmiDetails.stream()
                .collect(Collectors.groupingBy(InnerHmiDetail::getCarriage));

        for (Map.Entry<String, List<InnerHmiDetail>> carriageDetailsEntry : carriageDetailsMap.entrySet()) {
            String key = carriageDetailsEntry.getKey();
            List<InnerHmiDetail> details = carriageDetailsEntry.getValue();

            InnerHmiProcessorBean innerHmiProcessorBean = InnerHmiProcessorBean.build(details, inParam);
            Object value = subProcess(innerHmiProcessorBean);

            if (Objects.nonNull(value)) {
                carriageParamMap.put(key, value);
            }
        }

        return carriageParamMap;
    }
}
