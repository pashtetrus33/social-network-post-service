package ru.skillbox.social_network_post.utils;


import java.util.List;
import java.util.Random;


public class CommentUtils {

    private static final Random random = new Random();

    // Private constructor to prevent instantiation
    private CommentUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    private static final List<String> COMMENTS = List.of(
            "😊 Отличный пост! Спасибо за интересные мысли. Это верно 🔥",
            "Очень интересно, 🤔 не думал об этом раньше! Это правильно ✅",
            "Полностью согласен, 👍 отличное объяснение. Действительно так 👏",
            "🤩 Интересная точка зрения! Подтверждаю 💡",
            "Не уверен, 🤷️ но звучит логично. Возможно так 🤓",
            "Круто, спасибо за информацию! 🎯 Доказано, что 📚",
            "Можно подробнее? Интересно разобраться. 📖 Я читал, что 🔍",
            "Не согласен, 🤨 но уважаю вашу точку зрения. Есть мнение, что 🤝",
            "Это действительно так? 🧐 Многие говорят, что 🤔",
            "Спасибо! ✅ Теперь мне стало понятнее. Есть подтверждение, что 📜",
            "Вы молодец! 🎓 Очень четко и по делу. Исследования показывают, что 📊",
            "Ух ты! 🤯 Никогда не задумывался об этом. В научных кругах говорят, что 🔬",
            "Это изменило мой взгляд на вещи. 🌍 Важно понимать, что 🔄",
            "Как раз искал такую информацию, благодарю! 🏆 Доказано учеными, что 📑",
            "Кажется, здесь ошибка. ❌ Проверьте ещё раз! Некоторые считают, что 🤷",
            "Надо будет попробовать, спасибо! 🛠️ В теории это значит, что ⚙️",
            "Очень полезно! 📌 Надо сохранить себе. Любопытно, что 💡",
            "Вау, 😲 даже не знал, что так можно! Практика показывает, что ⚡",
            "Можете объяснить это поподробнее? 👀 Важно отметить, что ❓",
            "Супер, теперь понятно! 🙌 Благодарю! Некоторые исследования говорят, что 📚"
    );

    private static final List<String> REPLIES = List.of(
            "Согласен! 👍🔥",
            "Спасибо! 😊🙏",
            "Хороший вопрос! 🤔",
            "Интересное мнение! 🤓",
            "Точно подмечено! 🎯",
            "Не думал об этом! 🤯",
            "Вот это да! 😲",
            "Полностью поддерживаю! 💪✅",
            "Есть над чем подумать! 🤨",
            "Рад, что полезно! 😃",
            "Согласен на 100%! 💯",
            "Так и есть! 📚",
            "Думаю, ты прав! 🤝",
            "Спасибо за обратную связь! 🙌",
            "Ну тут сложно спорить! 😃",
            "Проверю, спасибо за наводку! 🔍",
            "Уважительное мнение! 🧐",
            "Хорошее замечание! ✍️",
            "Спасибо за поддержку! 🤗",
            "Это звучит разумно! 🧠"
    );

    // Метод для получения случайного комментария
    public static String getRandomComment(String additionalText) {
        return COMMENTS.get(random.nextInt(COMMENTS.size())) + " " + additionalText;
    }

    // Метод для получения случайного ответа
    public static String getRandomReply() {
        return REPLIES.get(random.nextInt(REPLIES.size()));
    }
}