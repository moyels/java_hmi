package cn.crrczelc.common.hmi.processor;

import cn.crrczelc.common.hmi.utils.CatchUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * processor的公共接口
 * 描述一个需要 check 是否符合条件然后才执行process并返回值的接口
 *
 * @param <IN>  入参的泛型
 * @param <OUT> 返回值参数的泛型
 *
 * @author moyel
 */
public abstract class BaseProcessor<IN, OUT> {
    /**
     * 检查 check 是否符合条件
     *
     * @param checkParam 入参
     *
     * @return 是否符合条件
     */
    protected abstract boolean check(IN checkParam);

    /**
     * 执行操作
     *
     * @param inParam 入参
     *
     * @return 操作的结果
     */
    protected abstract OUT process(IN inParam);

    /**
     * 串联 check 和 process 两个行为，
     *
     * @param inParam 入参
     *
     * @return 返回值
     */
    public final OUT execute(IN inParam) {
        if (check(inParam)) {
            return CatchUtil.catchExcept(() -> process(inParam));
        }

        return null;
    }
}
