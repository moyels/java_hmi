package top.moyel.common.hmi.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author moyel
 */
public abstract class BaseHmiProcessor<IN, ITEM> extends BaseProcessor<IN, Object> {

    protected List<BaseProcessor<ITEM, Object>> subProcessors = new ArrayList<>();

    /**
     * 增加 子处理器
     *
     * @param subProcessor 子处理器
     * @return 本身，以实现链式调用
     */
    public BaseHmiProcessor<IN, ITEM> appendSubProcessor(BaseProcessor<ITEM, Object> subProcessor) {
        if (Objects.nonNull(subProcessor)) {
            subProcessors.add(subProcessor);
        }

        return this;
    }

    public Object subProcess(ITEM itemParam) {
        for (BaseProcessor<ITEM, Object> subProcessor : subProcessors) {
            Object outParam = subProcessor.execute(itemParam);

            if (Objects.nonNull(outParam)) {
                return outParam;
            }
        }

        return null;
    }
}
