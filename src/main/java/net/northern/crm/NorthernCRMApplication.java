package net.northern.crm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@EnableWebMvc
@SpringBootApplication(scanBasePackages = "net.northern.crm")
@ConfigurationPropertiesScan(basePackages = "net.northern.crm.config")
public class NorthernCRMApplication {

	private static final Logger logger = LoggerFactory.getLogger(NorthernCRMApplication.class);

	//TODO Create a default user and a default shipping location (named RECEIVING)

	public static void main(String[] args) {
		SpringApplication.run(NorthernCRMApplication.class, args);
		logger.info("Server Ready");
	}

//	public static DiscoveryAPIResponse getDiscoveryAPIResponse(EnvConfig envConfig) throws ConnectionException {
//		return discoveryAPIResponse == null ? new DiscoveryAPIClient().callDiscoveryAPI(envConfig.isProduction() ? Environment.PRODUCTION : Environment.SANDBOX) : discoveryAPIResponse;
//	}

}
