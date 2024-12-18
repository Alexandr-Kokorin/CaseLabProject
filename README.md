![Project](https://github.com/Alexandr-Kokorin/CaseLabProject/actions/workflows/project.yml/badge.svg)

# Проект Green СР 2024 Java

## Описание решения
**Стек технологий:**
* Java 21
* Spring Framework
* Liquabase
* PostgreSQL
* Maven
* Lombok
* Testcontainers
* JUnit
* Docker/Docker Compose
* Grafana
* Kafka
* Prometheus
* Elastic Search
* Logstash
* Minio

---

## Инструкция по запуску
**Требования: в системе должен быть установлен docker и docker-compose**

1. Скачайте архив с репозиторием в удобное место у себя на компьютере:
    ```
    git clone https://github.com/Alexandr-Kokorin/CaseLabProject.git
    ```
2. Далее перейдите в директорию с файлом *docker-compose.yml*:
    ```
    cd CaseLabProject
    cd docker
    ```
3. Теперь можно указать почту для нотификаций, указав адрес электронной почты и пароль приложения:
   * С помощью любого удобного редактора откройте файл `.env`, находящийся в директории docker
   и отредактируйте следующую переменную:
     ```
     ...
     MAIL_USERNAME=<EMAIL>
     MAIL_PASSWORD=<PASSWORD>
     ...
     Вместо `<EMAIL>` необходимо вставить адрес электронной почты.
     Вместо `<PASSWORD>` необходимо вставить пароль приложения для указанного адреса почты.
     ```
4. Теперь можно запустить приложение:
    * Для Linux систем:
      ```
      docker compose up
      ```
    * Для Windows систем:
      ```
      docker-compose up
      ```
5. Теперь можно протестировать приложение перейдя по следующему адресу:
      ```
      http://localhost:8080/swagger-ui/index.html
      ...
     Данные для входа в аккаунт администратора:
      Почта: admin@gmail.com
      Пароль: admin321@&123
      Данные для входа в аккаунт Grafana:
      Login: admin
      Пароль: admin
     ...
      ```    

## Правила работы с репозиторием


Cоздана ветка *develop*, ответвленная от *main*. Основная работа будет происходить в этой ветке, и все новые фичи, 
багфиксы и улучшения должны разрабатываться в отдельных *feature*-ветках, которые будут ответвляться от develop. Ветки 
создавайте по мере необходимости.

Для вливания одной ветки в другую используется Pull Request, в нем настроены тесты и проверка стиля, необходимо чтобы 
все успешно выполнялось, а так же необходимо подтверждение от любого другого участника команды, в таком случае ветку 
можно слить.

Критические багфиксы для продакшн-версии будем править через *hotfix*-ветки, которые ответвляются от main и вливаются 
обратно в *main* и *develop*.

Почитать про *git flow* [можно тут](https://habr.com/ru/articles/767424/ "habr.ru")

---

## Правила работы с проектом


#### Для запуска проекта нужно:

* Запустить docker (версия 4.34.2 и новее)
* Выбрать способ запуска:
    * Отладочный - БД в контейнере, приложение отдельно - запустить `elastic-compose.yml`
      и после запустить класс `Application`
    * Чистый - всё собирается в одном контейнере - запустить `compose.yml`
    * Полный - запускается все, что в чистом способе запуска, только вместе с системами метрик и мониторинга -
    в директории `spring-metrics` запустить `metrics-compose.yml`

Так же можно локально выполнить проверку код-stile, введя команду `mvn checkstyle:check`. Или же полную проверку, как 
в github, введя команду `mvn package`. Чтобы ввести команды, необходимо нажать дважды `ctrl`.

---

## Архитектура приложения

**База данных**
При проектировании БД был использован архитектурный стиль [EAV (Entity-Attribute-Value)](https://habr.com/ru/companies/tensor/articles/657895/)
![diagram_main](https://github.com/user-attachments/assets/852da4c2-3e8a-4058-bb80-2bf94d50bf77)

**Диаграмма состояний**
![{210F54AE-5C37-4702-BD82-40DC68A9C730}](https://github.com/user-attachments/assets/9613bf67-e641-446e-ab54-c4ac45af404f)



