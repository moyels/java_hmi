package cn.crrczelc.common.hmi.inner.utils;

import cn.crrczelc.common.hmi.inner.entity.InnerHmiDetail;
import cn.crrczelc.common.hmi.inner.entity.InnerHmiProcessorBean;
import cn.crrczelc.common.hmi.inner.processors.CarriageHmiProcessor;
import cn.crrczelc.common.hmi.inner.processors.SubIndexHmiProcessor;
import cn.crrczelc.common.hmi.inner.processors.iterator.IteratorHmiProcessor;
import cn.crrczelc.common.hmi.inner.processors.single.BaseSingleHmiExtendProcessor;
import cn.crrczelc.common.hmi.inner.processors.single.SingleHmiProcessor;
import cn.crrczelc.common.hmi.processor.BaseHmiProcessor;
import cn.crrczelc.common.hmi.utils.Maps;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;

import java.util.LinkedHashMap;

/**
 * @author moyel
 */
public class ProcessorCacheUtil {
    public static final LinkedHashMap<Class<? extends BaseHmiProcessor<InnerHmiProcessorBean, ?>>, BaseHmiProcessor<InnerHmiProcessorBean, ?>> PROCESSOR_MAP = Maps.expectedLinked(4);

    static {
        SingleHmiProcessor singleHmiProcessor = new SingleHmiProcessor(true);
        singleHmiProcessor.setPreProcessor(new JsonPreProcessor());

        IteratorHmiProcessor iteratorHmiProcessor = new IteratorHmiProcessor();
        iteratorHmiProcessor.appendSubProcessor(singleHmiProcessor);

        SubIndexHmiProcessor subIndexHmiProcessor = new SubIndexHmiProcessor();
        subIndexHmiProcessor.appendSubProcessor(iteratorHmiProcessor).appendSubProcessor(singleHmiProcessor);

        CarriageHmiProcessor carriageHmiProcessor = new CarriageHmiProcessor();
        carriageHmiProcessor.appendSubProcessor(subIndexHmiProcessor).appendSubProcessor(iteratorHmiProcessor)
                .appendSubProcessor(singleHmiProcessor);

        PROCESSOR_MAP.put(CarriageHmiProcessor.class, carriageHmiProcessor);
        PROCESSOR_MAP.put(SubIndexHmiProcessor.class, subIndexHmiProcessor);
        PROCESSOR_MAP.put(IteratorHmiProcessor.class, iteratorHmiProcessor);
        PROCESSOR_MAP.put(SingleHmiProcessor.class, singleHmiProcessor);
    }

    public static SingleHmiProcessor getSingleHmiProcessor() {
        if (!PROCESSOR_MAP.containsKey(SingleHmiProcessor.class)) {
            return null;
        }

        return ((SingleHmiProcessor) PROCESSOR_MAP.get(SingleHmiProcessor.class));
    }

    static class JsonPreProcessor extends BaseSingleHmiExtendProcessor<InnerHmiDetail, Void> {
        @Override
        protected boolean check(InnerHmiDetail checkParam) {
            return !(!(checkParam.getExtraInfo() instanceof String) || StrUtil.isBlank(((String) checkParam.getExtraInfo())) || !JSONUtil.isTypeJSONObject(((String) checkParam.getExtraInfo())));
        }

        @Override
        protected Void process(InnerHmiDetail inParam) {
            inParam.setExtraInfo(JSONUtil.parseObj(((String) inParam.getExtraInfo())));
            return null;
        }
    }
}
