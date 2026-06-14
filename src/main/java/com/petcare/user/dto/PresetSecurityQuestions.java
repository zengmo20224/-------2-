package com.petcare.user.dto;

import java.util.List;

/**
 * Preset security questions for password recovery.
 * Users must choose from this list — they cannot create custom questions.
 */
public final class PresetSecurityQuestions {

    private PresetSecurityQuestions() {}

    public static final List<String> QUESTIONS = List.of(
            "你的宠物叫什么名字？",
            "你最喜欢的食物是什么？",
            "你的家乡在哪里？",
            "你母亲的名字是什么？",
            "你就读的第一所学校叫什么？",
            "你最好的朋友叫什么名字？",
            "你最喜欢的电影是什么？",
            "你的宠物是什么品种？"
    );

    public static boolean isValid(String question) {
        return QUESTIONS.contains(question);
    }
}
