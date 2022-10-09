package cn.crrczelc.common.hmi.test;

import cn.crrczelc.common.hmi.inner.entity.InnerHmiConfig;
import cn.crrczelc.common.hmi.inner.entity.InnerHmiDetail;
import cn.crrczelc.common.hmi.inner.processors.single.BaseSingleHmiExtendProcessor;
import cn.crrczelc.common.hmi.inner.processors.single.SingleHmiProcessor;
import cn.crrczelc.common.hmi.inner.transfer.InnerHmiTransfer;
import cn.crrczelc.common.hmi.inner.utils.ProcessorCacheUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

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
}
