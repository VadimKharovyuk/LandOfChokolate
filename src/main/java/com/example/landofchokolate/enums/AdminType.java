package com.example.landofchokolate.enums;
import lombok.Getter;

@Getter
public enum AdminType {
    NONE("Нет доступа"),            // не админ
    VIEWER("Только просмотр"),      // может только просматривать
    EDITOR("Редактирование"),       // может редактировать
    SUPER_ADMIN("Полный доступ");   // может всё, включая создание новых админов

    private final String displayName;

    AdminType(String displayName) {
        this.displayName = displayName;
    }
}
