package cn.crrczelc.common.hmi.inner.processors.iterator;

import cn.crrczelc.common.hmi.inner.entity.InnerHmiDetail;
import cn.crrczelc.common.hmi.inner.entity.InnerHmiProcessorBean;

import java.util.List;

/**
 * @author moyel
 */
public interface IIteratorAdapter<T> {
    /**
     * 构造循环列表
     *
     * @param innerHmiDetail detail 信息
     *
     * @return 循环的列表
     */
    List<T> iteratorList(InnerHmiProcessorBean innerHmiProcessorBean, InnerHmiDetail innerHmiDetail);

    /**
     * 修改 detail 中的内容，如signalCode，以便根据循环获取不同的值
     *
     * @param item           一次循环的内容
     * @param innerHmiDetail 详情
     */
    void alterDetail(T item, InnerHmiDetail innerHmiDetail);

    /**
     * 迭代结果的格式转换
     * 默认的迭代结果是一个 object 的列表，当此方法返回值不为空时，讲使用此方法的返回值作为迭代的实际返回值
     * 例如可以讲list转为map
     *
     * @param list 迭代结果列表
     *
     * @return 转换后的迭代结果
     */
    default Object formatConversion(List<Object> list) {
        return null;
    }
}
