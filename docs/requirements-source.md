# 项目开发总提示词：AI 增强型宠物门店 O2O 服务预约与客户运营平台

你现在作为我的项目开发工程助手，请基于以下完整需求，帮助我逐步设计并开发一个 **微信小程序版本的宠物门店服务预约与客户运营系统**。

本项目定位为 **简历项目优先，同时具备真实单体宠物店试运营潜力**。不要把它做成简单 CRUD Demo，而是要体现真实业务建模、预约排班、内容运营、AI 辅助和后台管理能力。

---

## 一、项目基本定位

项目名称建议：

**PetCare O2O：AI 增强型宠物门店服务预约与客户运营平台**

或中文名：

**宠伴到家：AI 增强型宠物门店服务预约与客户运营小程序**

项目核心定位：

面向单体宠物门店，提供：

1. 微信小程序端；
2. 后续可扩展 H5 / 响应式网页端；
3. PC 管理后台；
4. 后端 RESTful API；
5. MySQL 数据库；
6. AI 客服、AI 宠物陪伴、AI 发帖辅助、AI 经营分析能力。

项目业务主线：

```text
用户预约宠物服务
→ 商家确认预约
→ 员工根据排班执行到店或上门服务
→ 用户发布宠物日常或服务体验
→ AI 辅助用户聊天、发帖、咨询
→ 商家通过后台管理预约、商品、内容、排班
→ AI 总结经营数据并给出运营建议
```

---

## 二、核心业务优先级

系统优先级如下：

```text
P0：服务预约与真实商家排班
P0：后台管理系统
P1：宠物档案与用户地址
P1：用户社区与内容风控
P1：AI 客服与宠物陪伴助手
P2：商品展示与到店自提订单
P2：AI 经营分析
P3：营销活动分析、会员、优惠券、积分
```

第一版不要追求复杂商业化功能，先完成核心闭环。

---

## 三、技术栈要求

推荐技术栈：

```text
小程序端：
uni-app + Vue3 + Pinia

管理后台：
Vue3 + Vite + Element Plus

后端：
Spring Boot 3
MyBatis-Plus
Sa-Token 或 Spring Security + JWT

数据库：
MySQL 8

缓存：
Redis，V2 再引入，V1 可暂不使用

文件存储：
V1 可本地上传
V2 可迁移到 MinIO / 腾讯云 COS / 阿里云 OSS

AI API：
使用我自己的 AI API 接入
后端需要封装统一 AI Provider Client

部署：
Docker Compose + Nginx + MySQL
```

---

## 四、系统角色

系统至少包含以下角色：

### 1. 小程序用户

可以：

```text
微信登录
维护个人信息
添加宠物档案
添加地址
预约服务
查看预约
浏览商品
提交到店自提订单
发布宠物日常 / 养宠心得
点赞、评论、收藏帖子
使用 AI 客服
使用 AI 宠物陪伴助手
使用 AI 发帖辅助
```

### 2. 管理员 / 商家

可以：

```text
管理门店信息
管理服务项目
管理员工
管理员工技能
管理员工排班
确认 / 拒绝 / 修改服务预约
改派员工
管理商品
处理商品自提订单
审核和处理用户帖子
管理敏感词
处理举报
查看经营数据
使用 AI 经营分析助手
```

### 3. 员工

V1 可以不做独立员工端，只在后台中由管理员维护员工和排班。

后续 V2 可以扩展员工端。

---

## 五、业务规则

### 1. 服务预约规则

服务预约是项目核心业务。

规则如下：

```text
1. 用户提交服务预约后，必须由商家确认。
2. 用户不能指定员工。
3. 系统根据员工技能、排班、不可用时间、已有预约自动推荐可用员工。
4. 后台管理员可以手动改派员工。
5. 洗澡、洗护、美容等服务以到店为主。
6. 上门喂食、上门遛狗等服务以上门为主。
7. 上门服务需要填写地址。
8. 上门服务需要校验用户地址是否在门店 5 到 8 公里服务半径内。
9. 服务半径不写死，放在门店配置中，默认 5km，后台可配置。
10. 第一版不接微信支付。
11. 服务付款方式以到店付款、服务后线下付款为主。
```

预约状态建议：

```text
PENDING_CONFIRM：待商家确认
CONFIRMED：已确认
IN_SERVICE：服务中
COMPLETED：已完成
CANCELLED：已取消
REJECTED：已拒绝
```

支付状态建议：

```text
UNPAID：未支付
OFFLINE_PAID：已线下支付
REFUNDED：已退款
```

付款方式建议：

```text
OFFLINE_STORE：到店付款
OFFLINE_HOME：上门服务后线下付款
ONLINE_WECHAT：微信支付，预留，不实现
FREE：免费
```

---

### 2. 员工排班规则

真实排班是本项目技术亮点之一。

系统需要支持：

```text
员工表
员工技能表
员工工作日排班表
员工不可用时间表
服务预约表
预约状态日志表
```

可预约时间计算逻辑：

```text
用户选择服务项目和日期
→ 获取服务所需时长 duration_minutes
→ 找出具备该服务技能的员工
→ 找出这些员工当天的工作时间
→ 排除午休、请假、临时不可用时间
→ 排除已有预约时间
→ 按 store_config.time_slot_minutes 生成可预约时间段
→ 返回用户可选时间
```

注意：

```text
1. 如果服务需要 90 分钟，那么开始时间 + 90 分钟不能超过员工下班时间。
2. 同一个员工同一时间段不能有两个有效预约。
3. CANCELLED 和 REJECTED 状态的预约不占用排班。
4. PENDING_CONFIRM、CONFIRMED、IN_SERVICE 状态需要占用时间。
```

---

### 3. 上门服务范围规则

上门服务需要校验用户地址与门店距离。

用户地址表和门店表都需要保存：

```text
longitude
latitude
```

第一版可以使用经纬度计算直线距离。

规则：

```text
如果 service_mode = HOME：
    必须选择 address_id
    必须获取用户地址经纬度
    必须获取门店经纬度
    计算 distance_km
    如果 distance_km > store_config.home_service_radius_km：
        拒绝预约
```

错误提示示例：

```text
当前地址距离门店约 9.2 公里，超出本店 5 公里上门服务范围，请更换地址或选择到店服务。
```

---

### 4. 用户社区规则

用户社区是本项目拉新和维系老客的关键模块。

用户可以：

```text
发布宠物日常
发布养宠心得
发布服务体验
上传图片
选择话题
点赞
评论
收藏
举报
```

内容状态建议：

```text
PUBLISHED：已发布
PENDING_REVIEW：待审核
REJECTED：审核拒绝
HIDDEN：已隐藏
DELETED：已删除
```

审核规则：

```text
1. 用户发布内容时先进行敏感词检测。
2. 未命中敏感词：可以直接发布。
3. 命中轻度或中度敏感词：进入待审核。
4. 命中严重敏感词：直接拒绝发布。
5. 管理员可以在后台审核、下架、删除帖子。
6. 评论也需要支持敏感词检测和后台隐藏 / 删除。
7. 用户可以举报帖子，管理员后台处理。
```

敏感词等级：

```text
1：轻度，进入待审核
2：中度，进入待审核并标记风险
3：严重，直接拒绝发布
```

---

### 5. 商品订单规则

商品模块不是核心营收，而是补充营收和门店商品展示。

商品订单第一版采用：

```text
线上下单
到店自提
线下付款
后台确认收款
```

商品订单流程：

```text
用户浏览商品
→ 加入购物车
→ 提交到店自提订单
→ 商家确认订单
→ 商家备货
→ 用户到店付款自提
→ 管理员确认收款
→ 订单完成
```

订单状态建议：

```text
PENDING_CONFIRM：待商家确认
PREPARING：备货中
READY_FOR_PICKUP：待自提
COMPLETED：已完成
CANCELLED：已取消
OUT_OF_STOCK：缺货取消
```

自提状态建议：

```text
WAIT_PREPARE：待备货
READY_FOR_PICKUP：待自提
PICKED_UP：已自提
```

---

### 6. AI 功能规则

项目需要加入 AI 特色，但 AI 不能只是套壳聊天。

AI 模块分为用户端 AI 和管理端 AI。

#### 用户端 AI

包含：

```text
AI 客服助手
AI 宠物陪伴助手
AI 发帖辅助
```

##### AI 客服助手

回答与门店业务相关的问题：

```text
营业时间
门店地址
洗护价格
预约规则
上门服务范围
商品自提规则
取消预约规则
服务注意事项
```

数据来源：

```text
store
store_config
service_item
product
faq_knowledge
```

AI 客服不能编造价格、库存、时间、规则。

##### AI 宠物陪伴助手

用于：

```text
陪用户聊宠物日常
提供基础养宠知识
提供洗护准备建议
解释常见行为问题
给内向用户提供聊天陪伴
```

安全边界：

```text
不能诊断疾病
不能开药
不能替代兽医
不能承诺治疗结果
不能鼓励自行用药
```

遇到以下高风险症状时，必须建议联系宠物医院：

```text
呕吐
抽搐
便血
中毒
呼吸困难
长期不吃不喝
误食异物
严重外伤
高烧
骨折
```

##### AI 发帖辅助

用于帮助用户：

```text
生成宠物日常文案
生成标题
优化表达
降低发帖门槛
提升社区活跃度
```

AI 不应编造用户没有提供的事实。

---

#### 管理端 AI

包含：

```text
AI 经营分析助手
AI 社区洞察
AI 销售分析
AI 活动复盘
AI 营销建议
```

后台 AI 分析原则：

```text
后端负责 SQL 聚合统计
AI 负责解释数据和生成建议
不要让 AI 直接查数据库
不要让 AI 编造不存在的数据
```

AI 可以分析：

```text
最近用户在社区聊什么
哪个服务预约最多
哪个时间段最忙
哪个商品卖得最好
用户什么时候买东西
哪个活动提升了销量或营业额
下个月适合做什么活动
周末是否需要增加员工排班
```

需要设计营销活动表，用于后续分析活动效果。

---

## 六、数据库表结构设计要求

请先帮我编写 MySQL 8 的建表 SQL。

要求：

```text
1. 使用 InnoDB。
2. 字符集使用 utf8mb4。
3. 主键使用 BIGINT。
4. 金额使用 DECIMAL(10,2)。
5. 经纬度使用 DECIMAL(10,6)。
6. 大部分业务表包含 create_time、update_time、deleted。
7. 状态字段使用 VARCHAR(32)，方便阅读。
8. 表名使用小写下划线。
9. 字段名使用小写下划线。
10. 为高频查询字段增加索引。
11. 重要唯一字段增加唯一索引。
12. 保留逻辑删除字段 deleted。
13. SQL 中加入必要 COMMENT。
```

---

## 七、需要设计的数据库表

### A. 用户与宠物模块

```text
user
pet
user_address
```

#### user

字段：

```text
id BIGINT PRIMARY KEY
openid VARCHAR(128) UNIQUE
unionid VARCHAR(128)
nickname VARCHAR(64)
avatar_url VARCHAR(255)
phone VARCHAR(20)
gender TINYINT
status VARCHAR(32)
last_login_time DATETIME
create_time DATETIME
update_time DATETIME
deleted TINYINT
```

#### pet

字段：

```text
id BIGINT PRIMARY KEY
user_id BIGINT
name VARCHAR(64)
type VARCHAR(32) -- DOG / CAT / OTHER
breed VARCHAR(64)
gender TINYINT
age DECIMAL(4,1)
weight DECIMAL(5,2)
size VARCHAR(32) -- SMALL / MEDIUM / LARGE
sterilized TINYINT
avatar_url VARCHAR(255)
remark VARCHAR(500)
create_time DATETIME
update_time DATETIME
deleted TINYINT
```

#### user_address

字段：

```text
id BIGINT PRIMARY KEY
user_id BIGINT
contact_name VARCHAR(64)
contact_phone VARCHAR(20)
province VARCHAR(64)
city VARCHAR(64)
district VARCHAR(64)
detail_address VARCHAR(255)
longitude DECIMAL(10,6)
latitude DECIMAL(10,6)
is_default TINYINT
create_time DATETIME
update_time DATETIME
deleted TINYINT
```

---

### B. 门店与配置模块

```text
store
store_config
```

#### store

字段：

```text
id BIGINT PRIMARY KEY
store_name VARCHAR(100)
phone VARCHAR(20)
address VARCHAR(255)
longitude DECIMAL(10,6)
latitude DECIMAL(10,6)
business_hours VARCHAR(100)
status VARCHAR(32) -- OPEN / CLOSED
description VARCHAR(500)
create_time DATETIME
update_time DATETIME
deleted TINYINT
```

#### store_config

字段：

```text
id BIGINT PRIMARY KEY
store_id BIGINT
home_service_radius_km DECIMAL(5,2)
booking_advance_days INT
booking_cancel_hours INT
time_slot_minutes INT
auto_confirm_booking TINYINT
content_auto_publish TINYINT
create_time DATETIME
update_time DATETIME
```

默认建议：

```text
home_service_radius_km = 5.00
booking_advance_days = 14
booking_cancel_hours = 4
time_slot_minutes = 30
auto_confirm_booking = 0
content_auto_publish = 1
```

---

### C. 服务与员工模块

```text
service_category
service_item
staff
staff_skill
```

#### service_category

字段：

```text
id BIGINT PRIMARY KEY
name VARCHAR(64)
icon_url VARCHAR(255)
sort INT
status VARCHAR(32)
create_time DATETIME
update_time DATETIME
deleted TINYINT
```

#### service_item

字段：

```text
id BIGINT PRIMARY KEY
category_id BIGINT
name VARCHAR(100)
service_mode VARCHAR(32) -- STORE / HOME / BOTH
price DECIMAL(10,2)
duration_minutes INT
pet_type VARCHAR(32) -- DOG / CAT / ALL
pet_size VARCHAR(32) -- SMALL / MEDIUM / LARGE / ALL
need_address TINYINT
need_pet TINYINT
description TEXT
cover_url VARCHAR(255)
status VARCHAR(32) -- ON_SALE / OFF_SALE
sort INT
create_time DATETIME
update_time DATETIME
deleted TINYINT
```

#### staff

字段：

```text
id BIGINT PRIMARY KEY
store_id BIGINT
name VARCHAR(64)
phone VARCHAR(20)
avatar_url VARCHAR(255)
role VARCHAR(32) -- GROOMER / WALKER / FEEDER / MANAGER
status VARCHAR(32) -- ACTIVE / INACTIVE
description VARCHAR(500)
create_time DATETIME
update_time DATETIME
deleted TINYINT
```

#### staff_skill

字段：

```text
id BIGINT PRIMARY KEY
staff_id BIGINT
service_category_id BIGINT
create_time DATETIME
```

唯一索引：

```text
staff_id + service_category_id
```

---

### D. 排班与预约模块

```text
staff_schedule
staff_unavailable_time
service_booking
booking_status_log
```

#### staff_schedule

字段：

```text
id BIGINT PRIMARY KEY
staff_id BIGINT
store_id BIGINT
work_date DATE
start_time TIME
end_time TIME
status VARCHAR(32) -- AVAILABLE / UNAVAILABLE
remark VARCHAR(255)
create_time DATETIME
update_time DATETIME
deleted TINYINT
```

#### staff_unavailable_time

字段：

```text
id BIGINT PRIMARY KEY
staff_id BIGINT
unavailable_date DATE
start_time TIME
end_time TIME
reason_type VARCHAR(32) -- LUNCH / LEAVE / TEMP
reason VARCHAR(255)
create_time DATETIME
update_time DATETIME
deleted TINYINT
```

#### service_booking

字段：

```text
id BIGINT PRIMARY KEY
booking_no VARCHAR(64) UNIQUE
user_id BIGINT
pet_id BIGINT
store_id BIGINT
service_item_id BIGINT
staff_id BIGINT
service_mode VARCHAR(32) -- STORE / HOME
booking_date DATE
start_time TIME
end_time TIME
address_id BIGINT
distance_km DECIMAL(6,2)
contact_name VARCHAR(64)
contact_phone VARCHAR(20)
price DECIMAL(10,2)
payment_method VARCHAR(32)
payment_status VARCHAR(32)
status VARCHAR(32)
remark VARCHAR(500)
merchant_remark VARCHAR(500)
confirm_time DATETIME
complete_time DATETIME
cancel_time DATETIME
cancel_reason VARCHAR(255)
create_time DATETIME
update_time DATETIME
deleted TINYINT
```

#### booking_status_log

字段：

```text
id BIGINT PRIMARY KEY
booking_id BIGINT
old_status VARCHAR(32)
new_status VARCHAR(32)
operator_type VARCHAR(32) -- USER / ADMIN / SYSTEM
operator_id BIGINT
remark VARCHAR(500)
create_time DATETIME
```

---

### E. 社区与内容模块

```text
topic
post
post_image
post_comment
post_like
post_favorite
post_report
```

#### topic

字段：

```text
id BIGINT PRIMARY KEY
name VARCHAR(64)
description VARCHAR(255)
sort INT
status VARCHAR(32)
create_time DATETIME
update_time DATETIME
deleted TINYINT
```

#### post

字段：

```text
id BIGINT PRIMARY KEY
user_id BIGINT
pet_id BIGINT
topic_id BIGINT
title VARCHAR(120)
content TEXT
status VARCHAR(32)
risk_level TINYINT
view_count INT
like_count INT
comment_count INT
favorite_count INT
reject_reason VARCHAR(255)
publish_time DATETIME
create_time DATETIME
update_time DATETIME
deleted TINYINT
```

#### post_image

字段：

```text
id BIGINT PRIMARY KEY
post_id BIGINT
image_url VARCHAR(255)
sort INT
create_time DATETIME
```

#### post_comment

字段：

```text
id BIGINT PRIMARY KEY
post_id BIGINT
user_id BIGINT
parent_id BIGINT
content VARCHAR(1000)
status VARCHAR(32)
risk_level TINYINT
like_count INT
create_time DATETIME
update_time DATETIME
deleted TINYINT
```

#### post_like

字段：

```text
id BIGINT PRIMARY KEY
post_id BIGINT
user_id BIGINT
create_time DATETIME
```

唯一索引：

```text
post_id + user_id
```

#### post_favorite

字段：

```text
id BIGINT PRIMARY KEY
post_id BIGINT
user_id BIGINT
create_time DATETIME
```

唯一索引：

```text
post_id + user_id
```

#### post_report

字段：

```text
id BIGINT PRIMARY KEY
post_id BIGINT
reporter_id BIGINT
reason_type VARCHAR(32) -- SPAM / ILLEGAL / ABUSE / OTHER
reason VARCHAR(500)
status VARCHAR(32) -- PENDING / PROCESSED / IGNORED
handle_result VARCHAR(500)
handler_id BIGINT
handle_time DATETIME
create_time DATETIME
```

---

### F. 敏感词与审核模块

```text
sensitive_word
content_review_record
```

#### sensitive_word

字段：

```text
id BIGINT PRIMARY KEY
word VARCHAR(100)
category VARCHAR(32)
level TINYINT
status VARCHAR(32)
create_time DATETIME
update_time DATETIME
deleted TINYINT
```

#### content_review_record

字段：

```text
id BIGINT PRIMARY KEY
content_type VARCHAR(32) -- POST / COMMENT
content_id BIGINT
user_id BIGINT
risk_level TINYINT
matched_words VARCHAR(500)
review_status VARCHAR(32) -- PENDING / APPROVED / REJECTED
reviewer_id BIGINT
review_remark VARCHAR(500)
create_time DATETIME
review_time DATETIME
```

---

### G. 商品与自提订单模块

```text
product_category
product
product_image
cart_item
product_order
product_order_item
```

#### product_category

字段：

```text
id BIGINT PRIMARY KEY
name VARCHAR(64)
icon_url VARCHAR(255)
sort INT
status VARCHAR(32)
create_time DATETIME
update_time DATETIME
deleted TINYINT
```

#### product

字段：

```text
id BIGINT PRIMARY KEY
category_id BIGINT
name VARCHAR(100)
cover_url VARCHAR(255)
price DECIMAL(10,2)
stock INT
sales_count INT
description TEXT
pickup_only TINYINT
status VARCHAR(32)
sort INT
create_time DATETIME
update_time DATETIME
deleted TINYINT
```

#### product_image

字段：

```text
id BIGINT PRIMARY KEY
product_id BIGINT
image_url VARCHAR(255)
sort INT
create_time DATETIME
```

#### cart_item

字段：

```text
id BIGINT PRIMARY KEY
user_id BIGINT
product_id BIGINT
quantity INT
checked TINYINT
create_time DATETIME
update_time DATETIME
```

唯一索引：

```text
user_id + product_id
```

#### product_order

字段：

```text
id BIGINT PRIMARY KEY
order_no VARCHAR(64) UNIQUE
user_id BIGINT
store_id BIGINT
total_amount DECIMAL(10,2)
payment_method VARCHAR(32)
payment_status VARCHAR(32)
pickup_status VARCHAR(32)
status VARCHAR(32)
contact_name VARCHAR(64)
contact_phone VARCHAR(20)
remark VARCHAR(500)
merchant_remark VARCHAR(500)
confirm_time DATETIME
complete_time DATETIME
cancel_time DATETIME
create_time DATETIME
update_time DATETIME
deleted TINYINT
```

#### product_order_item

字段：

```text
id BIGINT PRIMARY KEY
order_id BIGINT
product_id BIGINT
product_name VARCHAR(100)
product_cover_url VARCHAR(255)
price DECIMAL(10,2)
quantity INT
total_amount DECIMAL(10,2)
create_time DATETIME
```

---

### H. 营销活动模块

```text
marketing_activity
activity_product
activity_service
```

#### marketing_activity

字段：

```text
id BIGINT PRIMARY KEY
title VARCHAR(100)
activity_type VARCHAR(32) -- SERVICE / PRODUCT / COMMUNITY / MIXED
description TEXT
start_time DATETIME
end_time DATETIME
status VARCHAR(32) -- DRAFT / ACTIVE / ENDED / CANCELLED
create_time DATETIME
update_time DATETIME
deleted TINYINT
```

#### activity_product

字段：

```text
id BIGINT PRIMARY KEY
activity_id BIGINT
product_id BIGINT
create_time DATETIME
```

#### activity_service

字段：

```text
id BIGINT PRIMARY KEY
activity_id BIGINT
service_item_id BIGINT
create_time DATETIME
```

---

### I. AI 模块

```text
ai_conversation
ai_message
ai_usage_log
ai_analysis_report
faq_knowledge
```

#### ai_conversation

字段：

```text
id BIGINT PRIMARY KEY
user_id BIGINT
admin_id BIGINT
conversation_type VARCHAR(32) -- CUSTOMER_SERVICE / PET_CHAT / ADMIN_ANALYSIS
title VARCHAR(100)
create_time DATETIME
update_time DATETIME
deleted TINYINT
```

#### ai_message

字段：

```text
id BIGINT PRIMARY KEY
conversation_id BIGINT
role VARCHAR(32) -- system / user / assistant
content TEXT
token_count INT
create_time DATETIME
```

#### ai_usage_log

字段：

```text
id BIGINT PRIMARY KEY
user_id BIGINT
admin_id BIGINT
api_type VARCHAR(32) -- CHAT / CUSTOMER_SERVICE / CONTENT_GENERATE / ANALYSIS
model_name VARCHAR(100)
prompt_tokens INT
completion_tokens INT
total_tokens INT
success TINYINT
error_message VARCHAR(1000)
create_time DATETIME
```

#### ai_analysis_report

字段：

```text
id BIGINT PRIMARY KEY
report_type VARCHAR(32) -- BUSINESS / COMMUNITY / SALES / ACTIVITY
start_date DATE
end_date DATE
raw_data_json JSON
ai_summary TEXT
suggestions TEXT
created_by BIGINT
create_time DATETIME
```

#### faq_knowledge

字段：

```text
id BIGINT PRIMARY KEY
question VARCHAR(255)
answer TEXT
category VARCHAR(32) -- STORE / BOOKING / SERVICE / PRODUCT / AFTER_SALE
status VARCHAR(32)
create_time DATETIME
update_time DATETIME
deleted TINYINT
```

---

### J. 后台管理模块

```text
admin_user
admin_operation_log
```

#### admin_user

字段：

```text
id BIGINT PRIMARY KEY
username VARCHAR(64) UNIQUE
password VARCHAR(255)
nickname VARCHAR(64)
phone VARCHAR(20)
role VARCHAR(32) -- SUPER_ADMIN / MANAGER / STAFF
status VARCHAR(32)
last_login_time DATETIME
create_time DATETIME
update_time DATETIME
deleted TINYINT
```

#### admin_operation_log

字段：

```text
id BIGINT PRIMARY KEY
admin_id BIGINT
module VARCHAR(64)
operation VARCHAR(100)
request_method VARCHAR(16)
request_url VARCHAR(255)
request_params TEXT
ip VARCHAR(64)
result VARCHAR(32)
error_message VARCHAR(1000)
create_time DATETIME
```

---

## 八、请你第一步完成的任务

请你先不要写前端，也不要直接写全部业务代码。

第一步请完成：

```text
1. 创建 MySQL 数据库初始化 SQL 文件。
2. 文件名建议：schema.sql。
3. 根据上面的表结构生成完整建表 SQL。
4. 每张表添加 COMMENT。
5. 每个重要字段添加 COMMENT。
6. 为常用查询字段添加索引。
7. 为唯一字段添加唯一索引。
8. 给状态字段设置合理默认值。
9. 给 create_time 设置默认 CURRENT_TIMESTAMP。
10. 给 update_time 设置 DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP。
11. 给 deleted 设置默认 0。
```

输出要求：

```text
请生成完整 schema.sql。
不要省略表。
不要只给片段。
不要改变当前业务设计。
如果你认为某些字段需要微调，请先说明原因，再给出 SQL。
```

---

## 九、第二步任务预告

完成 schema.sql 后，下一步再继续做：

```text
1. Spring Boot 项目结构设计。
2. application.yml 配置。
3. MyBatis-Plus 实体类。
4. Mapper、Service、Controller 分层。
5. 用户登录模块。
6. 服务预约与排班可用时间计算模块。
7. 内容敏感词审核模块。
8. 商品自提订单模块。
9. AI API 统一封装模块。
10. 管理后台 API。
```

---

## 十、项目核心亮点要求

开发时必须保留以下亮点：

```text
1. 真实商家排班：
   通过员工技能、工作时间、不可用时间、服务时长和已有预约动态计算可预约时间。

2. 上门服务范围校验：
   基于门店经纬度和用户地址经纬度计算距离，限制 5 到 8 公里服务范围。

3. 用户社区内容运营：
   支持用户发帖、评论、点赞、收藏、举报，形成客户维护和拉新闭环。

4. 内容风控：
   通过敏感词、风险等级、审核记录、后台处理机制控制内容风险。

5. 商品轻电商：
   支持商品展示、购物车、到店自提订单、线下付款和后台确认收款。

6. AI 用户助手：
   支持 AI 客服、AI 宠物陪伴、AI 发帖辅助。

7. AI 经营分析：
   后端先聚合统计服务、商品、社区、活动数据，再由 AI 生成经营总结和运营建议。

8. 真实门店经营思维：
   系统不是简单 Demo，而是围绕单体宠物店实际经营进行建模。
```

请从 `schema.sql` 开始实现。
