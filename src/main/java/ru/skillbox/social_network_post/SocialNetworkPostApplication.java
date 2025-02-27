package ru.skillbox.social_network_post;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableFeignClients
@SpringBootApplication
public class SocialNetworkPostApplication {

	public static void main(String[] args) {
		SpringApplication.run(SocialNetworkPostApplication.class, args);
	}

}