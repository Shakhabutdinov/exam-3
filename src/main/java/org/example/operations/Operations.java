package org.example.operations;

import org.example.model.BotState;
import org.example.model.Storage;
import org.example.model.User;

import java.util.ArrayList;
import java.util.List;

public class Operations {
    public static List<User> getUsers() {
        List<User> users = Storage.users;
        return users == null ? new ArrayList<>() : users;
    }
    public static User getUserWithChatId(String chatId) {
        return getUsers().stream().filter(user ->
                chatId.equals(user.getChatId())).findFirst().orElse(new User());
    }
    public static void login(String chatId) {
        User user = new User(chatId, BotState.MAIN_MENU);
        Storage.users.add(user);
    }
    public static void updateUserState(String chatId, BotState botState) {
        List<User> users = getUsers();
        User user = users.stream()
                .filter(u -> u.getChatId().equals(chatId))
                .findFirst()
                .orElse(new User());
        user.setBotState(botState);
    }


}
