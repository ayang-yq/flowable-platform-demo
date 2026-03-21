UPDATE users SET password = '$2a$10$9TesPoS4qSMo13oducMUuO4ie5SEOyvFsw40nBhdE8gTmoxvBGWE' WHERE username = 'admin';
SELECT username, password FROM users WHERE username = 'admin';
