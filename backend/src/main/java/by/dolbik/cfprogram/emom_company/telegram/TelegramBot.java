package by.dolbik.cfprogram.emom_company.telegram;

import by.dolbik.cfprogram.emom_company.dto.SessionDataDto;
import by.dolbik.cfprogram.emom_company.entity.TrainingSession;
import by.dolbik.cfprogram.emom_company.repository.TrainingSessionRep;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

@Component
@RequiredArgsConstructor
@Slf4j
public class TelegramBot extends TelegramLongPollingBot {

    public final BotProperties botProperties;
    private final TrainingSessionRep trainingSessionRep;

    @Override
    public String getBotUsername() {
        return botProperties.getName();
    }

    @Override
    public String getBotToken() {
        return botProperties.getToken();
    }

    final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @Override
    public void onUpdateReceived(Update update) {
        final String chatId = update.getMessage().getChatId().toString();

        if (update.hasMessage() && update.getMessage().hasText()) {
            final String messageText = update.getMessage().getText();

            if (messageText.startsWith("/start")) {
                sendMessage(new SendMessage(chatId, "Введите дату в формате dd.MM.yyyy (пример 01.01.2025)"));
            } else {
                try {
                    final LocalDate sessionDate = LocalDate.parse(messageText, formatter);

                    final TrainingSession trainingSession = trainingSessionRep.findFirstBySessionDate(sessionDate);

                    if(trainingSession == null){
                        sendMessage(new SendMessage(chatId, "Тренировка на %s, %s, отсутствует"
                                .formatted(formatter.format(sessionDate), dayOfWeekToRusString(sessionDate.getDayOfWeek()))));
                        return;
                    }

                    ObjectMapper objectMapper = new ObjectMapper();
                    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                    SessionDataDto sessionDataDto = objectMapper.readValue(trainingSession.getSessionData(), SessionDataDto.class);

                    final StringBuilder sb = new StringBuilder();

                    if(StringUtils.isNotEmpty(sessionDataDto.getSavedWorkout().getInstruction())) {
                        sb.append(System.getProperty("line.separator"));
                        sb.append("Instructions on %s: %s".formatted(trainingSession.getSessionDate(), sessionDataDto.getSavedWorkout().getInstruction()));
                        sb.append(System.getProperty("line.separator"));
                    }

                    for (SessionDataDto.WorkoutSet workoutSet : sessionDataDto.getSavedWorkout().getWorkoutSets()) {
                        sb.append(workoutSet.getTitle() + ":");
                        sb.append(System.getProperty("line.separator"));
                        sb.append(workoutSet.getInstruction());
                        sb.append(System.getProperty("line.separator"));
                        sb.append(System.getProperty("line.separator"));
                    }

                    sendMessage(new SendMessage(chatId, sb.toString()));
                }catch (Exception e){
                    sendMessage(new SendMessage(chatId, "Введена некорректная дата"));
                    sendMessage(new SendMessage(chatId, "Введите дату в формате dd.MM.yyyy (пример 01.01.2025)"));
                }
            }
        }
    }

    private void sendMessage(SendMessage sendMessage) {
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }

    private String dayOfWeekToRusString(DayOfWeek dayOfWeek){
        return dayOfWeek.getDisplayName(TextStyle.FULL, Locale.forLanguageTag("ru"));
    }
}
