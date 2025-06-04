package com.xuecheng.generator;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.config.*;
import com.baomidou.mybatisplus.generator.config.po.TableFill;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;

import java.util.Arrays;

/**
 * MyBatis-Plus 代码生成类
 */
public class CommonCodeGenerator {
	// TODO 数据库驱动类名
	private static String driverName="com.mysql.cj.jdbc.Driver";
	// TODO 数据库连接地址
	private static final String SERVICE_HOST = "192.168.72.65:3306";
	// TODO 数据库连接用户名
	private static final String DATA_SOURCE_USER_NAME  = "root";
	// TODO 数据库连接用户名密码
	private static final String DATA_SOURCE_PASSWORD  = "mysql";
	// TODO 数据库名字
	private static final String SERVICE_NAME = "xcgc_content";

	private static final String[] TABLE_NAMES = new String[]{
			//TODO 需要生成的表的名字
			"mq_message",
			"mq_message_history"
	};

	// TODO 默认生成entity，需要生成DTO修改此变量
	private static final Boolean IS_DTO = false;

	public static void main(String[] args) {
		// 代码生成器
		AutoGenerator mpg = new AutoGenerator();
		// 选择 freemarker 引擎，默认 Velocity
		mpg.setTemplateEngine(new FreemarkerTemplateEngine());
		// 全局配置
		// 全局策略配置提供了一些全局的设置，
		// 如输出目录、文件覆盖、开发者信息等，以及一些高级选项，如 Kotlin 模式、Swagger2 集成、ActiveRecord 模式等。
		GlobalConfig gc = new GlobalConfig();
		//是否覆盖已有文件。
		gc.setFileOverride(true);
		//输出路径
		gc.setOutputDir(System.getProperty("user.dir") + "/xuecheng-plus-generator/src/main/java");
		//作者
		gc.setAuthor("gc");
		gc.setOpen(false);
		gc.setSwagger2(false);
		//设置Service命名方式
		gc.setServiceName("%sService");
		// 开启BaseResultMap
        gc.setBaseResultMap(true);
		// 开启baseColumnList
        gc.setBaseColumnList(true);

		if (IS_DTO) {
			gc.setSwagger2(true);
			gc.setEntityName("%sDTO");
		}
		mpg.setGlobalConfig(gc);

		// 数据库配置
		DataSourceConfig dsc = new DataSourceConfig();
		dsc.setDbType(DbType.MYSQL);
		dsc.setUrl("jdbc:mysql://"+SERVICE_HOST+"/" + SERVICE_NAME
				+ "?useUnicode=true&useSSL=false&characterEncoding=utf8");
		dsc.setDriverName(driverName);
		dsc.setUsername(DATA_SOURCE_USER_NAME);
		dsc.setPassword(DATA_SOURCE_PASSWORD);
		mpg.setDataSource(dsc);

		// 包配置
		// 包名配置用于定义生成代码的包结构，确保生成的代码放置在正确的目录中。
		// 通过配置包名，可以控制代码的组织方式，使其符合项目的架构设计。
		PackageConfig pc = new PackageConfig();
		pc.setModuleName(SERVICE_NAME);
		pc.setParent("com.xuecheng");
		pc.setServiceImpl("service.impl");
		pc.setXml("mapper");
		pc.setEntity("model.po");
		mpg.setPackageInfo(pc);


		// 设置模板
		TemplateConfig tc = new TemplateConfig();
		mpg.setTemplate(tc);

		// 策略配置，数据库表配置
		//数据库表配置用于定义生成代码时如何处理数据库表和字段。
		// 通过策略配置，可以指定生成哪些表的代码、如何命名实体类和字段、以及是否包含特定的注解或属性。
		StrategyConfig strategy = new StrategyConfig();
		// 指定需要生成代码的表名
		strategy.setInclude(TABLE_NAMES);
		//数据库表映射到实体的命名策略。默认使用下划线转驼峰命名
		strategy.setNaming(NamingStrategy.underline_to_camel);
		//数据库表字段映射到实体的命名策略。默认和Naming一样
		strategy.setColumnNaming(NamingStrategy.underline_to_camel);
		// 设置实体类使用Lombok模型
		strategy.setEntityLombokModel(true);
		// 设置Controller使用REST风格
		strategy.setRestControllerStyle(true);
		// 驼峰转连字符。
		strategy.setControllerMappingHyphenStyle(true);
		// 表前缀，用于过滤带有特定前缀的表。
		strategy.setTablePrefix(pc.getModuleName() + "_");
		// Boolean类型字段是否移除is前缀处理
		strategy.setEntityBooleanColumnRemoveIsPrefix(true);
		//生成 @RestController 控制器。
		strategy.setRestControllerStyle(true);

		// 自动填充字段配置
		strategy.setTableFillList(Arrays.asList(
				new TableFill("create_date", FieldFill.INSERT),
				new TableFill("change_date", FieldFill.INSERT_UPDATE),
				new TableFill("modify_date", FieldFill.UPDATE)
		));
		mpg.setStrategy(strategy);

		mpg.execute();
		System.out.println("生成成功！");
	}

}
