find src -type f -name "*.ts*" -print -exec cat {} \; > frontendCode;
find src -type f -name "*.styl" -print -exec cat {} \; > frontendStyle;
