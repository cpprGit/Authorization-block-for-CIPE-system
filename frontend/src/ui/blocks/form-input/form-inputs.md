## Возвращаемые значения

| Input                         | Не заполнен |      Заполнен      |            Что по валидаторам?             |
| ----------------------------- | :---------: | :----------------: | :----------------------------------------: |
| form-addable-input.tsx        |             |                    |                     -                      |
| form-stage-input.tsx          |     []      |      Stage[]       |                     -                      |
| form-checkbox-input.tsx       |     []      |     ["2 курс"]     |                     +                      |
| form-date-input.tsx           |    null     |    объект Даты     | + (на валидность даты инпут проверяет сам) |
| form-email-input.tsx          |     ""      |     "sdfvdfsv"     |                     +                      |
| form-file-input.tsx           |     ""      |   "C:/file.txt"    |                     +                      |
| form-long-text-input.tsx      |     ""      |       "text"       |                     +                      |
| form-mentor-suggest-input.tsx |  undefined  | {name: "t", id: 4} |                     +                      |
| form-number-input.tsx         |     ""      |        "5"         |                     +                      |
| form-password-input.tsx       |     ""      |   "QWErty12345"    |                     +                      |
| form-radio-input.tsx          |     ""      |        "2"         |                     +                      |
| form-select-input.tsx         |     ""      |     "БПМИ161"      |                     +                      |
| form-short-text-input.tsx     |     ""      |     "sdfvdfsv"     |                     +                      |
| form-switch-input.tsx         |    false    |      boolean       |                     -                      |
