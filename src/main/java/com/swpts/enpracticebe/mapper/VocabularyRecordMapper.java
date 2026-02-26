package com.swpts.enpracticebe.mapper;

import com.swpts.enpracticebe.dto.response.VocabularyRecordResponse;
import com.swpts.enpracticebe.entity.VocabularyRecord;
import org.springframework.stereotype.Component;

@Component
public class VocabularyRecordMapper {

    public VocabularyRecordResponse entityToDto(VocabularyRecord from) {
        return VocabularyRecordResponse.builder()
                .id(from.getId())
                .englishWord(from.getEnglishWord())
                .userMeaning(from.getUserMeaning())
                .correctMeaning(from.getCorrectMeaning())
                .alternatives(from.getAlternatives())
                .synonyms(from.getSynonyms())
                .isCorrect(from.getIsCorrect())
                .testedAt(from.getTestedAt())
                .build();
    }
}
