package com.transit.train;

import com.transit.train.dto.TrainDto;
import com.transit.train.event.TrainEventPublisherService;
import com.transit.train.response.TrainResponse;
import lombok.RequiredArgsConstructor;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Objects;

@Component
public class TrainScheduler {

    private final TrainEventPublisherService trainEventPublisherService;

    private final FileSystem fileSystem;

    private final Schema SCHEMA;

    private final WebClient webClient = WebClient.builder()
            .baseUrl("http://swopenapi.seoul.go.kr/api/subway/")
            .codecs(configurer ->
                    configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024)) // 16MB로 증가
            .build();

    private final String apiKey;

    public TrainScheduler(
            TrainEventPublisherService trainEventPublisherService,
            FileSystem fileSystem,
            ResourceLoader resourceLoader,
            @Value("${seoul.open-api}") String apiKey
    ){
        this.trainEventPublisherService = trainEventPublisherService;
        this.fileSystem = fileSystem;
        this.apiKey = apiKey;
        try {
            SCHEMA = new Schema.Parser().parse(TrainScheduler.class.getResourceAsStream("/avro/SubwayTrainData.avsc"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Scheduled(cron = "*/10 * * * * *")
    public void schedule() {
        webClient.get()
                .uri(apiKey + "/json/realtimeStationArrival/ALL")
                .exchangeToMono(clientResponse -> {
                    if (clientResponse.statusCode().is3xxRedirection()) {
                        String redirectUrl = Objects.requireNonNull(clientResponse.headers().asHttpHeaders().getLocation()).toString();
                        return webClient.get().uri(redirectUrl).retrieve().bodyToMono(TrainResponse.class);
                    } else {
                        return clientResponse.bodyToMono(TrainResponse.class);
                    }
                })
                .doOnError(error -> {
                    System.err.println("API 호출 오류: " + error.getMessage());
                })
                .flatMap(trainResponse -> Mono.just(trainResponse.getRealtimeArrivalList()))
                .flatMap(realtimeArrivalList -> {
                    realtimeArrivalList.forEach(realtimeArrival -> {
                        GenericData.Record subwayRecord = new GenericData.Record(SCHEMA);
                        subwayRecord.put("subwayId", "1065");
                        subwayRecord.put("trainLineNm", "검암행 - 계양방면 (막차)");
                        try (DataFileWriter<GenericRecord> dataFileWriter = new DataFileWriter<>(new GenericDatumWriter<>(SCHEMA))) {
                            dataFileWriter.create(SCHEMA, Paths.get("seoul_train_data.avro").toFile());
                            dataFileWriter.append(subwayRecord);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                    return Mono.just(realtimeArrivalList);
                })
                .subscribe(trainEventPublisherService::publishEvent);


    }
}
