信贸链合约

1. 使用maven编译项目，系统依赖jdk11及以上，从target目录获取aeochaincontract.jar，将其放入待运行程序目录。

2. 在jar包的同级目录新建application.properties文件，并增加配置信息如下：

    contract.exchange.id= 此系统的传输ID
    
    spring.security.oauth2.resourceserver.jwt.public-key-location= jwt证书
    
    spring.rabbitmq.host= rabbitmq主机地址
    
    spring.rabbitmq.password= rabbitmq的用户密码
    
    spring.rabbitmq.port= rabbitmq的端口
    
    spring.rabbitmq.username= rabbitmq的用户名
    
    spring.rabbitmq.virtual-host= rabbitmq的虚拟主机
    
    spring.rabbitmq.template.routing-key= 发送消息的队列名
    
    contract.message.receivequeue= 接收消息的队列名
    
    spring.cloud.nacos.discovery.server-addr= nacos的地址及端口，如ip:port
    
    server.port= web服务的端口
3. 系统运行依赖jdk11及以上版本，Windows采用java -jar aeochaincontract.jar启动程序，linux系统授予aeochaincontract.jar可执行权限，采用./aeochaincontract.jar启动程序。
4. 关于数据存储，程序会在当前目录自动创建数据库文件，默认为test数据库，可以在配置文件中添加spring.datasource.url=jdbc:h2:[路径]/数据库名;FILE_LOCK=FS，当前路径使用.。

NOTICE：
This software is licensed under the GNU Lesser General Public License (LGPL) version 3.0 or later. However, it is not permitted to use this software for commercial purposes without explicit permission from the copyright holder.
If the above restrictions are violated, all commercial profits generated during unauthorized commercial use shall belong to the copyright holder. 
The copyright holder reserves the right to pursue legal liability against infringers through legal means, including but not limited to demanding the cessation of infringement and compensation for losses suffered as a result of infringement.
本软件根据GNU较宽松通用公共许可证（LGPL）3.0或更高版本获得许可。但是，未经版权所有者明确许可，不得将本软件用于商业目的。
若违反上述限制，在未经授权的商业化使用过程中所产生的一切商业收益，均归版权所有者。
版权所有者保留通过法律途径追究侵权者法律责任的权利，包括但不限于要求停止侵权行为、赔偿因侵权行为所遭受的损失等。
