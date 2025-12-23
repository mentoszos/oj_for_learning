create table question
(
    id           int auto_increment comment '主键id'
        primary key,
    tag          varchar(512)     null comment '题目标签列表，json字符串',
    submit_num   int default 0    not null comment '该题的提交数',
    accepted_num int default 0    not null comment '通过数',
    user_id      int              not null comment '创建者id',
    time_limit   int default 1000 not null comment '时间限制，单位ms，默认1000',
    memory_limit int default 256  not null comment '内存限制，单位MB，默认256',
    title        varchar(512)     not null comment '题目标题',
    create_time  timestamp        null comment '创建时间',
    update_time  timestamp        null comment '更新时间'
)
    comment '题目表';

create table question_info
(
    id          int auto_increment comment '主键id'
        primary key,
    question_id int       not null comment '题目id',
    content     text      not null comment '题目内容',
    create_time timestamp null comment '创建时间',
    update_time timestamp null comment '更新时间'
)
    comment '题目详情';

create table question_solution
(
    id          bigint auto_increment comment '主键id'
        primary key,
    content     mediumint null comment '题解内容',
    question_id int       not null comment '题目id',
    user_id     int       not null comment '提交题解的用户id'
)
    comment '题解';

create table question_submit
(
    id            bigint auto_increment comment '主键id'
        primary key,
    question_id   int               not null comment '题目id',
    user_id       int               not null comment '提交用户id',
    status        tinyint default 0 not null comment '判题状态 0-未提交 1-判题中 2-已通过 3-判题失败',
    judge_info    json              null comment '判题结果详情，json存储{usercase：1 time：1200 memory：256 "message": "Runtime Error", "pass_count": 5, "total_count": 10}}',
    create_time   timestamp         null comment '本条记录创建时间',
    update_time   timestamp         null comment '本条记录更新时间',
    sumbit_code   text              null comment '提交的代码',
    code_language varchar(10)       null comment '代码类型，c、java'
)
    comment '题目提交记录';

create table question_usecase
(
    question_id int       not null comment '题目id',
    id          int auto_increment comment '主键id'
        primary key,
    input       text      null comment '测试用例输入',
    output      text      null comment '测试用例输出',
    create_time timestamp null comment '创建时间',
    update_time timestamp null comment '更新时间'
)
    comment '题目测试用例';

create index index_question_id
    on question_usecase (question_id);


