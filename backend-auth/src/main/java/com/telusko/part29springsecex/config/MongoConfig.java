package com.telusko.part29springsecex.config;

import com.telusko.part29springsecex.model.TempUser;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.data.mongodb.core.index.IndexResolver;
import org.springframework.data.mongodb.core.index.MongoPersistentEntityIndexResolver;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.core.convert.converter.Converter;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Configuration
public class MongoConfig {

    @Bean
    public MongoCustomConversions customConversions() {
        List<Converter<?, ?>> converters = new ArrayList<>();
        converters.add(new LocalDateTimeToDateConverter());
        converters.add(new DateToLocalDateTimeConverter());
        return new MongoCustomConversions(converters);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initIndexes(ApplicationReadyEvent event) {
        // Get MongoTemplate from application context after it's fully initialized
        MongoTemplate mongoTemplate = event.getApplicationContext().getBean(MongoTemplate.class);
        MongoMappingContext mongoMappingContext = event.getApplicationContext().getBean(MongoMappingContext.class);
        
        // Ensure TTL index is created for TempUser collection
        IndexOperations indexOps = mongoTemplate.indexOps(TempUser.class);
        IndexResolver resolver = new MongoPersistentEntityIndexResolver(mongoMappingContext);
        resolver.resolveIndexFor(TempUser.class).forEach(indexOps::ensureIndex);
    }

    // Converter from LocalDateTime to Date for MongoDB storage
    public static class LocalDateTimeToDateConverter implements Converter<LocalDateTime, Date> {
        @Override
        public Date convert(LocalDateTime source) {
            return source == null ? null : Date.from(source.toInstant(ZoneOffset.UTC));
        }
    }

    // Converter from Date to LocalDateTime for MongoDB retrieval
    public static class DateToLocalDateTimeConverter implements Converter<Date, LocalDateTime> {
        @Override
        public LocalDateTime convert(Date source) {
            return source == null ? null : LocalDateTime.ofInstant(source.toInstant(), ZoneOffset.UTC);
        }
    }
}
