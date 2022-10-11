package cn.crrczelc.common.hmi.inner.item;

import cn.crrczelc.common.hmi.inner.entity.InnerHmiProcessorItemBean;
import cn.crrczelc.common.hmi.processor.BaseHmiItemProcessor;
import cn.crrczelc.common.hmi.utils.CatchUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.expression.ExpressionUtil;
import com.googlecode.aviator.AviatorEvaluator;

import java.util.Objects;

/**
 * @author moyel
 */
public class HmiAviatorItemProcessor extends BaseHmiItemProcessor<InnerHmiProcessorItemBean> {
    private Boolean expressionCache;

    public HmiAviatorItemProcessor() {
        this(false);
    }

    public HmiAviatorItemProcessor(Boolean expressionCache) {
        this.expressionCache = expressionCache;

        if (this.expressionCache) {
            AviatorEvaluator.getInstance().setCachedExpressionByDefault(true);
        }
    }

    public void setExpressionCache(Boolean expressionCache) {
        if (Objects.equals(expressionCache, this.expressionCache)) {
            return;
        }

        this.expressionCache = expressionCache;
        AviatorEvaluator.getInstance().setCachedExpressionByDefault(this.expressionCache);
    }

    @Override
    protected boolean check(InnerHmiProcessorItemBean checkParam) {
        String signalCode = CatchUtil.catchExcept(() -> checkParam.getHmiDetail().getSignalCode());

        if (StrUtil.isBlank(signalCode)) {
            return false;
        }

        return Objects.nonNull(CatchUtil.catchExcept(() -> AviatorEvaluator.compile(signalCode)));
    }

    @Override
    protected Object process(InnerHmiProcessorItemBean inParam) {
        String signalCode = CatchUtil.catchExcept(() -> inParam.getHmiDetail().getSignalCode());

        return CatchUtil.catchExcept(() -> ExpressionUtil.eval(signalCode, inParam.getParamMap()));
    }
}
