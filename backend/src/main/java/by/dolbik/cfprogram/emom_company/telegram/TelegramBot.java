package by.dolbik.cfprogram.emom_company.telegram;

import by.dolbik.cfprogram.emom_company.dto.SessionDataDto;
import by.dolbik.cfprogram.emom_company.entity.TrainingSession;
import by.dolbik.cfprogram.emom_company.repository.TrainingSessionRep;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.translate.Language;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.Translation;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
@RequiredArgsConstructor
@Slf4j
public class TelegramBot extends TelegramLongPollingBot {

    public final BotProperties botProperties;
    private final TrainingSessionRep trainingSessionRep;
    private final Translate translate;

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
            log.info("Входящий запрос от %s: %s"
                    .formatted(update.getMessage().getFrom().getFirstName(),
                            update.getMessage().getText())
            );

            if (messageText.startsWith("/start")) {
                sendMessage(new SendMessage(chatId, "Введите дату в формате dd.MM.yyyy (пример 01.01.2025)"));
            } else {
                try {
                    final String targetDataStr = messageText.substring(0, 10);
                    final LocalDate sessionDate = LocalDate.parse(targetDataStr, formatter);
                    boolean translationToRus = false;

                    if(messageText.length() >= 13){
                        final String language = messageText.substring(11, 13);
                        translationToRus = language.equals("RU");
                    }

                    final TrainingSession trainingSession = trainingSessionRep.findFirstBySessionDate(sessionDate);

                    if(trainingSession == null){
                        sendMessage(new SendMessage(chatId, "Тренировка на %s, %s, отсутствует"
                                .formatted(formatter.format(sessionDate), dayOfWeekToRusString(sessionDate.getDayOfWeek()))));
                        return;
                    }

                    ObjectMapper objectMapper = new ObjectMapper();
                    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                    SessionDataDto sessionDataDto = objectMapper.readValue(trainingSession.getSessionData(), SessionDataDto.class);

                    if(translationToRus){
                        if(StringUtils.isNotEmpty(sessionDataDto.getSavedWorkout().getInstruction())) {
                            final String message = "Instructions on %s: %s".formatted(formatter.format(trainingSession.getSessionDate()),
                                    sessionDataDto.getSavedWorkout().getInstruction());
                            final String messageRus = translateToRus(message);

                            final StringBuilder sb = new StringBuilder();

                            sb.append(message);
                            sb.append(System.getProperty("line.separator"));
                            sb.append(System.getProperty("line.separator"));
                            sb.append("(").append(messageRus).append(")");

                            sendMessage(new SendMessage(chatId, sb.toString()));
                        }

                        for (SessionDataDto.WorkoutSet workoutSet : sessionDataDto.getSavedWorkout().getWorkoutSets()) {
                            final String message = "%s%s%s".formatted(
                                    workoutSet.getTitle() + ":",
                                    System.getProperty("line.separator"),
                                    workoutSet.getInstruction()
                            );
                            final String messageRus = translateToRus(message);

                            final StringBuilder sb = new StringBuilder();
                            sb.append(message);
                            sb.append(System.getProperty("line.separator"));
                            sb.append(System.getProperty("line.separator"));
                            sb.append("(").append(messageRus).append(")");

                            sendMessage(new SendMessage(chatId, sb.toString()));
                        }
                    } else {
                        final StringBuilder sb = new StringBuilder();

                        if(StringUtils.isNotEmpty(sessionDataDto.getSavedWorkout().getInstruction())) {
                            sb.append(System.getProperty("line.separator"));
                            sb.append("Instructions on %s: %s".formatted(formatter.format(trainingSession.getSessionDate()),
                                    sessionDataDto.getSavedWorkout().getInstruction()));
                            sb.append(System.getProperty("line.separator"));
                        }

                        for (SessionDataDto.WorkoutSet workoutSet : sessionDataDto.getSavedWorkout().getWorkoutSets()) {
                            sb.append(workoutSet.getTitle() + ":");
                            sb.append(System.getProperty("line.separator"));
                            sb.append(workoutSet.getInstruction());
                            sb.append(System.getProperty("line.separator"));
                            sb.append(System.getProperty("line.separator"));
                        }

                        final String responseMassageToBot = sb.toString();

                        for (String message : splitString(responseMassageToBot, 4096)) {
                            sendMessage(new SendMessage(chatId, message));
                        }
                    }
                }catch (Exception e){
                    sendMessage(new SendMessage(chatId, "Ошибка, обратитесь к администратору"));
                    log.error("Ошибка: {}", e.getMessage());
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

    private List<String> splitString(String text, int n) {
        List<String> results = new ArrayList<>();
        int length = text.length();

        for (int i = 0; i < length; i += n) {
            results.add(text.substring(i, Math.min(length, i + n)));
        }

        return results;
    }

    private String translateToRus(String text){
        Translation en = translate.translate(text,
                Translate.TranslateOption.sourceLanguage(translate.detect(text).getLanguage()),
                Translate.TranslateOption.targetLanguage("ru")
        );

        return en.getTranslatedText();
    }
}
