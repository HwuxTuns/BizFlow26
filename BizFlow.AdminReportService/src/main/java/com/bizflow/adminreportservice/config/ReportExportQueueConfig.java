package com.bizflow.adminreportservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class ReportExportQueueConfig {
	public static final String REPORT_EXPORT_TOPIC = "admin-report-export-jobs";

	@Bean
	public NewTopic reportExportTopic() {
		return TopicBuilder.name(REPORT_EXPORT_TOPIC)
				.partitions(1)
				.replicas(1)
				.build();
	}
}
