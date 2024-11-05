package by.dolbik.cfprogram.emom_company.controller;

import by.dolbik.cfprogram.emom_company.dto.RangeDto;
import by.dolbik.cfprogram.emom_company.dto.SessionDataDto;
import by.dolbik.cfprogram.emom_company.entity.TrainingSession;
import by.dolbik.cfprogram.emom_company.repository.TrainingSessionRep;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("data")
public class DataController {
    private final TrainingSessionRep trainingSessionRep;

    @PostMapping
    public ResponseEntity<Void> loadDataToDb(){
        final LocalDate startDate = LocalDate.parse("2023-01-01");
        LocalDate targetDate = startDate;

        while (targetDate.getYear() < LocalDate.now().getYear()
                || ((targetDate.getYear() == LocalDate.now().getYear()) && (targetDate.getMonthValue() <= LocalDate.now().getMonthValue()))){
            final LocalDate firstDayOfMonth = targetDate.with(TemporalAdjusters.firstDayOfMonth());
            final LocalDate lastDayOfMonth = targetDate.with(TemporalAdjusters.lastDayOfMonth());
            log.info("Загрузка данных %s-%s".formatted(firstDayOfMonth, lastDayOfMonth));

            final RestTemplate restTemplate = new RestTemplate();

            final HttpHeaders headers = new HttpHeaders();
            headers.set("session-token", "10db3b44c73316cbb62c597ac2800c82");
            final HttpEntity<String> requestEntity = new HttpEntity<>(headers);

            final String rangeUrl = "https://api.trainheroic.com/public/programworkout/range";

            final URI rangeUri = UriComponentsBuilder.fromUriString(rangeUrl)
                    .queryParam("startDate", firstDayOfMonth)
                    .queryParam("endDate", lastDayOfMonth)
                    .build()
                    .toUri();

            final ResponseEntity<List<RangeDto>> exchange =
                    restTemplate.exchange(rangeUri, HttpMethod.GET, requestEntity, new ParameterizedTypeReference<>(){});

            final List<RangeDto> rangeDtoList = exchange.getBody();

            assert rangeDtoList != null;

            for (RangeDto rangeDto : rangeDtoList) {
                final String sessionUrl = "https://api.trainheroic.com/3.0/athlete/savedworkout/{sessionID}";

                final Map<String, String> urlParams = new HashMap<>();
                urlParams.put("sessionID", rangeDto.getId().toString());

                final URI sessionUri = UriComponentsBuilder.fromUriString(sessionUrl)
                        .buildAndExpand(urlParams)
                        .toUri();

                final ResponseEntity<String> sessionRs
                        = restTemplate.exchange(sessionUri, HttpMethod.GET, requestEntity, String.class);

                TrainingSession trainingSession = TrainingSession.builder()
                        .id(rangeDto.getId())
                        .sessionDate(rangeDto.getDate())
                        .sessionData(sessionRs.getBody())
                        .build();

                trainingSessionRep.save(trainingSession);
            }

            targetDate = targetDate.plusMonths(1);
        }

        return ResponseEntity.ok().build();
    }
}
