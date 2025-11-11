Процесс запуска:

- Сначала запускаете Tabbycat, просто через docker compose up с их офф гитхаба
- Далее авторизуетесь там и заходите на http://localhost:8000/accounts/password_change/ и там есть tabbycat API KEY, копируете ее
- далее в файлах бэка, а именно по пути: qdeb\src\main\resources\application-docker.properties
и
qdeb\src\main\resources\application.properties

в этих файлах есть tabbycat.api.key и ее меняете на то что получили с сайта

и всё
