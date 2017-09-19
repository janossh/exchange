package ua.moyo.rabbitmq;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
//import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;


@EntityScan(
		basePackageClasses = { DemoApplication.class, Jsr310JpaConverters.class }

)
@ComponentScan
@SpringBootApplication
@EnableScheduling
@EnableAsync
public class DemoApplication extends SpringBootServletInitializer {


	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	private static Class<DemoApplication> applicationClass = DemoApplication.class;

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(applicationClass);
	}



}
