Satellite Application
### Сборка

Требуется Java 17 + postgres + доступ к браузеру

### Перед началом использования приложения необходимо переопределить следующие переменные в файле application.properties:
| Переменная                    | Описание                                                                            | Пример                                                   |
|-------------------------------|-------------------------------------------------------------------------------------|----------------------------------------------------------|
| server.port                   | порт, на котором запускается приложение                                             | 8099                                                     |
| spring.datasource.url         | путь подключения к базе данных                                                      | jdbc:postgresql://localhost:5436/user?currentSchema=sat  |
| spring.datasource.username    | имя пользователя базы данных                                                        | user                                                     |
| spring.datasource.password    | пароль для подключения к базе данных                                                | user                                                     |
| spring.flyway.schemas         | имя схемы для миграции данных                                                       | sat                                                      |                               | /home/user/data_satellite                                |

### Описание пользовательского поведения
1. Открыть в браузере страничку по адресу http://localhost:8099/v2/api (порт вписывается согласно вашим настройкам); 
2. В блоке "Загрузка Constellation файла" по очереди загружаем все файлы созвезди спутников. Загрузка файла происходит путем выбора требуемого файла и нажатием кнопки "Загрузка";
3. В блоке "Загрузка Facility файла" по очереди загружаем все файлы наземный станций. Загрузка файла происходит путем выбора требуемого файла и нажатием кнопки "Загрузка";
4. Вычисление запускается нажатием клавиши "Вычислить". Данное действие длиться от 3 и более минуты (зависит от объема загруженных данных и мощности машины)

## ОЧЕНЬ ВАЖНОЕ ПРИМЕЧАНИЕ:
Имена загружаемых файлов должны быть уникальными, кроме того, название наземной станции в имени файла и в его содержании должны совпадать посимвольно (в предоставленных файлах обнаружена опечатка в названии файла Dehli и разное написание Cape-Town/Cape_Town, для корректной работы приложения эти различия должны быть устранены).

### Выгрузка отчетов:
В результате работы приложения будут выгружены файлы с расписанием на каждую наземную станцию, каждую орбитальную плоскость, а также отчет об изменениях в состоянии ЗУ спутника, его сеансах связи и съемки и служебный отчет.
* Файлы, сохраняющиеся в **alternative-format**, содержат хронологический список сеансов съемки территории РФ спутником и сеансов связи с наземными станциями для передачи данных. Также указывается изменение состояния его ЗУ при выполнении этих сеансов.
* Файлы, сохраняющиеся в **Russia2Constellation**, содержат хронологический список сеансов съемки спутниками в каждой орбитальной плоскости.
* Файлы, сохраняющиеся в **Facility2Constellation**, содержат хронологический список сеансов связи с каждым спутником, установившим контакт со станцией в течение расчетного периода.
* Файл **report** содержит название спутника, дату и время первого переполнения ЗУ спутника, общий объем данных за весь расчетный период.


## Алгоритм вычисления расписания спутников
1. На подготовительном этапе происходит:
 * вычитывание из БД всех данных сеансов спутник-наземная станция. Сеансы сгруппированы по наземной станции
 * вычитывание из БД всех данных сеансов спутник-съемка. Сеансы сгруппированы спутнику.
 * из сеансов съемки спутника оставляем только те, что начинаются в светлое время суток (от 9:00 до 18:00)
2. Для каждого спутника проходим по сеансам съемки, на каждой итерации цикла происходит следуещее:
 * проверяем, чтобы начало текущей сессии было позже, чем конец прошлой (данное пересечение возможно, если сеанс передачи данных закончился позже, чем должен был начаться сеанс текущей съемки)
 * производим вычисления изменения памяти
 * для переменной **previousEndSession** присваивем значение конца сессии съемки
 * находим интервал между концом следуещего и текущего сеанса съемки (алгоритм съемки описан ниже)
 * если подходящая интервал найден запускаем его (в дневное время суток возможно не более одной итерации передачи данных в перерыве между запланированными сеансами съемки)
 * увеличиаем переменную **memorySendingSum** на размер переданной информации
 * для переменной **previousEndSession** присваивем значение конца сессии передачи данных (обновили второй раз за одну итерацию)
3. Если во время сеанса съемки произошло обнуление памяти, то конец съемки сохраняем в переменную **memoryOverflowData** (используется для передачи в отчет)

## Алгоритм вычисления подходящего сеанса для выгрузки данных
1. Алгоритм принимает следующие поля
```
satellite Спутник, для которого ищем расписание.
startFreeInterval Начала интервала, в который спутник доступен для передачи данных.
endFreeInterval   Конец интервала, в который спутник доступен для передачи данных.
```
2. из данных, полученных на стадии подготовки, у каждого наземной станции ищем сеанс выгрузки с спутником **satellite**, который начинается между интервалом  **startFreeInterval** и **endFreeInterval**
3. из полученного списка возможных сеансов (для каждой наземной станции получим по 1 варианту) выбираем тот, что начнется раньше всех.
4. из расписания сеансов выбранной нами наземной станции удаляем те, что начинаются во время действия выбранного сеанса. То есть у наземной станции в один интервал времени есть возможнсть получить данные из разных спутников, но если какой-то сеанс уже выбран, то оставшиеся сеансы, которые могли бы начаться в промежуток действия выбранного - будут удалены.

#### На данном этапе алгоритм составления расписани очень неоптимальный, но для работы по оптимизации нет существенных ограничений, т.е. имеется возможность в внесении изменений

### Структура БД.

Стркутура базы данных накатывается при запуске приложения путем миграций flyway.

##### Описание таблиц

| Таблица                    | Описание                                                 |
|----------------------------|----------------------------------------------------------|
| facility                   | Наземная станция                                         |
| area                       | Создвездие/плеяды спутников                              |
| satellite_type             | Типы спутника                                            |
| satellite                  | Таблица спутников                                        |
| satellite_facility_session | Таблица сеансов передачи данных Наземная станция-Спутник |
| satellite_area_session     | Таблица сеансов съемок спутника                          |
| uploaded_files             | Таблица загруженных файлов                               |
