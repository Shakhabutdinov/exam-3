package org.example.telegram;

import com.google.zxing.EncodeHintType;
import com.google.zxing.NotFoundException;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.example.interfaces.impl.UserServiceImpl;
import org.example.model.BotState;
import org.example.model.User;
import org.example.operations.GenerateQrCodes;
import org.example.operations.Operations;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.example.operations.GenerateQrCodes.createQRImage;

public class TelegramBotApiDemo extends TelegramLongPollingBot {
    private static int id = 0;
    private final String PATH = "src/main/resources";

    @Override
    public String getBotUsername() {
        return "B26telegrambot";
    }

    @Override
    public String getBotToken() {
        return "5987766044:AAGPw9u8_djTnA8KihWO7j9w6BxeS13IWBk";
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            List<PhotoSize> photo = update.getMessage().getPhoto();
            Message message = update.getMessage();
            String text = message.getText();
            User user = Operations.getUserWithChatId(message.getChatId().toString());
            if (text != null && text.equals("/start")) {
                if (user.getChatId() == null) {
                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setChatId(message.getChatId().toString());
                    sendMessage.setText("""
                            <b> Welcome to our crazy prazy telegram bot</b> ????
                            """);
                    sendMessage.setParseMode("HTML");
                    try {
                        execute(sendMessage);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                    Operations.login(message.getChatId().toString());
                    sendMessage(UserServiceImpl.getInstance().openUserMenu(message));
                }
            } else if (user.getBotState().equals(BotState.GENERATE_QR_CODE)) {
                String qrCodeText = update.getMessage().getText();
                String filePath = PATH + id + ".png";
                int size = 125;
                String fileType = "png";
                File qrFile = new File(filePath);
                try {
                    createQRImage(qrFile, qrCodeText, size, fileType);
                } catch (WriterException | IOException e) {
                    e.printStackTrace();
                }
                Operations.updateUserState(message.getChatId().toString(), BotState.MAIN_MENU);
                SendPhoto sendPhoto = new SendPhoto();
                sendPhoto.setChatId(message.getChatId().toString());
                File file = new File(PATH + id + ".png");
                sendPhoto.setPhoto(new InputFile(file));
                try {
                    execute(sendPhoto);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
                id++;
                sendMessage(UserServiceImpl.getInstance().openUserMenu(message));
            } else if (photo != null && !photo.isEmpty() && user.getBotState().equals(BotState.READ_QRCODE)) {
                photo.sort(Comparator.comparing(PhotoSize::getFileSize).reversed());
                PhotoSize photoSize = photo.get(0);
                GetFile getFile = new GetFile(photoSize.getFileId());
                try {
                    org.telegram.telegrambots.meta.api.objects.File file = execute(getFile);
                    java.io.File file1 = new java.io.File("src/main/resources" + id + "." + file.getFilePath().split("\\.")[1]);
                    downloadFile(file, file1);
                    String charset = "UTF-8";
                    Map<EncodeHintType, ErrorCorrectionLevel> hintMap = new HashMap<EncodeHintType, ErrorCorrectionLevel>();
                    hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
                    String s = GenerateQrCodes.readQRCode(file1.getPath(), charset, hintMap);
                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setChatId(message.getChatId());
                    sendMessage.setText(s);
                    execute(sendMessage);
                } catch (TelegramApiException | NotFoundException | IOException e) {
                    e.printStackTrace();
                }

                sendMessage(UserServiceImpl.getInstance().openUserMenu(message));
                Operations.updateUserState(message.getChatId().toString(), BotState.MAIN_MENU);

            }
        }
        if (update.hasCallbackQuery()) {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            User user = Operations.getUserWithChatId(update.getCallbackQuery().getMessage().getChatId().toString());
            String data = update.getCallbackQuery().getData();
            if (data.equals("generate_qr_code") && user.getBotState().equals(BotState.MAIN_MENU)) {
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(callbackQuery.getMessage().getChatId().toString());
                sendMessage.setText("Enter qr code name");
                try {
                    execute(sendMessage);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
                Operations.updateUserState(callbackQuery.getMessage().getChatId().toString(), BotState.GENERATE_QR_CODE);
            } else if (data.equals("read_qr_code") && user.getBotState().equals(BotState.MAIN_MENU)) {
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(callbackQuery.getMessage().getChatId().toString());
                sendMessage.setText("Send qr code ");
                try {
                    execute(sendMessage);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
                Operations.updateUserState(callbackQuery.getMessage().getChatId().toString(), BotState.READ_QRCODE);
            }
        }
    }

    public void sendMessage(Object object) {
        try {
            if (object instanceof SendMessage) {
                execute((SendMessage) object);
            }
            if (object instanceof SendPhoto) {
                execute((SendPhoto) object);
            }
            if (object instanceof EditMessageReplyMarkup) {
                execute((EditMessageReplyMarkup) object);
            }
            if (object instanceof EditMessageText) {
                execute((EditMessageText) object);
            }
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

}
