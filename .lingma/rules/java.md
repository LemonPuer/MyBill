---
trigger: always_on
---

你是一个资深的 Java 专家，请在开发中遵循如下规则：

1. 严格遵循SOLID、DRY、KISS、YAGNI原则
2. 遵循OWASP 安全最佳实践（如输入验证、SQL注入防护）
3. 采用分层架构设计，确保职责分离

## 技术栈要求

1. 语言：Java 21 
2. 主体框架：Spring Boot 3.5.10 
3. 数据库：mysql 
4. 配置中心：nacos

依赖：
1. spring-boot-starter-validation
2. spring-boot-starter-web
3. spring-boot-starter-security
4. hutool-all 
5. mybatis-flex
6. langchain4j

## 应用逻辑设计规范
1. 分层架构原则

|    层级    |                  职责                  | 约束                                                         |
| :--------: | :------------------------------------: | :----------------------------------------------------------- |
| Controller |    处理HTTP请求与响应，定义API接口     | - 禁止直接操作数据库，必须通过service层调用<br />- 响应必须使用ApiResp或PageResp进行封装<br />- 请求使用ApiReq进行封装<br />- 必填字段使用@Validated等注解进行校验<br />- 所有接口使用POST，复杂请求体需要创建对应的req对象 |
|  Service   |              业务逻辑实现              | - 不需要接口层，必须继承Flex对应ServiceImpl<br />- 可以使用Flex提供的方法或使用mapper操作数据库 |
|   Mapper   | 数据持久化操作，定义数据库增删改查功能 | - 必须继承Flex对应的BaseMapper                               |
|   Entity   |          数据库表结构映射对象          | - 禁止直接返回给前端，清晰分类DTO、VO等对象                  |

## 核心代码规范

### Controller：

```java
@RestController
@RequestMapping("openApi/test")
public class TestController {

    @GetMapping("hi")
    public ApiResp<String> test() {
        return ApiResp.ok("hello world");
    }

}
```

### Service

```java
@Service
public class UserTokenService extends ServiceImpl<UserTokenMapper, UserToken> {

}
```

### Mapper

```java
public interface AccountsMapper extends BaseMapper<Accounts> {

}
```

### Entity
```java
@Data
@Builder
@Table("tt_user")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @Id(keyType = KeyType.Auto)
    private Integer id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 邮件地址
     */
    private String email;

    /**
     * 密码
     */
    private String password;

    /**
     * 头像地址
     */
    private String avatarUrl;

    /**
     * 描述
     */
    private String description;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;

}
```

## 日志规范：

使用 SLF4J 记录日志（禁止直接使用 System.out.println）

核心操作需记录 INFO 级别日志，异常记录 ERROR 级别