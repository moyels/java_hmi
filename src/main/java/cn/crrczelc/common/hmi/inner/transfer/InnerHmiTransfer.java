package cn.crrczelc.common.hmi.inner.transfer;

import cn.crrczelc.common.hmi.inner.consts.ParamConsts;
import cn.crrczelc.common.hmi.inner.entity.InnerHmiConfig;
import cn.crrczelc.common.hmi.inner.entity.InnerHmiDetail;
import cn.crrczelc.common.hmi.inner.entity.InnerHmiProcessorBean;
import cn.crrczelc.common.hmi.inner.entity.InnerHmiTransferFrom;
import cn.crrczelc.common.hmi.inner.utils.ProcessorCacheUtil;
import cn.crrczelc.common.hmi.processor.BaseHmiProcessor;
import cn.crrczelc.common.hmi.processor.BaseProcessor;
import cn.crrczelc.common.hmi.transfer.BaseHmiTransfer;
import cn.crrczelc.common.hmi.utils.Maps;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 内置hmi转换器
 *
 * @author moyel
 */
public class InnerHmiTransfer extends BaseHmiTransfer<InnerHmiTransferFrom, InnerHmiProcessorBean> {
    private Map<String, List<String>> lineCoachesMap;
    private List<InnerHmiConfig> hmiConfigs;
    private List<InnerHmiDetail> hmiDetails;
    private Map<String, InnerHmiConfig> hmiConfigMap;
    private Map<String, Map<String, List<InnerHmiDetail>>> hmiDetailsMap;

    public InnerHmiTransfer() {
        this(MapUtil.newHashMap(0), Collections.emptyList(), Collections.emptyList());
    }

    public InnerHmiTransfer(Map<String, List<String>> lineCoachesMap, List<InnerHmiConfig> hmiConfigs, List<InnerHmiDetail> hmiDetails) {
        this.lineCoachesMap = lineCoachesMap;
        this.hmiConfigs = hmiConfigs;
        this.hmiDetails = hmiDetails;

        init();
    }

    public void init() {
        hmiConfigMap = groupHmiConfigs(hmiConfigs);
        hmiDetailsMap = groupHmiDetails(hmiDetails);
    }

    public static Map<String, InnerHmiConfig> groupHmiConfigs(List<InnerHmiConfig> configs) {
        if (CollectionUtil.isEmpty(configs)) {
            return Maps.expectedSize(0);
        }

        List<InnerHmiConfig> hmiConfigList = configs.stream()
                .filter(innerHmiConfig -> !(Objects.isNull(innerHmiConfig) || StrUtil.isBlank(innerHmiConfig.getHmiCode())))
                .collect(Collectors.toList());

        Map<String, InnerHmiConfig> hmiCodeConfigMap = Maps.expectedSize(hmiConfigList.size());

        for (InnerHmiConfig innerHmiConfig : hmiConfigList) {
            hmiCodeConfigMap.put(innerHmiConfig.getHmiCode(), innerHmiConfig);
        }

        return hmiCodeConfigMap;
    }

    public static Map<String, Map<String, List<InnerHmiDetail>>> groupHmiDetails(List<InnerHmiDetail> details) {
        if (CollectionUtil.isEmpty(details)) {
            return Maps.expectedSize(0);
        }

        Map<String, List<InnerHmiDetail>> hmiCodeDetailsMap = details.stream()
                .filter(innerHmiDetail -> !(Objects.isNull(innerHmiDetail) || StrUtil.hasBlank(innerHmiDetail.getHmiCode(), innerHmiDetail.getParam())))
                .collect(Collectors.groupingBy(InnerHmiDetail::getHmiCode));

        Map<String, Map<String, List<InnerHmiDetail>>> hmiCodeParamDetailsMap = Maps.expectedSize(hmiCodeDetailsMap.size());

        for (Map.Entry<String, List<InnerHmiDetail>> hmiCodeDetailsEntry : hmiCodeDetailsMap.entrySet()) {
            String hmiCode = hmiCodeDetailsEntry.getKey();
            List<InnerHmiDetail> partDetails = hmiCodeDetailsEntry.getValue();

            Map<String, List<InnerHmiDetail>> paramDetailsMap = partDetails.stream()
                    .collect(Collectors.groupingBy(InnerHmiDetail::getParam));

            hmiCodeParamDetailsMap.put(hmiCode, paramDetailsMap);
        }

        return hmiCodeParamDetailsMap;
    }

    public Map<String, Object> transfer(String lineCode, String hmiCode, Map<String, Object> paramMap) {
        if (StrUtil.isBlank(hmiCode) || !hmiConfigMap.containsKey(hmiCode) || !hmiDetailsMap.containsKey(hmiCode)) {
            return Maps.expectedSize(0);
        }

        Map<String, List<InnerHmiDetail>> paramDetailsMap = hmiDetailsMap.get(hmiCode);
        InnerHmiTransferFrom inParam = new InnerHmiTransferFrom(lineCode, hmiCode, paramDetailsMap, paramMap);
        return transfer(inParam);
    }

    @Override
    public Map<String, Object> transfer(InnerHmiTransferFrom inParam) {
        Map<String, List<InnerHmiDetail>> hmiDetailMap = inParam.getHmiDetailMap();

        boolean hasCoaches = StrUtil.isNotBlank(inParam.getLineCode());
        int size = hasCoaches ? hmiDetailMap.size() + 1 : hmiDetailMap.size();

        Map<String, Object> resMap = Maps.expectedSize(size, LinkedHashMap.class);

        if (hasCoaches) {
            resMap.put(ParamConsts.PARAM_COACHES_KEY, lineCoachesMap.getOrDefault(inParam.getLineCode(), CollectionUtil.newArrayList()));
        }

        for (Map.Entry<String, List<InnerHmiDetail>> paramDetailsEntry : hmiDetailMap.entrySet()) {
            String param = paramDetailsEntry.getKey();

            InnerHmiProcessorBean processorBean = new InnerHmiProcessorBean(paramDetailsEntry.getValue(), inParam.getParamMap());

            resMap.put(param, singleTransfer(processorBean));
        }

        return resMap;
    }

    public static class Builder {
        private Map<String, List<String>> lineCoachesMap;
        private List<InnerHmiConfig> hmiConfigs;
        private List<InnerHmiDetail> hmiDetails;
        private List<BaseProcessor<InnerHmiProcessorBean, Object>> processors = null;

        public Builder setLineCoachesMap(Map<String, List<String>> lineCoachesMap) {
            this.lineCoachesMap = lineCoachesMap;
            return this;
        }

        public Builder setHmiConfigs(List<InnerHmiConfig> hmiConfigs) {
            this.hmiConfigs = hmiConfigs;
            return this;
        }

        public Builder setHmiDetails(List<InnerHmiDetail> hmiDetails) {
            this.hmiDetails = hmiDetails;
            return this;
        }

        /**
         * 将缓存中的processor全部添加到transfer中
         *
         * @return Builder
         */
        public Builder appendProcessor() {
            for (Map.Entry<Class<? extends BaseHmiProcessor<InnerHmiProcessorBean, ?>>, BaseHmiProcessor<InnerHmiProcessorBean, ?>> processorEntry : ProcessorCacheUtil.PROCESSOR_MAP.entrySet()) {
                appendProcessor(processorEntry.getValue());
            }

            return this;
        }

        /**
         * 添加一个processor到transfer中
         *
         * @param processor processor
         *
         * @return Builder
         */
        public Builder appendProcessor(BaseProcessor<InnerHmiProcessorBean, Object> processor) {
            if (Objects.isNull(processor)) {
                return this;
            }

            if (Objects.isNull(processors)) {
                processors = new ArrayList<>();
            }

            processors.add(processor);

            return this;
        }

        public InnerHmiTransfer build() {
            InnerHmiTransfer hmiTransfer = new InnerHmiTransfer(lineCoachesMap, hmiConfigs, hmiDetails);

            if (CollectionUtil.isNotEmpty(processors)) {
                for (BaseProcessor<InnerHmiProcessorBean, Object> processor : processors) {
                    hmiTransfer.appendProcessor(processor);
                }
            }

            return hmiTransfer;
        }
    }
}
