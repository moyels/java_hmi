package top.moyel.common.hmi.inner.processors;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import top.moyel.common.hmi.inner.entity.InnerHmiDetail;
import top.moyel.common.hmi.inner.entity.InnerHmiProcessorBean;
import top.moyel.common.hmi.processor.BaseHmiProcessor;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author moyel
 */
public class SubIndexHmiProcessor extends BaseHmiProcessor<InnerHmiProcessorBean, InnerHmiProcessorBean> {
    @Override
    protected boolean check(InnerHmiProcessorBean inParam) {
        List<InnerHmiDetail> hmiDetails = inParam.getHmiDetails();
        if (CollectionUtil.isEmpty(hmiDetails)) {
            return false;
        }

        for (InnerHmiDetail hmiDetail : hmiDetails) {
            if (StrUtil.isBlank(hmiDetail.getSubIndex())) {
                return false;
            }
        }

        return true;
    }

    @Override
    protected Object process(InnerHmiProcessorBean inParam) {
        List<Object> list = new ArrayList<>();

        List<InnerHmiDetail> hmiDetails = inParam.getHmiDetails();
        Map<String, List<InnerHmiDetail>> subIndexDetailsMap = hmiDetails.stream()
                .collect(Collectors.groupingBy(InnerHmiDetail::getSubIndex, LinkedHashMap::new, Collectors.toList()));

        for (Map.Entry<String, List<InnerHmiDetail>> subIndexDetailsEntry : subIndexDetailsMap.entrySet()) {
            List<InnerHmiDetail> details = subIndexDetailsEntry.getValue();

            InnerHmiProcessorBean innerHmiProcessorBean = InnerHmiProcessorBean.build(details, inParam);
            Object value = subProcess(innerHmiProcessorBean);

            if (Objects.nonNull(value)) {
                list.add(value);
            }
        }

        return list;
    }
}
