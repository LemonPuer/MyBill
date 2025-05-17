import com.alibaba.druid.pool.DruidDataSource;
import com.mybatisflex.codegen.Generator;
import com.mybatisflex.codegen.config.GlobalConfig;

/**
 * description: add a description
 *
 * @author Lemon
 * @version 1.0.0
 * @date 2024/10/01 20:03:23
 */
public class Codegen {

    public static void main(String[] args) {
        // 配置数据源
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setUrl("jdbc:mysql://192.168.31.100:3306/bill?characterEncoding=utf-8");
        dataSource.setUsername("root");
        dataSource.setPassword("aA20010726");

        GlobalConfig globalConfig = createGlobalConfigUseStyle2();

        // 通过 datasource 和 globalConfig 创建代码生成器
        Generator generator = new Generator(dataSource, globalConfig);

        // 生成代码
        generator.generate();
    }

    public static GlobalConfig createGlobalConfigUseStyle2() {
        // 创建配置内容
        GlobalConfig globalConfig = new GlobalConfig();

        // 设置根包
        globalConfig.getPackageConfig().setBasePackage("org.lemon");

        // 设置表前缀和只生成哪些表，setGenerateTable 未配置时，生成所有表
        globalConfig.getStrategyConfig()
                .setTablePrefix("tt_")
                .setGenerateTable("tt_user_token");

        // 设置生成 entity 并启用 Lombok
        globalConfig.enableEntity().setWithLombok(true)
                .setWithBasePackage("domain")
                .setOverwriteEnable(true)
                .setJdkVersion(8);

        // globalConfig.enableTableDef().setOverwriteEnable(true);
        // 设置生成 mapper
        globalConfig.enableMapper().setOverwriteEnable(true);

        // globalConfig.enableService().setOverwriteEnable(true);

        globalConfig.enableServiceImpl().setOverwriteEnable(true);

        // globalConfig.enableController().setOverwriteEnable(true);

        // 可以单独配置某个列
        // ColumnConfig columnConfig = new ColumnConfig();
        // columnConfig.setColumnName("tenant_id");
        // columnConfig.setLarge(true);
        // columnConfig.setVersion(true);
        // globalConfig.getStrategyConfig()
        //         .setColumnConfig("tb_account", columnConfig);

        return globalConfig;
    }
}