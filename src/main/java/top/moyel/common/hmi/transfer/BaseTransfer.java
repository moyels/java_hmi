package top.moyel.common.hmi.transfer;

import top.moyel.common.hmi.processor.BaseProcessor;
import top.moyel.common.hmi.utils.CatchUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * transfer的基类，以辨别每一个transfer
 * <p>
 * 一个 transfer 包含 多个 processor
 *
 * @author moyel
 */
public abstract class BaseTransfer<TIN, TOUT, PIN, POUT> {
    protected final List<BaseProcessor<PIN, POUT>> processors = new ArrayList<>();

    public BaseTransfer<TIN, TOUT, PIN, POUT> appendProcessor(BaseProcessor<PIN, POUT> processor) {
        if (Objects.nonNull(processor)) {
            processors.add(processor);
        }

        return this;
    }

    /**
     * 转换方法
     *
     * @param inParam 入参
     * @return 出参
     */
    protected POUT singleTransfer(PIN inParam) {
        for (BaseProcessor<PIN, POUT> processor : processors) {
            POUT item = CatchUtil.catchExcept(() -> processor.execute(inParam));
            if (Objects.isNull(item)) {
                continue;
            }

            return item;
        }

        return null;
    }

    /**
     * 实际使用的转换方法
     *
     * @param inParam 入参
     * @return 返回结果
     */
    public abstract TOUT transfer(TIN inParam);
}
