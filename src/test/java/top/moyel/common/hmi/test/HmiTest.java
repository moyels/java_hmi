package top.moyel.common.hmi.test;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.json.JSONUtil;
import org.junit.jupiter.api.Test;
import top.moyel.common.hmi.inner.entity.InnerHmiConfig;
import top.moyel.common.hmi.inner.entity.InnerHmiDetail;
import top.moyel.common.hmi.inner.transfer.InnerHmiTransfer;

import java.util.List;
import java.util.Map;

public class HmiTest {
    @Test
    public void hmi() {
        List<InnerHmiConfig> innerHmiConfigs = CollectionUtil.newArrayList(
                new InnerHmiConfig(1L, "test", "测试", 1)
        );

        List<InnerHmiDetail> innerHmiDetails = CollectionUtil.newArrayList(
                new InnerHmiDetail(1L, "M1", "test", "test_1", "测试1", "1_1", "2751_1", null, null, "V", "备注--默认备注", null, "{}"),
                new InnerHmiDetail(1L, "M2", "test", "test_1", "测试1", "1_1", "2751_2", null, "0", "V", "备注--默认备注", null, "{}")
        );

        InnerHmiTransfer hmiTransfer = new InnerHmiTransfer.Builder()
                .setHmiConfigs(innerHmiConfigs)
                .setHmiDetails(innerHmiDetails)
                .appendProcessor()
                .build();

        Map<String, Object> paramMap = MapUtil.builder("2751_3", (Object) 10).build();
        Map<String, Object> res = hmiTransfer.transfer(null, "test", paramMap);

        System.out.println(JSONUtil.parseObj(res));
    }

    @Test
    public void iteratorHmi() {
        List<InnerHmiConfig> innerHmiConfigs = CollectionUtil.newArrayList(
                new InnerHmiConfig(1L, "test", "测试", 1)
        );

        List<InnerHmiDetail> innerHmiDetails = CollectionUtil.newArrayList(
                new InnerHmiDetail(1L, "M1", "test", "test_1", "测试1", null, "2751_1", null, "0", "V", "备注--默认备注", "cn.crrczelc.common.hmi.test.TestIteratorClass", "{}")
        );

        InnerHmiTransfer hmiTransfer = new InnerHmiTransfer.Builder()
                .setHmiConfigs(innerHmiConfigs)
                .setHmiDetails(innerHmiDetails)
                .appendProcessor()
                .build();

        Map<String, Object> param = MapUtil.builder("512_1", (Object) 10).build();
        Map<String, Object> test = hmiTransfer.transfer(null, "test", param);

        System.out.println(JSONUtil.toJsonStr(test));
    }
}
