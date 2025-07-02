信贸链合约

1. 使用maven编译项目，编译依赖jdk8，从target目录获取aeochaincontract.jar，将其放入待运行程序目录。

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
3. 系统运行依赖jdk8版本，Windows采用java -jar aeochaincontract.jar启动程序，linux系统授予aeochaincontract.jar可执行权限，采用./aeochaincontract.jar启动程序。
4. 关于数据存储，程序会在当前目录自动创建数据库文件，默认为test数据库，可以在配置文件中添加spring.datasource.url=jdbc:h2:[路径]/数据库名;FILE_LOCK=FS，当前路径使用.。