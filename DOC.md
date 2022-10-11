1. 安装
    1. maven
       ```xml
       <dependency>
          <groupId>cn.crrczelc.common</groupId>
          <artifactId>hmi</artifactId>
          <version>${latest_version}</version>
       </dependency>
       ```
    2. gradle
       ```gradle
       dependencies {
          compile 'cn.crrczelc.common:hmi:${latest_version}'
       }
       ```
    3. gradle(dsl)
       ```kts
       dependencies {
          implementation("cn.crrczelc.common:hmi:${latest_version}")
       }
       ```

2. 使用
    1. 需要创建一个`InnerHmiTransfer`的单例
        1. Spring Boot （java）
           ```java
           @Configuration
           class BeanFactory {
               @Bean
               public InnerHmiTransfer innerHmiTransfer() {
                   return new InnerHmiTransfer.Builder()
                       .setLineCoachesMap(lineCoachesMap)
                       .setHmiConfigs(innerHmiConfigs)
                       .setHmiDetails(innerHmiDetails)
                       .appendProcessor()
                       .build();
               }
           }
           ```
        2. 手动单例（java）
           ```java
           class InnerHmiTransferHolder {
               private volatile static InnerHmiTransfer innerHmiTransfer = null;
               
               public static InnerHmiTransfer getInstance() {
                   if (innerHmiTransfer == null) {
                       synchronized (InnerHmiTransferHolder.class) {
                           if (innerHmiTransfer == null) {
                               innerHmiTransfer = new InnerHmiTransfer.Builder()
                                   .setLineCoachesMap(lineCoachesMap)
                                   .setHmiConfigs(innerHmiConfigs)
                                   .setHmiDetails(innerHmiDetails)
                                   .appendProcessor()
                                   .build();
                           }
                       }
                   }
           
                   return innerHmiTransfer;
               }
           }
           ```
    2. 执行实际hmi转换
        1. java
            ```java
           class TestService {
               // 此处使用方式根据单例的实现方式不同，如使用SpringBoot的bean方式，则可以注入，如果使用手动单例则使用getInstance()方法获取
               private InnerHmiTransfer innerHmiTransfer;
           
               public Map<String, Object> executeTransfer(String lineCode, String trainCode, String coach, String hmiCode) {
                   String redisKey = buildRedisKey(lineCode, trainCode, coach);
                   Map<String, Object> redisParamMap = getRedisParamMap(redisKey);
           
                   Map<String, Object> extraParamMap = new HashMap<>();
                   extraParamMap.put("train", trainCode);
                   extraParamMap.put("coach", coach);
           
                   // 此处最后一个参数是额外参数，用以在遍历类型的hmi中提供额外的参数以便定义迭代规则
                   return innerHmiTransfer.transfer(lineCode, hmiCode, redisParamMap, extraParamMap);
               }
           }
           ```

3. 自定义processor

   > 此前在创建InnerHmiTransfer时是直接使用无参数的appendProcessor，此操作会直接将ProcessorCacheUtil中创建好的processor按顺序加入到transfer中，如果需要新增自定义的processor，则需要在此处调整appendProcessor的执行

   1. java
      ```java
      class Demo {
          public InnerHmiTransfer innerHmiTransfer() {
              new InnerHmiTransfer.Builder()
       		   .setXxx()
       		   // 此处可根据按顺序添加自定义的processor，且自定义的processor中可以添加subProcessor（如添加singleHmiProcessor），则可以利用已写好的部分hmi处理器
       		   .appendProcessor(xxxProcessor)
       		   // singleHmiProcessor必须最后一个添加，因为是最基础的hmi转换的功能
       		   .appendProcessor(singleHmiProcessor)
       		   .build();
          }
      }
      ```

4. 自定义前后处理器

    > 此处前后处理器是针对于singleHmiProcessor而言的，因为transfer中调用的所有processor的subProcessors中都需要包含singleHmiProcessor作为基础hmi处理的功能

    1. 需要注意的是，singleHmiProcessor的前处理器默认使用了JsonParse的前处理器，会在执行hmi前将extraInfo信息转换为JsonObject类型，如果设置了自定义的前处理器但又想保留此功能，可以使用subProcessors形式组合多个processor进行处理

    2. singleHmiProcessor的后处理器默认为空

    3. 使用方式

       ```java
       class Demo {
           public void demo() {
               // 直接使用处理器缓存工具设置（此设置在单例创建前创建后应当均会生效，因为修改的是引用，但需要在transfer实际执行前设置）
               ProcessorCacheUtil.getSingleHmiProcessor()
       			// 设置前处理器
                   .setPreProcessor(customPreProcessor)
                   // 设置后处理器
                   .setPostProcessor(customPostProcessor);
           }
       }
       ```

5. 遍历类型hmi对应的adapter实现流程

    ```java
    public class TestIteratorClass implements IIteratorAdapter<Integer> {
        /**
         * 此方法用以生成遍历的list，innerHmiProcessorBean中包含了调用transfer时提供的线路号和extraParam参数，如果没有提供则为空
         **/
        @Override
        public List<Integer> iteratorList(InnerHmiProcessorBean innerHmiProcessorBean, InnerHmiDetail innerHmiDetail) {
    
            List<Integer> list = new ArrayList<>();
            for (int i = 0; i < 43; i++) {
                list.add(i);
            }
    
            return list;
        }
    
        /**
         * 此方法会根据上述方法生成的list遍历后执行，比如此处根据item值生成对应的signalCode，设置到detail中，在执行hmi转换时即会使用此处设置后的signalCode信息
         **/
        @Override
        public void alterDetail(Integer item, InnerHmiDetail innerHmiDetail) {
            String pos = item * 512 + "_" + item;
            innerHmiDetail.setSignalCode(pos);
        }
        
        /**
         * 此方法不强制要求实现，不实现时将返回null
         * <p>
         * 迭代类型的hmi转换后将生成一个列表类型的数据，如果需要对其作一些操作，则可以实现此方法，并将转换后的值返回
         **/
        @Override
        public Object formatConversion(List<Object> list) {
            return IIteratorAdapter.super.formatConversion(list);
        }
    }
    ```