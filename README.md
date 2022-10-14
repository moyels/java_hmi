# HMI 项目重构

1. 依赖
    1. maven坐标
        ```xml
       <dependency>
           <groupId>cn.crrczelc.common</groupId>
           <artifactId>hmi</artifactId>
           <version>0.1</version>
       </dependency>
        ```
    2. gradle坐标
        ```groovy
       implementation 'cn.crrczelc.common:hmi:0.1'
        ```
2. 因为前一个版本架构原因较难实现解析规则的新增，重构后的版本实现了前一个版本所有的功能，同时新增了如下功能
    1. signalCode支持简单表达式参数以数字开头，并支持不加引号的判定，如`2357_1==1||2357_1==10&&2357_1==11`
       可以执行判定并忽略`2357_1`类型，两边均转为字符串比较
        1. 左侧参数必须仅由（数字、字母及下划线）组成，当存在其他字符时无法判定为简单表达式类型
        2. 仅支持上述例子中简单表达式的形式判定，当存在括号约定优先级时，暂无法识别
        3. 当同时存在与或运算符时，会先以或作分隔符从左至右进行判定，如果两个或之间的部分存在与，则会将此作整体判定，如上例子中会将`2357_1==10&&2357_1==11`当作整体判定，而非先判定`2357_1==1||2357_1==10`再将结果和`2357_1==11`做与操作
        4. 简单表达式与或判断存在短路特性，即如`2357_1==1`判定结果为true则不会进行后续判定
    2. 支持hmi单项迭代操作，即单条hmi信息可通过书写`Adapter`
       实现循环迭代操作，示例见：`cn.crrczelc.common.hmi.common.processor.iterator.TestIteratorAdapter`

3. 使用方式

   > 因为使用了完全不同的架构，所以此处使用方式将有一个较大的改动，当然，简单的使用的话修改比较少

    1. 按依赖方式引入此包
    2. 构造bean的方式
        1. kotlin
           ```kotlin
           @Configuration
           class Beans {
               @Bean
               fun createTransferProcessor() : InnerHmiTransfer {
                    return InnerHmiTransfer.Builder()
                       .setLineCoachesMap(lineCoachesMap)
                       .setHmiConfigs(innerHmiConfigs)
                       .setHmiDetails(innerHmiDetails)
                       .appendProcessor()
                       .build()
               }
           }
           ```
        2. java
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

4. 描述图

    ![HMI架构描述图](https://s1.ax1x.com/2022/10/14/xd7ToD.png)