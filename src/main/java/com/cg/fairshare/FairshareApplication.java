//package com.cg.fairshare;
//
//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//
//@SpringBootApplication
//public class FairshareApplication {
//
//	public static void main(String[] args) {
//		SpringApplication.run(FairshareApplication.class, args);
//	}
//
//}

package com.cg.fairshare;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.cg.fairshare")
public class FairshareApplication {
	public static void main(String[] args) {
		SpringApplication.run(FairshareApplication.class, args);
	}
}


