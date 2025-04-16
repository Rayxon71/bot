package tdiutf.uz.schedulebot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.pengrad.telegrambot.request.SendMessage;
import org.json.JSONObject;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ScheduleBot {
    private static TelegramBot bot;
    private static JSONObject schedule;

    public static void main(String[] args) {
        // Bot tokenini kiriting
        String botToken = "7934825748:AAHNOM0qiat4RyKsyMP1NL0tSXgPqA2biZA"; // @BotFather dan olingan token
        bot = new TelegramBot(botToken);


        // Jadvalni JSON fayldan o'qish
        loadSchedule();

        // Bot yangilanishlarini tinglash
        bot.setUpdatesListener(updates -> {
            for (Update update : updates) {
                handleUpdate(update);
            }
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }

    // Jadvalni JSON fayldan o'qish
    private static void loadSchedule() {
        try {
            String content = new String(Files.readAllBytes(Paths.get("schedule1.json")));
            schedule = new JSONObject(content);
        } 
        catch (IOException e) {
            // Agar fayl topilmasa, standart jadval yaratamiz
            schedule = new JSONObject();
            schedule.put("Dushanba", "Kompyuter tarmoqlari: 09:00-10:20(101-xona)\\n Dasturlash texnalogiyalari: 10:30-11:50(101-xona)");
            schedule.put("Seshanba", "Buyumlar interneti: 09:00-10:20(101-xona)\\n Java dasturlash tili: 10:30-11:50(101-xona)\\nJava dasturlash tili: 12:00-13:20(106-xona)");
            schedule.put("Chorshanba", "Dasturlash texnalogiyalari: 09:00-10:20(204-xona)\\n Buyumlar interneti: 10:30-11:50(204-xona)");
            schedule.put("Payshanba", "Kompyuter tarmoqlari: 09:00-10:20(101-xona)\\nAxborot xavfsizligi: 10:30-11:50(101-xona)\\nKompyuter tarmoqlari: 12:00-13:20(106-xona)\\nSkript tillari:14:00-15:20(108-xona)");
            schedule.put("Juma", "Skript tillari:09:00-10:20(208-xona)\\nSkript tillari: 12:00-13:20(106-xona)");
            saveSchedule();
        }
    }

    // Jadvalni JSON faylga saqlash
    private static void saveSchedule() {
        try (FileWriter file = new FileWriter("schedule.json")) {
            file.write(schedule.toString(4));
        } catch (IOException e) {
        }
    }

    // Yangilanishlarni boshqarish
    private static void handleUpdate(Update update) {
        if (update.message() != null && update.message().text() != null) {
            long chatId = update.message().chat().id();
            String messageText = update.message().text();

            if (messageText.equals("/start")) {
                sendWelcomeMessage(chatId);
            } else if (messageText.equals("Dars jadvali 📅")) {
                sendScheduleMenu(chatId);
            }
        } else if (update.callbackQuery() != null) {
            long chatId = update.callbackQuery().message().chat().id();
            String data = update.callbackQuery().data();
            sendDaySchedule(chatId, data);
        }
    }

    // Xush kelibsiz xabari
    private static void sendWelcomeMessage(long chatId) {
        String welcomeText = "🎓 *Dars jadvali botiga xush kelibsiz!*\n" +
                            "Hafta kunlariga qarab dars jadvalini ko’rishingiz mumkin.\n" +
                            "Quyidagi tugmani bosing 👇";

        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup(
                new String[]{"Dars jadvali 📅"}
        ).resizeKeyboard(true);

        SendMessage message = new SendMessage(chatId, welcomeText)
                .parseMode(com.pengrad.telegrambot.model.request.ParseMode.Markdown)
                .replyMarkup(keyboard);
        bot.execute(message);
    }

    // Jadval menyusini yuborish
    private static void sendScheduleMenu(long chatId) {
        String menuText = "📅 *Hafta kunini tanlang:*";

        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        keyboard.addRow(
                new InlineKeyboardButton("Dushanba").callbackData("Dushanba"),
                new InlineKeyboardButton("Seshanba").callbackData("Seshanba")
        );
        keyboard.addRow(
                new InlineKeyboardButton("Chorshanba").callbackData("Chorshanba"),
                new InlineKeyboardButton("Payshanba").callbackData("Payshanba")
        );
        keyboard.addRow(
                new InlineKeyboardButton("Juma").callbackData("Juma")
        );

        SendMessage message = new SendMessage(chatId, menuText)
                .parseMode(com.pengrad.telegrambot.model.request.ParseMode.Markdown)
                .replyMarkup(keyboard);
        bot.execute(message);
    }

    // Kunlik jadvalni yuborish
    private static void sendDaySchedule(long chatId, String day) {
        String scheduleText = schedule.has(day) ? schedule.getString(day) : "Bu kun uchun jadval yo’q.";
        String messageText = String.format("📚 *%s jadvali:*\n\n%s", day, scheduleText);

        SendMessage message = new SendMessage(chatId, messageText)
                .parseMode(com.pengrad.telegrambot.model.request.ParseMode.Markdown);
        bot.execute(message);
    }
}