package cn.crrczelc.common.hmi.inner.processors.single;

import cn.crrczelc.common.hmi.processor.BaseProcessor;

/**
 * @author moyel
 */
public abstract class BaseSingleHmiExtendProcessor<IN, OUT> extends BaseProcessor<IN, OUT> {
    @Override
    protected boolean check(IN checkParam) {
        return true;
    }
}
