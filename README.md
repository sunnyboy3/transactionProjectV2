# transactionProjectV2
说明：事务补偿，目前初始版本只支持post请求，请求类型:application/json，springCloud

主要sql脚本
    
    create table transaction_project
    (
        id                serial       not null
        constraint transaction_project_pk
        primary key,
        project_name      varchar(100) not null,
        transaction_group varchar(100) not null,
        feign_client_name varchar(100)
    );

    create table transaction_request_logs
    (
        id                serial            not null
        constraint transaction_request_logs_pk
        primary key,
        trace_id          varchar(100)      not null,
        project_name      varchar(100)      not null,
        in_param          varchar,
        out_param         varchar,
        status            integer default 0 not null,
        group_name        varchar(100),
        feign_client_name varchar(100),
        sort              integer
    );

测试数据脚本

    create table transaction_user
    (
        id       serial  not null
        constraint transaction_user_pk
        primary key,
        username varchar not null,
        age      integer not null
    );

测试用例 
    ![流程图02](https://user-images.githubusercontent.com/13884959/196380236-31d592c3-6a71-4cde-bccc-162f81d683a9.png)

    案例1：
    访问projectD应用失败
    
    http://localhost:9003/gateway/test
    Content-Type:application/json
    {
        "username":"wangwu",
        "age":10,
        "flag":3
    }

    补偿事务
    http://localhost:9003/gateway/test
    Content-Type:application/json
    transaction-trace-id:xxxx     #注意这里使用表中trance_id
    {"username":"wangwu","age":10,"flag":0}
