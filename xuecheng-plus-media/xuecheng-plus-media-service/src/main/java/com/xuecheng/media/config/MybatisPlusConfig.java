package com.xuecheng.media.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.apache.ibatis.reflection.MetaObject;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

/**
 * <P>
 * 		Mybatis-Plus 配置
 * </p>
 */
@Configuration
@MapperScan("com.xuecheng.media.mapper")
public class MybatisPlusConfig {
	/**
	 * 新的分页插件
	 * 需要设置 MybatisConfiguration#useDeprecatedExecutor = false
	 * 避免缓存出现问题(该属性会在旧插件移除后一同移除)
	 */
	@Bean
	public MybatisPlusInterceptor mybatisPlusInterceptor() {
		MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
		interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
		return interceptor;
	}

	@Bean
	MetaObjectHandler metaObjectHandler(){
		return new MetaObjectHandler() {
			@Override
			public void insertFill(MetaObject metaObject) {
				this.strictInsertFill(metaObject, "createDate", LocalDateTime.class, LocalDateTime.now());
				this.strictInsertFill(metaObject, "changeDate", LocalDateTime.class, LocalDateTime.now());
			}

			@Override
			public void updateFill(MetaObject metaObject) {
				this.strictInsertFill(metaObject, "changeDate", LocalDateTime.class, LocalDateTime.now());
			}
		};
	}


}