package cn.crrczelc.common.hmi.test;

import cn.crrczelc.common.hmi.inner.entity.InnerHmiDetail;
import cn.crrczelc.common.hmi.inner.processors.iterator.IIteratorAdapter;

import java.util.ArrayList;
import java.util.List;

public class TestIteratorClass implements IIteratorAdapter<Integer> {
    @Override
    public List<Integer> iteratorList(InnerHmiDetail innerHmiDetail) {
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
