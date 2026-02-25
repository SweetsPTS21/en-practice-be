package com.swpts.enpracticebe.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swpts.enpracticebe.dto.*;
import com.swpts.enpracticebe.entity.ReviewSession;
import com.swpts.enpracticebe.entity.VocabularyRecord;
import com.swpts.enpracticebe.repository.ReviewSessionRepository;
import com.swpts.enpracticebe.repository.VocabularyRecordRepository;
import com.swpts.enpracticebe.util.AuthUtil;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class RecordService {
    private final VocabularyRecordRepository recordRepository;
    private final ReviewSessionRepository reviewSessionRepository;
    private final ObjectMapper objectMapper;
    private final AuthUtil authUtil;

    public List<VocabularyRecord> getAllRecords() {
        UUID userId = authUtil.getUserId();
        return recordRepository.findByUserIdOrderByTestedAtDesc(userId);
    }

    public VocabularyRecord createRecord(RecordRequest request) {
        UUID userId = authUtil.getUserId();
        VocabularyRecord record = VocabularyRecord.builder()
                .userId(userId)
                .englishWord(request.getEnglishWord())
                .userMeaning(request.getUserMeaning())
                .correctMeaning(request.getCorrectMeaning())
                .alternatives(request.getAlternatives())
                .synonyms(request.getSynonyms())
                .isCorrect(request.getIsCorrect())
                .build();
        return recordRepository.save(record);
    }

    @Transactional
    public void deleteRecord(UUID recordId) {
        UUID userId = authUtil.getUserId();
        VocabularyRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new NoSuchElementException("Record not found"));
        if (!record.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized to delete this record");
        }
        recordRepository.delete(record);
    }

    @Transactional
    public void deleteAllRecords() {
        UUID userId = authUtil.getUserId();
        recordRepository.deleteAllByUserId(userId);
    }

    @Transactional
    public ImportResponse importData(ImportRequest request) {
        UUID userId = authUtil.getUserId();
        int importedRecords = 0;
        int importedSessions = 0;

        // Import vocabulary records
        for (ImportRequest.ImportRecordDto dto : request.getRecords()) {
            VocabularyRecord record = VocabularyRecord.builder()
                    .userId(userId)
                    .englishWord(dto.getEnglishWord())
                    .userMeaning(dto.getUserMeaning())
                    .correctMeaning(dto.getCorrectMeaning())
                    .alternatives(dto.getAlternatives() != null ? dto.getAlternatives() : new ArrayList<>())
                    .synonyms(dto.getSynonyms() != null ? dto.getSynonyms() : new ArrayList<>())
                    .isCorrect(dto.getIsCorrect())
                    .testedAt(parseTimestamp(dto.getTimestamp()))
                    .build();
            recordRepository.save(record);
            importedRecords++;
        }

        // Import review sessions
        for (ImportRequest.ImportReviewSessionDto dto : request.getReviewSessions()) {
            ReviewSession session = ReviewSession.builder()
                    .userId(userId)
                    .filter(dto.getFilter() != null ? dto.getFilter() : "all")
                    .total(dto.getTotal())
                    .correct(dto.getCorrect())
                    .incorrect(dto.getIncorrect())
                    .accuracy(dto.getAccuracy())
                    .words(dto.getWords() != null ? dto.getWords() : new ArrayList<>())
                    .reviewedAt(parseTimestamp(dto.getTimestamp()))
                    .build();
            reviewSessionRepository.save(session);
            importedSessions++;
        }

        return ImportResponse.builder()
                .importedRecords(importedRecords)
                .importedSessions(importedSessions)
                .build();
    }

    public StatsResponse getStats(String period) {
        UUID userId = authUtil.getUserId();
        Instant since = getStartOfPeriod(period);

        long total = recordRepository.countByUserIdAndTestedAtAfter(userId, since);
        long correct = recordRepository.countCorrectByUserIdAndTestedAtAfter(userId, since);
        long incorrect = total - correct;
        int accuracy = total > 0 ? (int) Math.round((double) correct / total * 100) : 0;

        // Frequently wrong words (all time)
        List<FrequentlyWrongWord> frequentlyWrong = recordRepository.findFrequentlyWrongWords(userId)
                .stream()
                .map(row -> FrequentlyWrongWord.builder()
                        .word((String) row[0])
                        .correctMeaning((String) row[1])
                        .wrongCount(((Number) row[2]).longValue())
                        .lastAttempt(row[3] instanceof Timestamp ts ? ts.toInstant() : (Instant) row[3])
                        .build())
                .collect(Collectors.toList());

        return StatsResponse.builder()
                .total(total)
                .correct(correct)
                .incorrect(incorrect)
                .accuracy(accuracy)
                .frequentlyWrong(frequentlyWrong)
                .filtered(new ArrayList<>())
                .build();
    }

    public List<ChartEntry> getChartData(String period) {
        UUID userId = authUtil.getUserId();
        ZoneId zone = ZoneId.of("UTC");
        ZonedDateTime now = ZonedDateTime.now(zone);
        List<VocabularyRecord> records;
        List<ChartEntry> entries = new ArrayList<>();

        switch (period) {
            case "day" -> {
                // 24 entries, each hour in last 24 hours
                Instant since = now.minusHours(24).toInstant();
                records = recordRepository.findByUserIdAndTestedAtGreaterThanEqual(userId, since);

                for (int i = 23; i >= 0; i--) {
                    ZonedDateTime hourStart = now.minusHours(i).truncatedTo(java.time.temporal.ChronoUnit.HOURS);
                    ZonedDateTime hourEnd = hourStart.plusHours(1);
                    String name = hourStart.format(DateTimeFormatter.ofPattern("HH:00"));

                    long c = records.stream().filter(r -> {
                        ZonedDateTime t = r.getTestedAt().atZone(zone);
                        return !t.isBefore(hourStart) && t.isBefore(hourEnd);
                    }).filter(VocabularyRecord::isCorrect).count();

                    long ic = records.stream().filter(r -> {
                        ZonedDateTime t = r.getTestedAt().atZone(zone);
                        return !t.isBefore(hourStart) && t.isBefore(hourEnd);
                    }).filter(r -> !r.isCorrect()).count();

                    entries.add(ChartEntry.builder().name(name).correct(c).incorrect(ic).total(c + ic).build());
                }
            }
            case "week" -> {
                // 7 entries, each day in last 7 days
                Instant since = now.minusDays(7).truncatedTo(java.time.temporal.ChronoUnit.DAYS).toInstant();
                records = recordRepository.findByUserIdAndTestedAtGreaterThanEqual(userId, since);

                for (int i = 6; i >= 0; i--) {
                    ZonedDateTime dayStart = now.minusDays(i).truncatedTo(java.time.temporal.ChronoUnit.DAYS);
                    ZonedDateTime dayEnd = dayStart.plusDays(1);
                    String name = dayStart.format(DateTimeFormatter.ofPattern("dd/MM"));

                    long c = records.stream().filter(r -> {
                        ZonedDateTime t = r.getTestedAt().atZone(zone);
                        return !t.isBefore(dayStart) && t.isBefore(dayEnd);
                    }).filter(VocabularyRecord::isCorrect).count();

                    long ic = records.stream().filter(r -> {
                        ZonedDateTime t = r.getTestedAt().atZone(zone);
                        return !t.isBefore(dayStart) && t.isBefore(dayEnd);
                    }).filter(r -> !r.isCorrect()).count();

                    entries.add(ChartEntry.builder().name(name).correct(c).incorrect(ic).total(c + ic).build());
                }
            }
            case "month" -> {
                // 30 entries, each day in last 30 days
                Instant since = now.minusDays(30).truncatedTo(java.time.temporal.ChronoUnit.DAYS).toInstant();
                records = recordRepository.findByUserIdAndTestedAtGreaterThanEqual(userId, since);

                for (int i = 29; i >= 0; i--) {
                    ZonedDateTime dayStart = now.minusDays(i).truncatedTo(java.time.temporal.ChronoUnit.DAYS);
                    ZonedDateTime dayEnd = dayStart.plusDays(1);
                    String name = dayStart.format(DateTimeFormatter.ofPattern("dd/MM"));

                    long c = records.stream().filter(r -> {
                        ZonedDateTime t = r.getTestedAt().atZone(zone);
                        return !t.isBefore(dayStart) && t.isBefore(dayEnd);
                    }).filter(VocabularyRecord::isCorrect).count();

                    long ic = records.stream().filter(r -> {
                        ZonedDateTime t = r.getTestedAt().atZone(zone);
                        return !t.isBefore(dayStart) && t.isBefore(dayEnd);
                    }).filter(r -> !r.isCorrect()).count();

                    entries.add(ChartEntry.builder().name(name).correct(c).incorrect(ic).total(c + ic).build());
                }
            }
            default -> throw new IllegalArgumentException("Invalid period: " + period);
        }

        return entries;
    }

    public int getStreak() {
        UUID userId = authUtil.getUserId();
        List<java.sql.Date> dates = recordRepository.findDistinctRecordDates(userId);
        if (dates.isEmpty())
            return 0;

        LocalDate today = LocalDate.now(ZoneId.of("UTC"));
        int streak = 0;

        for (java.sql.Date sqlDate : dates) {
            LocalDate recordDate = sqlDate.toLocalDate();
            LocalDate expected = today.minusDays(streak);

            if (recordDate.equals(expected)) {
                streak++;
            } else {
                break;
            }
        }

        return streak;
    }

    public List<ReviewWordDto> getReviewWords(String filter, int limit) {
        UUID userId = authUtil.getUserId();
        List<Object[]> rawWords;

        switch (filter) {
            case "today" -> rawWords = recordRepository.findUniqueWordsSince(userId, getStartOfToday());
            case "week" -> rawWords = recordRepository.findUniqueWordsSince(userId, getStartOfWeek());
            case "month" -> rawWords = recordRepository.findUniqueWordsSince(userId, getStartOfMonth());
            case "wrong" -> rawWords = recordRepository.findUniqueWrongWords(userId);
            default -> rawWords = recordRepository.findAllUniqueWords(userId);
        }

        List<ReviewWordDto> words = rawWords.stream()
                .map(row -> ReviewWordDto.builder()
                        .englishWord((String) row[0])
                        .correctMeaning((String) row[1])
                        .alternatives(parseJsonList(row[2]))
                        .build())
                .collect(Collectors.toList());

        // Shuffle
        Collections.shuffle(words);

        // Limit
        if (words.size() > limit) {
            words = words.subList(0, limit);
        }

        return words;
    }

    public ReviewCountsDto getReviewCounts() {
        UUID userId = authUtil.getUserId();
        return ReviewCountsDto.builder()
                .today(recordRepository.countUniqueWordsSince(userId, getStartOfToday()))
                .week(recordRepository.countUniqueWordsSince(userId, getStartOfWeek()))
                .month(recordRepository.countUniqueWordsSince(userId, getStartOfMonth()))
                .wrong(recordRepository.countUniqueWrongWords(userId))
                .all(recordRepository.countAllUniqueWords(userId))
                .build();
    }

    // ---- Helper methods ----

    private Instant getStartOfPeriod(String period) {
        return switch (period) {
            case "day" -> getStartOfToday();
            case "week" -> getStartOfWeek();
            case "month" -> getStartOfMonth();
            default -> throw new IllegalArgumentException("Invalid period: " + period);
        };
    }

    private Instant getStartOfToday() {
        return LocalDate.now(ZoneId.of("UTC")).atStartOfDay(ZoneId.of("UTC")).toInstant();
    }

    private Instant getStartOfWeek() {
        return LocalDate.now(ZoneId.of("UTC"))
                .with(DayOfWeek.MONDAY)
                .atStartOfDay(ZoneId.of("UTC"))
                .toInstant();
    }

    private Instant getStartOfMonth() {
        return LocalDate.now(ZoneId.of("UTC"))
                .withDayOfMonth(1)
                .atStartOfDay(ZoneId.of("UTC"))
                .toInstant();
    }

    private Instant parseTimestamp(String timestamp) {
        if (timestamp == null || timestamp.isBlank()) {
            return Instant.now();
        }
        try {
            return Instant.parse(timestamp);
        } catch (Exception e) {
            return Instant.now();
        }
    }

    @SuppressWarnings("unchecked")
    private List<String> parseJsonList(Object jsonObj) {
        if (jsonObj == null)
            return new ArrayList<>();
        try {
            if (jsonObj instanceof String jsonStr) {
                return objectMapper.readValue(jsonStr, new TypeReference<List<String>>() {
                });
            }
            if (jsonObj instanceof List) {
                return (List<String>) jsonObj;
            }
        } catch (Exception e) {
            // ignore
        }
        return new ArrayList<>();
    }
}
