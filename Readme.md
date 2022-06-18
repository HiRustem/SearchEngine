# Поисковый движок

Данный проект - это дипломная работа, которая была создана в рамках курса профессии Java-разработчик образовательной платформы Skillbox. 
Поисковый движок индексирует сайты и позволяет осуществлять поиск по их контенту. 
Для работы веб приложение должно быть подключено к SQL базе данных, также имеется веб интерфейс.

В проекте используются Spring framework, MYSQL, JSOUP, библиотека lucene.morphology, JUNIT.

Перед началом работы необходимо создать SQL базу данных, после чего добавить ссылку, имя пользователя и пароль в файл application.yaml. Также вы можете настроить список сайтов для индексации.

## Пример заполнения

```javascript
spring.datasource.url: jdbc:mysql://localhost:3306/search_engine // ссылка базы данных
spring.datasource.username: root // имя пользователя
spring.datasource.password: root // пароль
spring.jpa.hibernate.ddl-auto: none

sites:
  list:
    - url: http://www.playback.ru
      name: PlayBack.ru
    - url: https://et-cetera.ru/mobile
      name: Et-Cetra
```