package cn.crrczelc.common.hmi.inner.processors.iterator;

import cn.crrczelc.common.hmi.inner.entity.InnerHmiDetail;
import cn.crrczelc.common.hmi.inner.entity.InnerHmiProcessorBean;
import cn.crrczelc.common.hmi.processor.BaseHmiProcessor;
import cn.crrczelc.common.hmi.utils.CatchUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.Log;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author moyel
 */
public class IteratorHmiProcessor extends BaseHmiProcessor<InnerHmiProcessorBean, InnerHmiProcessorBean> {
    private static final Log LOGGER = Log.get(IteratorHmiProcessor.class);
    private static final Map<String, IIteratorAdapter<Object>> ITERATOR_CLASS_INST_MAP = new HashMap<>();

    @Override
    protected boolean check(InnerHmiProcessorBean inParam) {
        List<InnerHmiDetail> hmiDetails = inParam.getHmiDetails();

        return !(CollectionUtil.isEmpty(hmiDetails) || !checkIteratorClass(hmiDetails));
    }

    /**
     * check 遍历类型是否描述正确
     *
     * @param hmiDetails hmi items
     *
     * @return 是否描述正确
     */
    private boolean checkIteratorClass(List<InnerHmiDetail> hmiDetails) {
        List<String> iteratorClassList = hmiDetails.stream().map(InnerHmiDetail::getIteratorClass)
                .collect(Collectors.toList());

        for (String iteratorClass : iteratorClassList) {
            if (!checkSingleIteratorClass(iteratorClass)) {
                return false;
            }
        }

        return true;
    }

    private boolean checkSingleIteratorClass(String iteratorClassStr) {
        if (StrUtil.isBlank(iteratorClassStr)) {
            return false;
        }

        Class<?> iteratorClass = CatchUtil.catchExcept(() -> Class.forName(iteratorClassStr));
        if (Objects.isNull(iteratorClass)) {
            return false;
        }

        boolean contains = Arrays.asList(iteratorClass.getInterfaces()).contains(IIteratorAdapter.class);

        if (contains) {
            IIteratorAdapter<Object> iteratorAdapter = (IIteratorAdapter<Object>) CatchUtil.catchExcept(iteratorClass::newInstance);

            if (Objects.isNull(iteratorAdapter)) {
                LOGGER.error("请务必使用空构造方法，{0}类构造失败", iteratorClassStr);

                return false;
            }

            ITERATOR_CLASS_INST_MAP.put(iteratorClassStr, iteratorAdapter);
        }

        return contains;
    }

    @Override
    protected Object process(InnerHmiProcessorBean inParam) {
        List<Object> outResList = new ArrayList<>();

        List<InnerHmiDetail> hmiDetails = inParam.getHmiDetails();
        Map<String, List<InnerHmiDetail>> classStrDetailsMap = hmiDetails.stream()
                .collect(Collectors.groupingBy(InnerHmiDetail::getIteratorClass, LinkedHashMap::new, Collectors.toList()));

        if (CollectionUtil.isEmpty(classStrDetailsMap)) {
            return null;
        }

        for (Map.Entry<String, List<InnerHmiDetail>> classStrDetailsEntry : classStrDetailsMap.entrySet()) {
            String classStr = classStrDetailsEntry.getKey();

            ArrayList<Object> inResList = new ArrayList<>();
            IIteratorAdapter<Object> iteratorAdapter = ITERATOR_CLASS_INST_MAP.get(classStr);

            List<InnerHmiDetail> details = classStrDetailsEntry.getValue();

            for (InnerHmiDetail detail : details) {

                for (Object item : iteratorAdapter.iteratorList(inParam, detail)) {
                    InnerHmiDetail tempDetail = BeanUtil.copyProperties(detail, InnerHmiDetail.class);
                    iteratorAdapter.alterDetail(item, tempDetail);

                    List<InnerHmiDetail> tempDetails = CollectionUtil.newArrayList(tempDetail);
                    inResList.add(subProcess(new InnerHmiProcessorBean(tempDetails, inParam.getParamMap())));
                }

            }

            Object finalValue = iteratorAdapter.formatConversion(inResList);
            if (Objects.nonNull(finalValue)) {
                outResList.add(inResList);
                break;
            }

            outResList.add(inResList);
        }

        return classStrDetailsMap.size() >= 2 ? outResList : outResList.get(0);
    }
}
