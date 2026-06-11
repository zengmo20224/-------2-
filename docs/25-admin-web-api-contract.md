# 管理后台 API 契约清单

日期：2026-06-11
来源：后端真实 Controller、DTO 和 `@PreAuthorize` 注解
任务包：10F-R2A

> 本文档由后端代码实证生成，不包含从前端反推的假设接口。
> 所有清单项均可通过 Controller 类名和方法名定位到真实代码。

## 契约问题汇总

| 级别 | 问题 | 阻塞项 |
|------|------|--------|
| CRITICAL | 雪花 BIGINT ID 精度风险：后端 `Long` → 前端 `number` | D-011 |
| HIGH | 固定 `STORE_ID = 1` 与雪花 ID 不兼容 | D-012 |
| HIGH | `getStaffSkills` 伪接口：后端无 GET 读取 | 无 |
| MEDIUM | `ServiceItemQueryParams.name` 后端不支持 | 无 |
| MEDIUM | `StoreUpdateParams` 全部可选 vs 后端必填 | 无 |
| MEDIUM | `PostReport` 前端缺少 `reasonType`、`handlerId`，多出 `handleRemark` | 无 |

---

## 1. 管理员认证

### 1.1 管理员登录

| 字段 | 值 |
|------|-----|
| HTTP | `POST /api/v1/admin/auth/login` |
| Controller | `AdminAuthController.login()` |
| 权限码 | 无（公开接口） |
| 请求 DTO | `AdminLoginRequest` |
| 响应 DTO | `AdminLoginResponse` |
| 前端函数 | `auth.login()` |
| 状态 | 已接入 |

**AdminLoginRequest 字段：**

| 字段 | 类型 | 必填 | 约束 |
|------|------|------|------|
| username | String | 是 | @NotBlank, @Size(3..64) |
| password | String | 是 | @NotBlank, @Size(8..128) |

**AdminLoginResponse 字段：**

| 字段 | 类型 | 可空 | 说明 |
|------|------|------|------|
| tokenType | String | 否 | |
| accessToken | String | 否 | |
| expiresInSeconds | int | 否 | |
| admin.id | Long | 否 | 雪花 ID |
| admin.username | String | 否 | |
| admin.nickname | String | 是 | @JsonInclude NON_NULL |
| admin.role | String | 是 | @JsonInclude NON_NULL |

### 1.2 获取当前管理员信息

| 字段 | 值 |
|------|-----|
| HTTP | `GET /api/v1/admin/auth/me` |
| Controller | `AdminAuthController.me()` |
| 权限码 | 无显式 @PreAuthorize（需 Bearer Token 认证） |
| 请求 DTO | 无 |
| 响应 DTO | `AdminMeResponse` |
| 前端函数 | `auth.getUserInfo()` |
| 状态 | 已接入 |

**AdminMeResponse 字段：**

| 字段 | 类型 | 可空 | 说明 |
|------|------|------|------|
| id | Long | 否 | 雪花 ID |
| username | String | 否 | |
| nickname | String | 是 | @JsonInclude NON_NULL |
| role | String | 是 | @JsonInclude NON_NULL |
| permissions | List\<String\> | 是 | @JsonInclude NON_NULL |

**契约风险：** 响应不包含 `storeId`，前端无法确定当前管理员关联的门店。见 D-012。

---

## 2. 门店信息和配置

> D-012 未决：前端当前固定 `STORE_ID = 1`。所有门店接口的路径 ID 来源未确定。

### 2.1 获取门店详情

| 字段 | 值 |
|------|-----|
| HTTP | `GET /api/v1/admin/stores/{id}` |
| Controller | `AdminManagementController.getStore()` |
| 权限码 | `store:info:read` |
| 路径参数 | `id: Long`（门店 ID） |
| 请求 DTO | 无 |
| 响应 DTO | `StoreView` |
| 前端函数 | `store.getStoreInfo()`（固定 STORE_ID=1） |
| 状态 | 已接入（但门店上下文阻塞） |

**StoreView 字段：**

| 字段 | 类型 | 可空 | 说明 |
|------|------|------|------|
| id | Long | 否 | 雪花 ID |
| storeName | String | 否 | |
| phone | String | 是 | |
| address | String | 是 | |
| longitude | BigDecimal | 是 | |
| latitude | BigDecimal | 是 | |
| businessHours | String | 是 | |
| status | String | 否 | OPEN / CLOSED |
| description | String | 是 | |

### 2.2 更新门店信息

| 字段 | 值 |
|------|-----|
| HTTP | `PATCH /api/v1/admin/stores/{id}` |
| Controller | `AdminManagementController.updateStore()` |
| 权限码 | `store:info:update` |
| 路径参数 | `id: Long`（门店 ID） |
| 请求 DTO | `StoreUpdateRequest` |
| 响应 DTO | `StoreView` |
| 前端函数 | `store.updateStoreInfo()`（固定 STORE_ID=1） |
| 状态 | 已接入（但门店上下文阻塞） |

**StoreUpdateRequest 字段：**

| 字段 | 类型 | 必填 | 约束 |
|------|------|------|------|
| storeName | String | **是** | @NotBlank, @Size(max=100) |
| phone | String | 否 | @Size(max=20) |
| address | String | 否 | @Size(max=255) |
| longitude | BigDecimal | 否 | |
| latitude | BigDecimal | 否 | |
| businessHours | String | 否 | @Size(max=100) |
| status | String | **是** | @NotBlank, @Pattern("OPEN\|CLOSED") |
| description | String | 否 | @Size(max=500) |

**契约问题：** 前端 `StoreUpdateParams` 将所有字段定义为可选（`storeName?`, `status?`），但后端 `storeName` 和 `status` 标注 `@NotBlank`，为必填。前端允许构造后端必然拒绝的请求。

### 2.3 获取门店配置

| 字段 | 值 |
|------|-----|
| HTTP | `GET /api/v1/admin/stores/{id}/config` |
| Controller | `AdminManagementController.getStoreConfig()` |
| 权限码 | `store:config:read` |
| 路径参数 | `id: Long`（门店 ID） |
| 请求 DTO | 无 |
| 响应 DTO | `StoreConfigView` |
| 前端函数 | `store.getStoreConfig()`（固定 STORE_ID=1） |
| 状态 | 已接入（但门店上下文阻塞） |

**StoreConfigView 字段：**

| 字段 | 类型 | 可空 | 说明 |
|------|------|------|------|
| id | Long | 否 | 雪花 ID |
| storeId | Long | 否 | 关联门店雪花 ID |
| homeServiceRadiusKm | BigDecimal | 否 | |
| bookingAdvanceDays | Integer | 否 | |
| bookingCancelHours | Integer | 否 | |
| timeSlotMinutes | Integer | 否 | |
| autoConfirmBooking | Boolean | 否 | |
| contentAutoPublish | Boolean | 否 | |

### 2.4 更新门店配置

| 字段 | 值 |
|------|-----|
| HTTP | `PUT /api/v1/admin/stores/{id}/config` |
| Controller | `AdminManagementController.updateStoreConfig()` |
| 权限码 | `store:config:update` |
| 路径参数 | `id: Long`（门店 ID） |
| 请求 DTO | `StoreConfigUpdateRequest` |
| 响应 DTO | `StoreConfigView` |
| 前端函数 | `store.updateStoreConfig()`（固定 STORE_ID=1） |
| 状态 | 已接入（但门店上下文阻塞） |

**StoreConfigUpdateRequest 字段：**

| 字段 | 类型 | 必填 | 约束 |
|------|------|------|------|
| homeServiceRadiusKm | BigDecimal | **是** | @NotNull, @DecimalMin("0.1") |
| bookingAdvanceDays | Integer | **是** | @NotNull, @Min(1), @Max(365) |
| bookingCancelHours | Integer | **是** | @NotNull, @Min(0), @Max(168) |
| timeSlotMinutes | Integer | **是** | @NotNull, @Min(5), @Max(240) |
| autoConfirmBooking | Boolean | **是** | @NotNull |
| contentAutoPublish | Boolean | **是** | @NotNull |

---

## 3. 服务项目

### 3.1 服务项目列表

| 字段 | 值 |
|------|-----|
| HTTP | `GET /api/v1/admin/service-items` |
| Controller | `AdminManagementController.listServiceItems()` |
| 权限码 | `service:item:read` |
| 查询参数 | `page: int(默认1)`, `size: int(默认20)`, `status: String(可选)` |
| 响应 DTO | `PageResponse<ServiceItemView>` |
| 前端函数 | `service.getServiceItems()` |
| 状态 | 已接入 |

**契约问题：** 前端 `ServiceItemQueryParams` 包含 `name` 字段，后端不支持此筛选参数。前端展示无效筛选。

**ServiceItemView 字段：**

| 字段 | 类型 | 可空 | 说明 |
|------|------|------|------|
| id | Long | 否 | 雪花 ID |
| categoryId | Long | 否 | 关联分类雪花 ID |
| name | String | 否 | |
| serviceMode | String | 否 | STORE / HOME / BOTH |
| price | BigDecimal | 否 | |
| durationMinutes | Integer | 否 | |
| petType | String | 是 | DOG / CAT / ALL |
| petSize | String | 是 | SMALL / MEDIUM / LARGE / ALL |
| needAddress | Boolean | 否 | |
| needPet | Boolean | 否 | |
| description | String | 是 | |
| coverUrl | String | 是 | |
| status | String | 否 | ACTIVE / DISABLED |
| sort | Integer | 是 | |

### 3.2 创建服务项目

| 字段 | 值 |
|------|-----|
| HTTP | `POST /api/v1/admin/service-items` |
| Controller | `AdminManagementController.createServiceItem()` |
| 权限码 | `service:item:create` |
| 请求 DTO | `ServiceItemRequest` |
| 响应 DTO | `ServiceItemView` |
| 前端函数 | `service.createServiceItem()` |
| 状态 | 已接入 |

**ServiceItemRequest 字段：**

| 字段 | 类型 | 必填 | 约束 |
|------|------|------|------|
| categoryId | Long | **是** | @NotNull, @Positive |
| name | String | **是** | @NotBlank, @Size(max=100) |
| serviceMode | String | **是** | @NotBlank, @Pattern("STORE\|HOME\|BOTH") |
| price | BigDecimal | **是** | @NotNull, @DecimalMin("0.0") |
| durationMinutes | Integer | **是** | @NotNull, @Positive |
| petType | String | 否 | @Pattern("DOG\|CAT\|ALL") |
| petSize | String | 否 | @Pattern("SMALL\|MEDIUM\|LARGE\|ALL") |
| needAddress | Boolean | **是** | @NotNull |
| needPet | Boolean | **是** | @NotNull |
| description | String | 否 | |
| coverUrl | String | 否 | @Size(max=255) |
| sort | Integer | 否 | @Min(0) |

### 3.3 更新服务项目

| 字段 | 值 |
|------|-----|
| HTTP | `PUT /api/v1/admin/service-items/{id}` |
| Controller | `AdminManagementController.updateServiceItem()` |
| 权限码 | `service:item:update` |
| 路径参数 | `id: Long` |
| 请求 DTO | `ServiceItemRequest`（同 3.2） |
| 响应 DTO | `ServiceItemView` |
| 前端函数 | `service.updateServiceItem()` |
| 状态 | 已接入 |

### 3.4 停用服务项目

| 字段 | 值 |
|------|-----|
| HTTP | `POST /api/v1/admin/service-items/{id}/disable` |
| Controller | `AdminManagementController.disableServiceItem()` |
| 权限码 | `service:item:disable` |
| 路径参数 | `id: Long` |
| 请求 DTO | 无 |
| 响应 DTO | `ServiceItemView` |
| 前端函数 | `service.disableServiceItem()` |
| 状态 | 已接入 |

---

## 4. 员工、技能和排班

### 4.1 员工列表

| 字段 | 值 |
|------|-----|
| HTTP | `GET /api/v1/admin/staff` |
| Controller | `AdminManagementController.listStaff()` |
| 权限码 | `staff:profile:read` |
| 查询参数 | `page: int(默认1)`, `size: int(默认20)`, `status: String(可选)` |
| 响应 DTO | `PageResponse<StaffView>` |
| 前端函数 | `staff.getStaffList()` |
| 状态 | 已接入 |

**StaffView 字段：**

| 字段 | 类型 | 可空 | 说明 |
|------|------|------|------|
| id | Long | 否 | 雪花 ID |
| storeId | Long | 否 | 关联门店雪花 ID |
| name | String | 否 | |
| phone | String | 是 | |
| avatarUrl | String | 是 | |
| role | String | 否 | GROOMER / WALKER / FEEDER / MANAGER |
| status | String | 否 | |
| description | String | 是 | |

### 4.2 创建员工

| 字段 | 值 |
|------|-----|
| HTTP | `POST /api/v1/admin/staff` |
| Controller | `AdminManagementController.createStaff()` |
| 权限码 | `staff:profile:create` |
| 请求 DTO | `StaffRequest` |
| 响应 DTO | `StaffView` |
| 前端函数 | `staff.createStaff()` |
| 状态 | 已接入 |

**StaffRequest 字段：**

| 字段 | 类型 | 必填 | 约束 |
|------|------|------|------|
| storeId | Long | **是** | @NotNull, @Positive |
| name | String | **是** | @NotBlank, @Size(max=64) |
| phone | String | 否 | @Size(max=20) |
| avatarUrl | String | 否 | @Size(max=255) |
| role | String | **是** | @NotBlank, @Pattern("GROOMER\|WALKER\|FEEDER\|MANAGER") |
| description | String | 否 | @Size(max=500) |

### 4.3 更新员工

| 字段 | 值 |
|------|-----|
| HTTP | `PUT /api/v1/admin/staff/{id}` |
| Controller | `AdminManagementController.updateStaff()` |
| 权限码 | `staff:profile:update` |
| 路径参数 | `id: Long` |
| 请求 DTO | `StaffRequest`（同 4.2） |
| 响应 DTO | `StaffView` |
| 前端函数 | `staff.updateStaff()` |
| 状态 | 已接入 |

### 4.4 停用员工

| 字段 | 值 |
|------|-----|
| HTTP | `POST /api/v1/admin/staff/{id}/disable` |
| Controller | `AdminManagementController.disableStaff()` |
| 权限码 | `staff:profile:disable` |
| 路径参数 | `id: Long` |
| 请求 DTO | 无 |
| 响应 DTO | `StaffView` |
| 前端函数 | `staff.disableStaff()` |
| 状态 | 已接入 |

### 4.5 替换员工技能

| 字段 | 值 |
|------|-----|
| HTTP | `PUT /api/v1/admin/staff/{id}/skills` |
| Controller | `AdminManagementController.replaceStaffSkills()` |
| 权限码 | `staff:skill:manage` |
| 路径参数 | `id: Long`（员工 ID） |
| 请求 DTO | `StaffSkillUpdateRequest` |
| 响应 DTO | `StaffSkillView` |
| 前端函数 | `staff.updateStaffSkills()` |
| 状态 | 未接入 |

**StaffSkillUpdateRequest 字段：**

| 字段 | 类型 | 必填 | 约束 |
|------|------|------|------|
| serviceCategoryIds | List\<Long\> | **是** | @NotNull, 元素 @NotNull @Positive |

**StaffSkillView 字段：**

| 字段 | 类型 | 可空 | 说明 |
|------|------|------|------|
| staffId | Long | 否 | 雪花 ID |
| serviceCategoryIds | List\<Long\> | 否 | |

**契约问题：** 后端不存在 `GET /api/v1/admin/staff/{id}/skills`。前端 `staff.getStaffSkills()` 是伪接口。缺少读取接口意味着无法在编辑页面回显当前技能。

### 4.6 员工排班列表

| 字段 | 值 |
|------|-----|
| HTTP | `GET /api/v1/admin/staff/{id}/schedules` |
| Controller | `AdminManagementController.listSchedules()` |
| 权限码 | `staff:schedule:read` |
| 路径参数 | `id: Long`（员工 ID） |
| 查询参数 | `page: int(默认1)`, `size: int(默认20)` |
| 响应 DTO | `PageResponse<StaffScheduleView>` |
| 前端函数 | `staff.getStaffSchedules()` |
| 状态 | 已接入 |

**StaffScheduleView 字段：**

| 字段 | 类型 | 可空 | 说明 |
|------|------|------|------|
| id | Long | 否 | 雪花 ID |
| staffId | Long | 否 | 关联员工雪花 ID |
| storeId | Long | 否 | 关联门店雪花 ID |
| workDate | LocalDate | 否 | |
| startTime | LocalTime | 否 | |
| endTime | LocalTime | 否 | |
| status | String | 否 | AVAILABLE / UNAVAILABLE |
| remark | String | 是 | |

### 4.7 创建排班

| 字段 | 值 |
|------|-----|
| HTTP | `POST /api/v1/admin/staff/{id}/schedules` |
| Controller | `AdminManagementController.createSchedule()` |
| 权限码 | `staff:schedule:manage` |
| 路径参数 | `id: Long`（员工 ID） |
| 请求 DTO | `StaffScheduleRequest` |
| 响应 DTO | `StaffScheduleView` |
| 前端函数 | `staff.createStaffSchedule()` |
| 状态 | 已接入 |

**StaffScheduleRequest 字段：**

| 字段 | 类型 | 必填 | 约束 |
|------|------|------|------|
| storeId | Long | **是** | @NotNull, @Positive |
| workDate | LocalDate | **是** | @NotNull |
| startTime | LocalTime | **是** | @NotNull |
| endTime | LocalTime | **是** | @NotNull |
| status | String | **是** | @NotBlank, @Pattern("AVAILABLE\|UNAVAILABLE") |
| remark | String | 否 | @Size(max=255) |

### 4.8 更新排班

| 字段 | 值 |
|------|-----|
| HTTP | `PUT /api/v1/admin/staff/{id}/schedules/{scheduleId}` |
| Controller | `AdminManagementController.updateSchedule()` |
| 权限码 | `staff:schedule:manage` |
| 路径参数 | `id: Long`（员工 ID）, `scheduleId: Long` |
| 请求 DTO | `StaffScheduleRequest`（同 4.7） |
| 响应 DTO | `StaffScheduleView` |
| 前端函数 | `staff.updateStaffSchedule()` |
| 状态 | 已接入 |

---

## 5. 预约管理

### 5.1 预约列表

| 字段 | 值 |
|------|-----|
| HTTP | `GET /api/v1/admin/bookings` |
| Controller | `AdminBookingController.listBookings()` |
| 权限码 | `booking:booking:read` |
| 查询参数 | `page: int(默认1)`, `size: int(默认20)`, `status: String(可选)`, `bookingDate: String(可选)` |
| 响应 DTO | `PageResponse<BookingResponse>` |
| 前端函数 | `booking.getBookingList()` |
| 状态 | 已接入 |

**BookingResponse 字段：**

| 字段 | 类型 | 可空 | 说明 |
|------|------|------|------|
| id | Long | 否 | 雪花 ID |
| bookingNo | String | 否 | |
| userId | Long | 否 | 雪花 ID |
| petId | Long | 是 | 雪花 ID，可为空 |
| storeId | Long | 否 | 雪花 ID |
| serviceItemId | Long | 否 | 雪花 ID |
| staffId | Long | 是 | 雪花 ID，可为空 |
| serviceMode | String | 否 | |
| bookingDate | LocalDate | 否 | |
| startTime | LocalTime | 否 | |
| endTime | LocalTime | 否 | |
| addressId | Long | 是 | 雪花 ID，可为空 |
| distanceKm | BigDecimal | 是 | |
| contactName | String | 否 | |
| contactPhone | String | 否 | |
| price | BigDecimal | 否 | |
| paymentMethod | String | 否 | |
| paymentStatus | String | 否 | |
| status | String | 否 | |
| remark | String | 是 | |
| merchantRemark | String | 是 | |
| createTime | LocalDateTime | 否 | |

### 5.2 预约详情

| 字段 | 值 |
|------|-----|
| HTTP | `GET /api/v1/admin/bookings/{id}` |
| Controller | `AdminBookingController.getBooking()` |
| 权限码 | `booking:booking:read` |
| 路径参数 | `id: Long` |
| 响应 DTO | `BookingResponse` |
| 前端函数 | `booking.getBookingDetail()` |
| 状态 | 已接入 |

### 5.3 确认预约

| 字段 | 值 |
|------|-----|
| HTTP | `POST /api/v1/admin/bookings/{id}/confirm` |
| Controller | `AdminBookingController.confirmBooking()` |
| 权限码 | `booking:booking:confirm` |
| 路径参数 | `id: Long` |
| 请求 DTO | `BookingConfirmRequest`（可选） |
| 响应 DTO | `BookingResponse` |
| 前端函数 | `booking.confirmBooking()` |
| 状态 | 已接入 |

**BookingConfirmRequest 字段：**

| 字段 | 类型 | 必填 | 约束 |
|------|------|------|------|
| merchantRemark | String | 否 | |

### 5.4 拒绝预约

| 字段 | 值 |
|------|-----|
| HTTP | `POST /api/v1/admin/bookings/{id}/reject` |
| Controller | `AdminBookingController.rejectBooking()` |
| 权限码 | `booking:booking:reject` |
| 路径参数 | `id: Long` |
| 请求 DTO | `BookingRejectRequest`（@Valid） |
| 响应 DTO | `BookingResponse` |
| 前端函数 | `booking.rejectBooking()` |
| 状态 | 已接入 |

**BookingRejectRequest 字段：**

| 字段 | 类型 | 必填 | 约束 |
|------|------|------|------|
| reason | String | **是** | @NotBlank |

### 5.5 开始服务

| 字段 | 值 |
|------|-----|
| HTTP | `POST /api/v1/admin/bookings/{id}/start` |
| Controller | `AdminBookingController.startBooking()` |
| 权限码 | `booking:booking:start` |
| 路径参数 | `id: Long` |
| 请求 DTO | 无 |
| 响应 DTO | `BookingResponse` |
| 前端函数 | `booking.startBooking()` |
| 状态 | 已接入 |

### 5.6 完成预约

| 字段 | 值 |
|------|-----|
| HTTP | `POST /api/v1/admin/bookings/{id}/complete` |
| Controller | `AdminBookingController.completeBooking()` |
| 权限码 | `booking:booking:complete` |
| 路径参数 | `id: Long` |
| 请求 DTO | 无 |
| 响应 DTO | `BookingResponse` |
| 前端函数 | `booking.completeBooking()` |
| 状态 | 已接入 |

### 5.7 取消预约

| 字段 | 值 |
|------|-----|
| HTTP | `POST /api/v1/admin/bookings/{id}/cancel` |
| Controller | `AdminBookingController.cancelBooking()` |
| 权限码 | `booking:booking:cancel` |
| 路径参数 | `id: Long` |
| 请求 DTO | `Map<String, String>`（可选，key: `reason`） |
| 响应 DTO | `BookingResponse` |
| 前端函数 | `booking.cancelBooking()` |
| 状态 | 已接入 |

### 5.8 改派预约

| 字段 | 值 |
|------|-----|
| HTTP | `POST /api/v1/admin/bookings/{id}/reassign` |
| Controller | `AdminBookingController.reassignBooking()` |
| 权限码 | `booking:booking:reassign` |
| 路径参数 | `id: Long` |
| 请求 DTO | `BookingReassignRequest`（@Valid） |
| 响应 DTO | `BookingResponse` |
| 前端函数 | `booking.reassignBooking()` |
| 状态 | 未接入 |

**BookingReassignRequest 字段：**

| 字段 | 类型 | 必填 | 约束 |
|------|------|------|------|
| newStaffId | Long | **是** | @NotNull，雪花 ID |

---

## 6. 商品和库存

### 6.1 商品列表

| 字段 | 值 |
|------|-----|
| HTTP | `GET /api/v1/admin/products` |
| Controller | `AdminManagementController.listProducts()` |
| 权限码 | `product:item:read` |
| 查询参数 | `page: int(默认1)`, `size: int(默认20)`, `status: String(可选)` |
| 响应 DTO | `PageResponse<ProductView>` |
| 前端函数 | `product.getProductList()` |
| 状态 | 已接入 |

**ProductView 字段：**

| 字段 | 类型 | 可空 | 说明 |
|------|------|------|------|
| id | Long | 否 | 雪花 ID |
| categoryId | Long | 否 | 关联分类雪花 ID |
| name | String | 否 | |
| coverUrl | String | 是 | |
| price | BigDecimal | 否 | |
| stock | Integer | 是 | |
| salesCount | Integer | 是 | |
| description | String | 是 | |
| pickupOnly | Boolean | 否 | |
| status | String | 否 | |
| sort | Integer | 是 | |

### 6.2 创建商品

| 字段 | 值 |
|------|-----|
| HTTP | `POST /api/v1/admin/products` |
| Controller | `AdminManagementController.createProduct()` |
| 权限码 | `product:item:create` |
| 请求 DTO | `ProductRequest` |
| 响应 DTO | `ProductView` |
| 前端函数 | `product.createProduct()` |
| 状态 | 已接入 |

**ProductRequest 字段：**

| 字段 | 类型 | 必填 | 约束 |
|------|------|------|------|
| categoryId | Long | **是** | @NotNull, @Positive |
| name | String | **是** | @NotBlank, @Size(max=100) |
| coverUrl | String | 否 | @Size(max=255) |
| price | BigDecimal | **是** | @NotNull, @DecimalMin("0.0") |
| description | String | 否 | |
| pickupOnly | Boolean | **是** | @NotNull |
| sort | Integer | 否 | @Min(0) |

### 6.3 更新商品

| 字段 | 值 |
|------|-----|
| HTTP | `PUT /api/v1/admin/products/{id}` |
| Controller | `AdminManagementController.updateProduct()` |
| 权限码 | `product:item:update` |
| 路径参数 | `id: Long` |
| 请求 DTO | `ProductRequest`（同 6.2） |
| 响应 DTO | `ProductView` |
| 前端函数 | `product.updateProduct()` |
| 状态 | 已接入 |

### 6.4 停用商品

| 字段 | 值 |
|------|-----|
| HTTP | `POST /api/v1/admin/products/{id}/disable` |
| Controller | `AdminManagementController.disableProduct()` |
| 权限码 | `product:item:disable` |
| 路径参数 | `id: Long` |
| 请求 DTO | 无 |
| 响应 DTO | `ProductView` |
| 前端函数 | `product.disableProduct()` |
| 状态 | 已接入 |

### 6.5 更新商品库存

| 字段 | 值 |
|------|-----|
| HTTP | `PUT /api/v1/admin/products/{id}/stock` |
| Controller | `AdminManagementController.updateProductStock()` |
| 权限码 | `product:stock:update` |
| 路径参数 | `id: Long` |
| 请求 DTO | `ProductStockUpdateRequest` |
| 响应 DTO | `ProductView` |
| 前端函数 | `product.updateProductStock()` |
| 状态 | 已接入 |

**ProductStockUpdateRequest 字段：**

| 字段 | 类型 | 必填 | 约束 |
|------|------|------|------|
| stock | Integer | **是** | @NotNull, @Min(0) |

---

## 7. 自提订单

### 7.1 订单列表

| 字段 | 值 |
|------|-----|
| HTTP | `GET /api/v1/admin/product-orders` |
| Controller | `AdminProductOrderController.listOrders()` |
| 权限码 | `product:order:read` |
| 查询参数 | `page: int(默认1)`, `size: int(默认20)`, `status: String(可选)` |
| 响应 DTO | `PageResponse<ProductOrderResponse>` |
| 前端函数 | `product-order.getProductOrderList()` |
| 状态 | 已接入 |

**ProductOrderResponse 字段：**

| 字段 | 类型 | 可空 | 说明 |
|------|------|------|------|
| id | Long | 否 | 雪花 ID |
| orderNo | String | 否 | |
| totalAmount | BigDecimal | 否 | |
| paymentMethod | String | 否 | |
| paymentStatus | String | 否 | |
| pickupStatus | String | 否 | |
| status | String | 否 | |
| contactName | String | 否 | |
| contactPhone | String | 否 | |
| remark | String | 是 | |
| createTime | LocalDateTime | 否 | |
| confirmTime | LocalDateTime | 是 | |
| completeTime | LocalDateTime | 是 | |
| cancelTime | LocalDateTime | 是 | |

### 7.2 订单详情

| 字段 | 值 |
|------|-----|
| HTTP | `GET /api/v1/admin/product-orders/{id}` |
| Controller | `AdminProductOrderController.getOrderDetail()` |
| 权限码 | `product:order:read` |
| 路径参数 | `id: Long` |
| 响应 DTO | `ProductOrderDetailResponse` |
| 前端函数 | `product-order.getProductOrderDetail()` |
| 状态 | 已接入 |

**ProductOrderDetailResponse 字段：**

| 字段 | 类型 | 可空 | 说明 |
|------|------|------|------|
| id | Long | 否 | 雪花 ID |
| orderNo | String | 否 | |
| userId | Long | 否 | 雪花 ID |
| storeId | Long | 否 | 雪花 ID |
| totalAmount | BigDecimal | 否 | |
| paymentMethod | String | 否 | |
| paymentStatus | String | 否 | |
| pickupStatus | String | 否 | |
| status | String | 否 | |
| contactName | String | 否 | |
| contactPhone | String | 否 | |
| remark | String | 是 | |
| merchantRemark | String | 是 | |
| createTime | LocalDateTime | 否 | |
| confirmTime | LocalDateTime | 是 | |
| completeTime | LocalDateTime | 是 | |
| cancelTime | LocalDateTime | 是 | |
| items | List\<OrderItemResponse\> | 否 | |

**OrderItemResponse 字段：**

| 字段 | 类型 | 可空 | 说明 |
|------|------|------|------|
| id | Long | 否 | 雪花 ID |
| productId | Long | 否 | 雪花 ID |
| productName | String | 否 | |
| productCoverUrl | String | 否 | |
| price | BigDecimal | 否 | |
| quantity | Integer | 否 | |
| totalAmount | BigDecimal | 否 | |

### 7.3 确认订单

| 字段 | 值 |
|------|-----|
| HTTP | `POST /api/v1/admin/product-orders/{id}/confirm` |
| Controller | `AdminProductOrderController.confirmOrder()` |
| 权限码 | `product:order:confirm` |
| 路径参数 | `id: Long` |
| 请求 DTO | 无 |
| 响应 DTO | `ProductOrderResponse` |
| 前端函数 | `product-order.confirmProductOrder()` |
| 状态 | 已接入 |

### 7.4 标记待自提

| 字段 | 值 |
|------|-----|
| HTTP | `POST /api/v1/admin/product-orders/{id}/ready` |
| Controller | `AdminProductOrderController.markReadyForPickup()` |
| 权限码 | `product:order:ready` |
| 路径参数 | `id: Long` |
| 请求 DTO | 无 |
| 响应 DTO | `ProductOrderResponse` |
| 前端函数 | `product-order.readyProductOrder()` |
| 状态 | 已接入 |

### 7.5 确认付款

| 字段 | 值 |
|------|-----|
| HTTP | `POST /api/v1/admin/product-orders/{id}/confirm-payment` |
| Controller | `AdminProductOrderController.confirmPayment()` |
| 权限码 | `product:order:confirm-payment` |
| 路径参数 | `id: Long` |
| 请求 DTO | 无 |
| 响应 DTO | `ProductOrderResponse` |
| 前端函数 | `product-order.confirmPaymentOrder()` |
| 状态 | 已接入 |

### 7.6 完成订单

| 字段 | 值 |
|------|-----|
| HTTP | `POST /api/v1/admin/product-orders/{id}/complete` |
| Controller | `AdminProductOrderController.completeOrder()` |
| 权限码 | `product:order:complete` |
| 路径参数 | `id: Long` |
| 请求 DTO | 无 |
| 响应 DTO | `ProductOrderResponse` |
| 前端函数 | `product-order.completeProductOrder()` |
| 状态 | 已接入 |

### 7.7 取消订单

| 字段 | 值 |
|------|-----|
| HTTP | `POST /api/v1/admin/product-orders/{id}/cancel` |
| Controller | `AdminProductOrderController.cancelOrder()` |
| 权限码 | `product:order:cancel` |
| 路径参数 | `id: Long` |
| 请求 DTO | `AdminOrderActionRequest`（可选） |
| 响应 DTO | `ProductOrderResponse` |
| 前端函数 | `product-order.cancelProductOrder()` |
| 状态 | 已接入 |

**AdminOrderActionRequest 字段：**

| 字段 | 类型 | 必填 | 约束 |
|------|------|------|------|
| reason | String | 否 | |

### 7.8 缺货处理

| 字段 | 值 |
|------|-----|
| HTTP | `POST /api/v1/admin/product-orders/{id}/out-of-stock` |
| Controller | `AdminProductOrderController.outOfStock()` |
| 权限码 | `product:order:cancel`（复用取消权限） |
| 路径参数 | `id: Long` |
| 请求 DTO | `AdminOrderActionRequest`（可选） |
| 响应 DTO | `ProductOrderResponse` |
| 前端函数 | `product-order.outOfStockProductOrder()` |
| 状态 | 未接入 |

---

## 8. 帖子管理

### 8.1 帖子列表

| 字段 | 值 |
|------|-----|
| HTTP | `GET /api/v1/admin/community/posts` |
| Controller | `AdminCommunityController.listPosts()` |
| 权限码 | `community:post:read` |
| 查询参数 | `status: String(可选)`, `page: int(默认1)`, `size: int(默认20)` |
| 响应 DTO | `PageResponse<PostResponse>` |
| 前端函数 | `community.getPostList()` |
| 状态 | 已接入 |

**PostResponse 字段：**

| 字段 | 类型 | 可空 | 说明 |
|------|------|------|------|
| id | Long | 否 | 雪花 ID |
| userId | Long | 否 | 雪花 ID |
| petId | Long | 是 | 雪花 ID，可为空 |
| topicId | Long | 是 | 雪花 ID，可为空 |
| title | String | 否 | |
| content | String | 否 | |
| status | String | 否 | |
| viewCount | Integer | 否 | |
| likeCount | Integer | 否 | |
| commentCount | Integer | 否 | |
| favoriteCount | Integer | 否 | |
| publishTime | LocalDateTime | 是 | |
| createTime | LocalDateTime | 否 | |

### 8.2 帖子详情

| 字段 | 值 |
|------|-----|
| HTTP | `GET /api/v1/admin/community/posts/{id}` |
| Controller | `AdminCommunityController.getPost()` |
| 权限码 | `community:post:read` |
| 路径参数 | `id: Long` |
| 响应 DTO | `PostResponse` |
| 前端函数 | `community.getPostDetail()` |
| 状态 | 已接入 |

### 8.3 审核通过帖子

| 字段 | 值 |
|------|-----|
| HTTP | `POST /api/v1/admin/community/posts/{id}/approve` |
| Controller | `AdminCommunityController.approvePost()` |
| 权限码 | `community:post:approve` |
| 路径参数 | `id: Long` |
| 请求 DTO | `AdminReviewRequest`（可选） |
| 响应 DTO | `PostResponse` |
| 前端函数 | `community.approvePost()` |
| 状态 | 已接入 |

**AdminReviewRequest 字段：**

| 字段 | 类型 | 必填 | 约束 |
|------|------|------|------|
| remark | String | 否 | @Size(max=500) |

### 8.4 审核拒绝帖子

| 字段 | 值 |
|------|-----|
| HTTP | `POST /api/v1/admin/community/posts/{id}/reject` |
| Controller | `AdminCommunityController.rejectPost()` |
| 权限码 | `community:post:reject` |
| 路径参数 | `id: Long` |
| 请求 DTO | `AdminReviewRequest`（可选） |
| 响应 DTO | `PostResponse` |
| 前端函数 | `community.rejectPost()` |
| 状态 | 已接入 |

### 8.5 隐藏帖子

| 字段 | 值 |
|------|-----|
| HTTP | `POST /api/v1/admin/community/posts/{id}/hide` |
| Controller | `AdminCommunityController.hidePost()` |
| 权限码 | `community:post:hide` |
| 路径参数 | `id: Long` |
| 请求 DTO | 无 |
| 响应 DTO | `PostResponse` |
| 前端函数 | `community.hidePost()` |
| 状态 | 已接入 |

### 8.6 删除帖子

| 字段 | 值 |
|------|-----|
| HTTP | `POST /api/v1/admin/community/posts/{id}/delete` |
| Controller | `AdminCommunityController.deletePost()` |
| 权限码 | `community:post:delete` |
| 路径参数 | `id: Long` |
| 请求 DTO | 无 |
| 响应 DTO | `Void` |
| 前端函数 | `community.deletePost()` |
| 状态 | 已接入 |

---

## 9. 评论管理

### 9.1 评论列表

| 字段 | 值 |
|------|-----|
| HTTP | `GET /api/v1/admin/community/comments` |
| Controller | `AdminCommunityController.listComments()` |
| 权限码 | `community:post:read`（复用帖子读取权限） |
| 查询参数 | `status: String(可选)`, `page: int(默认1)`, `size: int(默认20)` |
| 响应 DTO | `PageResponse<CommentResponse>` |
| 前端函数 | `community.getCommentList()` |
| 状态 | 已接入 |

**CommentResponse 字段：**

| 字段 | 类型 | 可空 | 说明 |
|------|------|------|------|
| id | Long | 否 | 雪花 ID |
| postId | Long | 否 | 雪花 ID |
| userId | Long | 否 | 雪花 ID |
| parentId | Long | 是 | 雪花 ID，可为空 |
| content | String | 否 | |
| status | String | 否 | |
| likeCount | Integer | 否 | |
| createTime | LocalDateTime | 否 | |

### 9.2 审核通过评论

| 字段 | 值 |
|------|-----|
| HTTP | `POST /api/v1/admin/community/comments/{id}/approve` |
| Controller | `AdminCommunityController.approveComment()` |
| 权限码 | `community:post:approve` |
| 路径参数 | `id: Long` |
| 请求 DTO | `AdminReviewRequest`（可选） |
| 响应 DTO | `CommentResponse` |
| 前端函数 | `community.approveComment()` |
| 状态 | 已接入 |

### 9.3 审核拒绝评论

| 字段 | 值 |
|------|-----|
| HTTP | `POST /api/v1/admin/community/comments/{id}/reject` |
| Controller | `AdminCommunityController.rejectComment()` |
| 权限码 | `community:post:reject` |
| 路径参数 | `id: Long` |
| 请求 DTO | `AdminReviewRequest`（可选） |
| 响应 DTO | `CommentResponse` |
| 前端函数 | `community.rejectComment()` |
| 状态 | 已接入 |

### 9.4 隐藏评论

| 字段 | 值 |
|------|-----|
| HTTP | `POST /api/v1/admin/community/comments/{id}/hide` |
| Controller | `AdminCommunityController.hideComment()` |
| 权限码 | `community:comment:hide` |
| 路径参数 | `id: Long` |
| 请求 DTO | 无 |
| 响应 DTO | `CommentResponse` |
| 前端函数 | `community.hideComment()` |
| 状态 | 已接入 |

### 9.5 删除评论

| 字段 | 值 |
|------|-----|
| HTTP | `POST /api/v1/admin/community/comments/{id}/delete` |
| Controller | `AdminCommunityController.deleteComment()` |
| 权限码 | `community:comment:delete` |
| 路径参数 | `id: Long` |
| 请求 DTO | 无 |
| 响应 DTO | `Void` |
| 前端函数 | `community.deleteComment()` |
| 状态 | 已接入 |

---

## 10. 举报管理

### 10.1 举报列表

| 字段 | 值 |
|------|-----|
| HTTP | `GET /api/v1/admin/community/reports` |
| Controller | `AdminCommunityController.listReports()` |
| 权限码 | `community:report:handle` |
| 查询参数 | `status: String(可选)`, `page: int(默认1)`, `size: int(默认20)` |
| 响应 DTO | `PageResponse<PostReport>` |
| 前端函数 | `community.getReportList()` |
| 状态 | 已接入 |

**PostReport 实体字段（直接返回实体，无专用 DTO）：**

| 字段 | 类型 | 可空 | 说明 |
|------|------|------|------|
| id | Long | 否 | 雪花 ID |
| postId | Long | 否 | 雪花 ID |
| reporterId | Long | 否 | 雪花 ID |
| reasonType | String | 是 | 前端缺少此字段 |
| reason | String | 是 | |
| status | String | 否 | |
| handleResult | String | 是 | |
| handlerId | Long | 是 | 雪花 ID，前端缺少此字段 |
| handleTime | LocalDateTime | 是 | |
| createTime | LocalDateTime | 否 | |

**契约问题：**
- 前端 `PostReport` 接口定义了 `handleRemark` 字段，但后端实体不存在此字段。后端实际字段为 `reasonType` 和 `handlerId`，前端未定义。

### 10.2 处理举报

| 字段 | 值 |
|------|-----|
| HTTP | `POST /api/v1/admin/community/reports/{id}/handle` |
| Controller | `AdminCommunityController.handleReport()` |
| 权限码 | `community:report:handle` |
| 路径参数 | `id: Long` |
| 请求 DTO | `AdminReportHandleRequest`（@Valid） |
| 响应 DTO | `Void` |
| 前端函数 | `community.handleReport()` |
| 状态 | 已接入 |

**AdminReportHandleRequest 字段：**

| 字段 | 类型 | 必填 | 约束 |
|------|------|------|------|
| handleResult | String | **是** | @NotBlank, @Pattern("PROCESSED\|IGNORED") |
| hidePost | boolean | 否 | 默认 false |
| handleRemark | String | 否 | @Size(max=500) |

---

## 11. 敏感词管理

### 11.1 敏感词列表

| 字段 | 值 |
|------|-----|
| HTTP | `GET /api/v1/admin/moderation/sensitive-words` |
| Controller | `AdminSensitiveWordController.listSensitiveWords()` |
| 权限码 | `community:sensitive-word:manage` |
| 查询参数 | `status: String(可选)`, `page: int(默认1)`, `size: int(默认20)` |
| 响应 DTO | `PageResponse<SensitiveWordResponse>` |
| 前端函数 | `moderation.getSensitiveWords()` |
| 状态 | 已接入 |

**SensitiveWordResponse 字段：**

| 字段 | 类型 | 可空 | 说明 |
|------|------|------|------|
| id | Long | 否 | 雪花 ID |
| word | String | 否 | |
| category | String | 是 | |
| level | Integer | 否 | |
| status | String | 否 | |
| createTime | LocalDateTime | 否 | |

### 11.2 创建敏感词

| 字段 | 值 |
|------|-----|
| HTTP | `POST /api/v1/admin/moderation/sensitive-words` |
| Controller | `AdminSensitiveWordController.createSensitiveWord()` |
| 权限码 | `community:sensitive-word:manage` |
| 请求 DTO | `SensitiveWordCreateRequest` |
| 响应 DTO | `SensitiveWordResponse` |
| 前端函数 | `moderation.createSensitiveWord()` |
| 状态 | 已接入 |

**SensitiveWordCreateRequest 字段：**

| 字段 | 类型 | 必填 | 约束 |
|------|------|------|------|
| word | String | **是** | @NotBlank, @Size(1..100) |
| category | String | 否 | |
| level | int | **是** | @Min(1), @Max(3) |

### 11.3 更新敏感词

| 字段 | 值 |
|------|-----|
| HTTP | `PATCH /api/v1/admin/moderation/sensitive-words/{id}` |
| Controller | `AdminSensitiveWordController.updateSensitiveWord()` |
| 权限码 | `community:sensitive-word:manage` |
| 路径参数 | `id: Long` |
| 请求 DTO | `SensitiveWordCreateRequest`（所有字段可选，局部更新） |
| 响应 DTO | `SensitiveWordResponse` |
| 前端函数 | `moderation.updateSensitiveWord()` |
| 状态 | 已接入 |

### 11.4 禁用敏感词

| 字段 | 值 |
|------|-----|
| HTTP | `POST /api/v1/admin/moderation/sensitive-words/{id}/disable` |
| Controller | `AdminSensitiveWordController.disableSensitiveWord()` |
| 权限码 | `community:sensitive-word:manage` |
| 路径参数 | `id: Long` |
| 请求 DTO | 无 |
| 响应 DTO | `Void` |
| 前端函数 | `moderation.disableSensitiveWord()` |
| 状态 | 已接入 |

---

## 12. 操作日志

### 12.1 操作日志列表

| 字段 | 值 |
|------|-----|
| HTTP | `GET /api/v1/admin/operation-logs` |
| Controller | `AdminManagementController.listOperationLogs()` |
| 权限码 | `admin:operation-log:read` |
| 查询参数 | `page: int(默认1)`, `size: int(默认20)`, `module: String(可选)` |
| 响应 DTO | `PageResponse<OperationLogView>` |
| 前端函数 | `operation-log.getOperationLogs()` |
| 状态 | 已接入 |

**OperationLogView 字段：**

| 字段 | 类型 | 可空 | 说明 |
|------|------|------|------|
| id | Long | 否 | 雪花 ID |
| adminId | Long | 否 | 雪花 ID |
| module | String | 否 | |
| operation | String | 否 | |
| requestMethod | String | 否 | |
| requestUrl | String | 否 | |
| result | String | 否 | |
| errorMessage | String | 是 | |
| createTime | LocalDateTime | 否 | |

---

## 13. AI 分析报告

> 前端当前无 AI 相关 API 文件。以下接口均为真实存在但未接入。

### 13.1 生成分析报告

| 字段 | 值 |
|------|-----|
| HTTP | `POST /api/v1/admin/ai/analysis-reports` |
| Controller | `AdminAiAnalysisController.generateReport()` |
| 权限码 | `ai:analysis:generate` |
| 请求 DTO | `AiAnalysisCreateRequest` |
| 响应 DTO | `AiAnalysisReportResponse` |
| 前端函数 | 无 |
| 状态 | 未接入 |

**AiAnalysisCreateRequest 字段：**

| 字段 | 类型 | 必填 | 约束 |
|------|------|------|------|
| reportType | String | **是** | @NotNull, @Pattern("BUSINESS\|COMMUNITY\|SALES\|ACTIVITY") |
| startDate | LocalDate | **是** | @NotNull |
| endDate | LocalDate | **是** | @NotNull |

### 13.2 分析报告列表

| 字段 | 值 |
|------|-----|
| HTTP | `GET /api/v1/admin/ai/analysis-reports` |
| Controller | `AdminAiAnalysisController.listReports()` |
| 权限码 | `analytics:dashboard:read` |
| 查询参数 | `page: int(默认1)`, `size: int(默认20)`, `reportType: String(可选)` |
| 响应 DTO | `PageResponse<AiAnalysisReportResponse>` |
| 前端函数 | 无 |
| 状态 | 未接入 |

### 13.3 分析报告详情

| 字段 | 值 |
|------|-----|
| HTTP | `GET /api/v1/admin/ai/analysis-reports/{id}` |
| Controller | `AdminAiAnalysisController.getReport()` |
| 权限码 | `analytics:dashboard:read` |
| 路径参数 | `id: Long` |
| 响应 DTO | `AiAnalysisReportResponse` |
| 前端函数 | 无 |
| 状态 | 未接入 |

**AiAnalysisReportResponse 字段：**

| 字段 | 类型 | 可空 | 说明 |
|------|------|------|------|
| id | Long | 否 | 雪花 ID |
| reportType | String | 否 | BUSINESS / COMMUNITY / SALES / ACTIVITY |
| startDate | LocalDate | 否 | |
| endDate | LocalDate | 否 | |
| aiSummary | String | 是 | |
| suggestions | String | 是 | |
| createdBy | Long | 是 | 雪花 ID |
| createTime | LocalDateTime | 否 | |

---

## 14. AI 用量查询

> 前端当前无 AI 相关 API 文件。以下接口均为真实存在但未接入。

### 14.1 AI 用量日志列表

| 字段 | 值 |
|------|-----|
| HTTP | `GET /api/v1/admin/ai/usage` |
| Controller | `AdminAiUsageController.listUsage()` |
| 权限码 | `ai:usage:read` |
| 查询参数 | `page: int(默认1)`, `size: int(默认20)`, `apiType: String(可选)`, `success: Boolean(可选)`, `startDate: String(可选)`, `endDate: String(可选)` |
| 响应 DTO | `PageResponse<AiUsageResponse>` |
| 前端函数 | 无 |
| 状态 | 未接入 |

**AiUsageResponse 字段：**

| 字段 | 类型 | 可空 | 说明 |
|------|------|------|------|
| id | Long | 否 | 雪花 ID |
| userId | Long | 是 | 雪花 ID |
| adminId | Long | 是 | 雪花 ID |
| apiType | String | 是 | |
| modelName | String | 是 | |
| promptTokens | Integer | 是 | |
| completionTokens | Integer | 是 | |
| totalTokens | Integer | 是 | |
| success | Boolean | 是 | |
| errorMessage | String | 是 | |
| createTime | LocalDateTime | 否 | |

---

## 附录 A：接口状态统计

| 状态 | 数量 | 说明 |
|------|------|------|
| 已接入 | 56 | 前端 API 文件已定义且路径匹配后端 |
| 未接入 | 6 | 后端真实存在，前端未定义 API 或未在页面使用 |
| 阻塞 | 4 | 门店相关接口因 D-012 阻塞 |
| 伪接口 | 1 | `getStaffSkills`：后端不存在 GET 读取 |

### 未接入接口清单

1. `PUT /api/v1/admin/staff/{id}/skills` — 替换员工技能（前端有定义但页面未完整接入）
2. `POST /api/v1/admin/bookings/{id}/reassign` — 预约改派
3. `POST /api/v1/admin/product-orders/{id}/out-of-stock` — 订单缺货
4. `POST /api/v1/admin/ai/analysis-reports` — 生成 AI 分析报告
5. `GET /api/v1/admin/ai/analysis-reports` — AI 分析报告列表
6. `GET /api/v1/admin/ai/analysis-reports/{id}` — AI 分析报告详情
7. `GET /api/v1/admin/ai/usage` — AI 用量日志列表

### 伪接口清单

1. `GET /api/v1/admin/staff/{id}/skills` — 前端 `staff.getStaffSkills()` 对应的后端 GET 不存在

### 被决策阻塞的接口

1. `GET /api/v1/admin/stores/{id}` — 需要 D-012 确定门店 ID 来源
2. `PATCH /api/v1/admin/stores/{id}` — 同上
3. `GET /api/v1/admin/stores/{id}/config` — 同上
4. `PUT /api/v1/admin/stores/{id}/config` — 同上

---

## 附录 B：权限码清单

以下为后端 `@PreAuthorize` 中使用的全部权限码：

| 权限码 | 接口 |
|--------|------|
| （无） | 登录、获取当前管理员 |
| `store:info:read` | 门店详情 |
| `store:info:update` | 更新门店 |
| `store:config:read` | 门店配置 |
| `store:config:update` | 更新门店配置 |
| `service:item:read` | 服务项目列表 |
| `service:item:create` | 创建服务项目 |
| `service:item:update` | 更新服务项目 |
| `service:item:disable` | 停用服务项目 |
| `staff:profile:read` | 员工列表 |
| `staff:profile:create` | 创建员工 |
| `staff:profile:update` | 更新员工 |
| `staff:profile:disable` | 停用员工 |
| `staff:skill:manage` | 替换员工技能 |
| `staff:schedule:read` | 员工排班列表 |
| `staff:schedule:manage` | 创建/更新排班 |
| `booking:booking:read` | 预约列表/详情 |
| `booking:booking:confirm` | 确认预约 |
| `booking:booking:reject` | 拒绝预约 |
| `booking:booking:start` | 开始服务 |
| `booking:booking:complete` | 完成预约 |
| `booking:booking:cancel` | 取消预约 |
| `booking:booking:reassign` | 改派预约 |
| `product:item:read` | 商品列表 |
| `product:item:create` | 创建商品 |
| `product:item:update` | 更新商品 |
| `product:item:disable` | 停用商品 |
| `product:stock:update` | 更新库存 |
| `product:order:read` | 订单列表/详情 |
| `product:order:confirm` | 确认订单 |
| `product:order:ready` | 标记待自提 |
| `product:order:confirm-payment` | 确认付款 |
| `product:order:complete` | 完成订单 |
| `product:order:cancel` | 取消/缺货 |
| `community:post:read` | 帖子列表/详情、评论列表 |
| `community:post:approve` | 审核通过帖子/评论 |
| `community:post:reject` | 审核拒绝帖子/评论 |
| `community:post:hide` | 隐藏帖子 |
| `community:post:delete` | 删除帖子 |
| `community:comment:hide` | 隐藏评论 |
| `community:comment:delete` | 删除评论 |
| `community:report:handle` | 举报列表/处理 |
| `community:sensitive-word:manage` | 敏感词 CRUD |
| `admin:operation-log:read` | 操作日志 |
| `ai:analysis:generate` | 生成 AI 分析 |
| `analytics:dashboard:read` | AI 分析报告查看 |
| `ai:usage:read` | AI 用量查询 |

---

## 附录 C：ID 字段汇总

> 所有以下 `Long` 字段在雪花 ID 生成策略下可能超过 JavaScript `Number.MAX_SAFE_INTEGER`。
> D-011 未决前，这些字段的 JSON 传输精度无法保证安全。

**涉及雪花 ID 的响应字段（按模块）：**

| 模块 | 字段 |
|------|------|
| 认证 | `AdminLoginResponse.admin.id`, `AdminMeResponse.id` |
| 门店 | `StoreView.id`, `StoreConfigView.id`, `StoreConfigView.storeId` |
| 服务 | `ServiceItemView.id`, `ServiceItemView.categoryId` |
| 员工 | `StaffView.id`, `StaffView.storeId`, `StaffSkillView.staffId`, `StaffSkillView.serviceCategoryIds[]`, `StaffScheduleView.id`, `StaffScheduleView.staffId`, `StaffScheduleView.storeId` |
| 预约 | `BookingResponse.id`, `userId`, `petId`, `storeId`, `serviceItemId`, `staffId`, `addressId` |
| 商品 | `ProductView.id`, `categoryId` |
| 订单 | `ProductOrderResponse.id`, `ProductOrderDetailResponse.userId`, `storeId`, `OrderItemResponse.id`, `productId` |
| 社区 | `PostResponse.id`, `userId`, `petId`, `topicId`, `CommentResponse.id`, `postId`, `userId`, `parentId`, `PostReport.id`, `postId`, `reporterId`, `handlerId` |
| 敏感词 | `SensitiveWordResponse.id` |
| 操作日志 | `OperationLogView.id`, `adminId` |
| AI | `AiAnalysisReportResponse.id`, `createdBy`, `AiUsageResponse.id`, `userId`, `adminId` |

**涉及雪花 ID 的请求字段：**

| 模块 | 字段 |
|------|------|
| 服务 | `ServiceItemRequest.categoryId` |
| 员工 | `StaffRequest.storeId`, `StaffSkillUpdateRequest.serviceCategoryIds[]`, `StaffScheduleRequest.storeId` |
| 预约 | `BookingReassignRequest.newStaffId` |
| 商品 | `ProductRequest.categoryId` |
| 所有路径参数 | `{id}` 和 `{scheduleId}` |
