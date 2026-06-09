package com.bizflow.adminreportservice.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.bizflow.adminreportservice.entity.ReportTemplate;
import com.bizflow.adminreportservice.repository.ReportTemplateRepository;

@Configuration
public class ReportTemplateSeeder {

    @Bean
    ApplicationRunner seedReportTemplates(ReportTemplateRepository repository) {
        return args -> {
            if (repository.count() > 0) {
                return;
            }

            repository.save(create("SALES_SUMMARY", "B\u00e1o c\u00e1o doanh thu (t\u1ed5ng h\u1ee3p)", "MONTHLY"));
            repository.save(create("INVENTORY_LOW", "B\u00e1o c\u00e1o t\u1ed3n kho th\u1ea5p", "DAILY"));
            repository.save(create("ORDERS_STATUS", "B\u00e1o c\u00e1o \u0111\u01a1n h\u00e0ng theo tr\u1ea1ng th\u00e1i", "WEEKLY"));
        };
    }

    private static ReportTemplate create(String code, String name, String frequency) {
        ReportTemplate t = new ReportTemplate();
        t.setCode(code);
        t.setName(name);
        t.setFrequency(frequency);
        return t;
    }
}
