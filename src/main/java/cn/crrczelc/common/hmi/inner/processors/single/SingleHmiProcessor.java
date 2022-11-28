package cn.crrczelc.common.hmi.inner.processors.single;

import cn.crrczelc.common.hmi.inner.entity.InnerHmiDetail;
import cn.crrczelc.common.hmi.inner.entity.InnerHmiProcessorBean;
import cn.crrczelc.common.hmi.inner.entity.InnerHmiProcessorItemBean;
import cn.crrczelc.common.hmi.inner.entity.InnerHmiResult;
import cn.crrczelc.common.hmi.inner.item.HmiAviatorItemProcessor;
import cn.crrczelc.common.hmi.inner.item.HmiExpItemProcessor;
import cn.crrczelc.common.hmi.inner.item.HmiKeyItemProcessor;
import cn.crrczelc.common.hmi.processor.BaseHmiProcessor;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * @author moyel
 */
public class SingleHmiProcessor extends BaseHmiProcessor<InnerHmiProcessorBean, InnerHmiProcessorItemBean> {
    private BaseSingleHmiExtendProcessor<InnerHmiDetail, Void> preProcessor;
    private BaseSingleHmiExtendProcessor<InnerHmiResult, Object> postProcessor;

    public SingleHmiProcessor() {
        this(false);
    }

    public SingleHmiProcessor(boolean expressionCache) {
        this.appendSubProcessor(new HmiKeyItemProcessor())
                .appendSubProcessor(new HmiExpItemProcessor())
                .appendSubProcessor(new HmiAviatorItemProcessor(expressionCache));
    }

    public SingleHmiProcessor setPreProcessor(BaseSingleHmiExtendProcessor<InnerHmiDetail, Void> preProcessor) {
        this.preProcessor = preProcessor;
        return this;
    }

    public SingleHmiProcessor setPostProcessor(BaseSingleHmiExtendProcessor<InnerHmiResult, Object> postProcessor) {
        this.postProcessor = postProcessor;
        return this;
    }

    /**
     * 简单校验参数
     *
     * @param inParam 入参
     * @return 简单校验是否符合程序继续执行的条件
     */
    private boolean validate(InnerHmiProcessorBean inParam) {
        return !CollectionUtil.isEmpty(inParam.getHmiDetails());
    }

    @Override
    protected boolean check(InnerHmiProcessorBean inParam) {
        // 如果简单校核都未通过则不需要继续check
        if (!validate(inParam)) {
            return false;
        }

        List<InnerHmiDetail> hmiDetails = inParam.getHmiDetails();

        for (InnerHmiDetail hmiDetail : hmiDetails) {
            if (StrUtil.isBlank(hmiDetail.getParam())) {
                return false;
            }
        }

        return true;
    }

    @Override
    protected Object process(InnerHmiProcessorBean inParam) {
        // 当使用正常流程进入此方法时，inParam必然已通过了validate及check的校验，所以不需要额外校验
        // 获取迭代器(使用迭代器的原因是可以简单且直接的获取是否是最后一个元素)
        Iterator<InnerHmiDetail> iterator = inParam.getHmiDetails().iterator();

        while (iterator.hasNext()) {
            InnerHmiDetail hmiDetail = iterator.next();
            boolean last = !iterator.hasNext();

            // 通过 子处理器 计算结果，此处计算出来的应该是value值
            InnerHmiProcessorItemBean itemParam = new InnerHmiProcessorItemBean(hmiDetail, inParam.getParamMap());

            if (Objects.nonNull(preProcessor)) {
                preProcessor.execute(itemParam.getHmiDetail());
            }

            Object value = subProcess(itemParam);

            // 根据value值构造最终结果
            InnerHmiResult innerHmiResult = buildResult(value, itemParam, last);

            // 如果不为空则直接返回
            if (Objects.nonNull(innerHmiResult)) {
                if (Objects.nonNull(postProcessor)) {
                    Object execute = postProcessor.execute(innerHmiResult);
                    if (Objects.nonNull(execute)) {
                        return execute;
                    }
                }

                return innerHmiResult;
            }
        }

        return null;
    }

    private InnerHmiResult buildResult(Object value, InnerHmiProcessorItemBean itemParam, boolean last) {
        boolean isNull = Objects.isNull(value);
        // 如果value为空 但是不是最后一条数据，则返回null，表示继续构造下一条记录
        if (isNull && !last) {
            return null;
        }

        // 如果此时value仍为空，则表示 是最后一条记录，所以此时需返回默认值
        if (isNull) {
            return InnerHmiResult.create(itemParam.getHmiDetail(), null, true);
        }

        if (isMappingItem(itemParam, value)) {
            // is mapping 时，不是布尔类型，但是是最后一条记录时， 使用字面值判断
            if (!BooleanUtil.isBoolean(value.getClass()) && !last) {
                return InnerHmiResult.create(itemParam.getHmiDetail(), BooleanUtil.toBoolean(value.toString()));
            }

            // 是布尔类型，且为true时
            if (BooleanUtil.isBoolean(value.getClass()) && ((Boolean) value)) {
                return InnerHmiResult.create(itemParam.getHmiDetail(), true);
            }

            // 此时表示虽然是mapping类型，但是value的结果为false，那么如果是最后一条记录，则返回默认
            if (last) {
                return InnerHmiResult.create(itemParam.getHmiDetail(), null, true);
            } else {
                // 当是mapping类型，但不是最后一条记录时，返回空示意继续构造下一条数据
                return null;
            }
        }

        // 运行至此时，value一定不为空，且不需要
        return InnerHmiResult.create(itemParam.getHmiDetail(), value);
    }

    private boolean isMappingItem(InnerHmiProcessorItemBean item, Object value) {
        // 如果 mapping value 没有值，表示绝不可能为 mapping 类型
        return !StrUtil.isBlank(item.getHmiDetail().getMappingValue());
    }
}
