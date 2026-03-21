UPDATE users SET password = '$2a$10$n7AyxDyB/0PIkeWoMMg16ezCYx5PsnEoCEpP3dIIrchD7DGjCMIUq' WHERE username = 'admin';
SELECT 'Password updated for admin user' as status;
SELECT username, substring(password, 1, 30) as password_hash FROM users WHERE username = 'admin';
