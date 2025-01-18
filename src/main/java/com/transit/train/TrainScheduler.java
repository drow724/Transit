package com.transit.train;

import com.transit.train.dto.SubwayTrainData;
import com.transit.train.dto.TrainDto;
import com.transit.train.event.TrainEventPublisherService;
import com.transit.train.response.TrainResponse;
import org.apache.hadoop.fs.FileSystem;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import org.apache.avro.Schema;
import org.apache.kafka.clients.producer.Producer;
import reactor.netty.http.client.HttpClient;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalTime;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

@Component
public class TrainScheduler {

    private final TrainEventPublisherService trainEventPublisherService;

    private final FileSystem fileSystem;

    private final Schema SCHEMA;

    private final Producer<String, SubwayTrainData> kafkaProducer;

    private final WebClient webClient = WebClient.builder()
            .baseUrl("http://swopenapi.seoul.go.kr/api/subway/")
            .codecs(configurer ->
                    configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024)) // 16MB로 증가
            .clientConnector(new ReactorClientHttpConnector(HttpClient.create().responseTimeout(Duration.ofSeconds(30L))))
            .build();

    private final String apiKey;

    public TrainScheduler(
            TrainEventPublisherService trainEventPublisherService,
            FileSystem fileSystem,
            Producer<String, SubwayTrainData> kafkaProducer,
            @Value("${seoul.open-api}") String apiKey
    ){
        this.trainEventPublisherService = trainEventPublisherService;
        this.fileSystem = fileSystem;
        this.kafkaProducer = kafkaProducer;
        this.apiKey = apiKey;
        try {
            ClassPathResource resource = new ClassPathResource("/avro/seoul.train.avsc");
            SCHEMA = new Schema.Parser().parse(resource.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Scheduled(fixedRate = 74000) // 74초 간격
    public void schedule() {
        LocalTime now = LocalTime.now();

        // 현재 시간 확인
        if (isWithinTimeRange(now)) {
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
                    .doOnError(error -> System.err.println("API 호출 오류: " + error.getMessage()))
                    .flatMap(trainResponse -> Mono.just(trainResponse.getRealtimeArrivalList()))
                    .flatMap(realtimeArrivalList -> {
                        realtimeArrivalList.stream().map(TrainDto::toSubwayTrainData).forEach(subwayTrainData -> {
                            Future<RecordMetadata> future = kafkaProducer.send(new ProducerRecord<>("subway", subwayTrainData));
//                            future.whenComplete((result, ex) -> {
//                                if(Objects.nonNull(ex)) {
//                                    ex.printStackTrace();
//                                }
//                            });
                        });

                        return Mono.just(realtimeArrivalList);
                    })
//                    .flatMap(realtimeArrivalList -> {
//                        String yyyMMdd = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
//                        String yyyyMMddHHmmss = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
//                        String hdfsPath = "/data/subway/" + yyyMMdd + "/" + yyyyMMddHHmmss + ".avro";
//
//                        Path filePath = new Path(hdfsPath);
//
//                        try (DataFileWriter<GenericRecord> dataFileWriter = new DataFileWriter<>(new GenericDatumWriter<>(SCHEMA))) {
//                            // 파일이 없으면 새로 생성
//                            dataFileWriter.create(SCHEMA, fileSystem.create(filePath));
//                            for(TrainDto realtimeArrival : realtimeArrivalList) {
//                                SubwayTrainData.
//                                GenericRecord subwayRecord = new GenericData.Record(SCHEMA);
//                                realtimeArrival.toRecord(subwayRecord);
//                                CompletableFuture<SendResult<String, SubwayTrainData>> future = kafkaTemplate.send("subway", subwayRecord);
//                                future.whenComplete((result, ex) -> {
//                                    if(Objects.nonNull(ex)) {
//                                        ex.printStackTrace();
//                                    }
//                                });
//                            }
//                        } catch (IOException e) {
//                            throw new RuntimeException(e);
//                        }
//
//                        return Mono.just(realtimeArrivalList);
//                    })
                    .subscribe(trainEventPublisherService::publishEvent);
        }
    }

    private boolean isWithinTimeRange(LocalTime now) {
        // 00:00 ~ 01:30 범위
        if (now.isAfter(LocalTime.MIDNIGHT) && now.isBefore(LocalTime.of(1, 30))) {
            return true;
        }
        // 05:00 ~ 24:00 범위
        return now.isAfter(LocalTime.of(5, 0)) || now.equals(LocalTime.of(5, 0));
    }
}
