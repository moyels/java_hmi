package top.moyel.common.hmi.test;

import top.moyel.common.hmi.inner.entity.InnerHmiDetail;
import top.moyel.common.hmi.inner.entity.InnerHmiProcessorBean;
import top.moyel.common.hmi.inner.processors.iterator.IIteratorAdapter;

import java.util.ArrayList;
import java.util.List;

public class TestIteratorClass implements IIteratorAdapter<Integer> {
    @Override
    public List<Integer> iteratorList(InnerHmiProcessorBean innerHmiProcessorBean, InnerHmiDetail innerHmiDetail) {

        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < 43; i++) {
            list.add(i);
        }

        return list;
    }

    @Override
    public void alterDetail(Integer item, InnerHmiDetail innerHmiDetail) {
        String pos = item * 512 + "_" + item;
        innerHmiDetail.setSignalCode(pos);
    }
}
