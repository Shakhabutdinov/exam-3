package org.example.interfaces.impl;

import org.example.interfaces.UserService;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

public class UserServiceImpl implements UserService{
    public static final UserService userService = new UserServiceImpl();
    public static UserService getInstance(){return userService;}
    @Override
    public SendMessage openUserMenu(Message message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.setText("Choose menu");
        List<List<InlineKeyboardButton>> lists = oneLine(1, 2);
        lists.get(0).get(0).setText("Generate qr code");
        lists.get(0).get(0).setCallbackData("generate_qr_code");

        lists.get(0).get(1).setText("Read qr code");
        lists.get(0).get(1).setCallbackData("read_qr_code");

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(lists);

        sendMessage.setReplyMarkup(inlineKeyboardMarkup);

        return sendMessage;
    }
    public static List<List<InlineKeyboardButton>> oneLine(int rowNumber, int columnNumber) {
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        for (int i = 0; i < rowNumber; i++) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            for (int j = 0; j < columnNumber; j++) {
                row.add(new InlineKeyboardButton());
            }
            buttons.add(row);
        }
        return buttons;
    }
}
