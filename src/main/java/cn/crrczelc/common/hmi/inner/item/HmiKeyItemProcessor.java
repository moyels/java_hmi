package cn.crrczelc.common.hmi.inner.item;

import cn.crrczelc.common.hmi.inner.entity.InnerHmiProcessorItemBean;
import cn.crrczelc.common.hmi.inner.utils.SignalCodeFormatUtil;
import cn.crrczelc.common.hmi.processor.BaseHmiItemProcessor;
import cn.crrczelc.common.hmi.utils.CatchUtil;
import cn.hutool.core.util.StrUtil;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author moyel
 */
public class HmiKeyItemProcessor extends BaseHmiItemProcessor<InnerHmiProcessorItemBean> {
    @Override
    protected boolean check(InnerHmiProcessorItemBean checkParam) {
        String signalCode = CatchUtil.catchExcept(() -> checkParam.getHmiDetail().getSignalCode());

        if (StrUtil.isBlank(signalCode)) {
            return false;
        }

        return SignalCodeFormatUtil.singleValid(signalCode) || SignalCodeFormatUtil.multiValid(signalCode);
    }

    @Override
    protected Object process(InnerHmiProcessorItemBean inParam) {
        String signalCode = CatchUtil.catchExcept(() -> inParam.getHmiDetail().getSignalCode());

        if (StrUtil.isBlank(signalCode)) {
            return null;
        }

        Map<String, Object> paramMap = CatchUtil.catchExcept(inParam::getParamMap);
        Map<String, Object> finalMap = Objects.nonNull(paramMap) ? paramMap : new HashMap<>(0);

        String[] multiRes = SignalCodeFormatUtil.getMultiRes(signalCode);
        if (Objects.nonNull(multiRes)) {
            return Arrays.stream(multiRes).map(key -> CatchUtil.catchExcept(() -> finalMap.get(key)));
        }

        return CatchUtil.catchExcept(() -> finalMap.get(signalCode.trim()));
    }
}
