package com.ces.exam;

import java.util.TimeZone;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ExamSystemApplication {

	// The app stores and displays naive local timestamps (LocalDateTime), so the
	// JVM must run in the same wall-clock zone that admins enter and candidates
	// see. Without this the container defaults to UTC, making freshly created
	// exam links fail their start-date check ("Exam has not started yet") for the
	// duration of the UTC offset.
	private static final String APP_TIME_ZONE = "Asia/Baku";

	public static void main(String[] args) {
		TimeZone.setDefault(TimeZone.getTimeZone(APP_TIME_ZONE));
		SpringApplication.run(ExamSystemApplication.class, args);
	}

	@PostConstruct
	void enforceTimeZone() {
		TimeZone.setDefault(TimeZone.getTimeZone(APP_TIME_ZONE));
	}

}
