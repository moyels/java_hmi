package top.moyel.common.hmi.inner.processors.single;

import top.moyel.common.hmi.processor.BaseProcessor;

/**
 * @author moyel
 */
public abstract class BaseSingleHmiExtendProcessor<IN, OUT> extends BaseProcessor<IN, OUT> {
    @Override
    protected boolean check(IN checkParam) {
        return true;
    }
}
