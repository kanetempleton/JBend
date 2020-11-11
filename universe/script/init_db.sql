create database jbend;
show databases;
CREATE USER 'jbendroot'@'localhost' IDENTIFIED BY 'password';
CREATE USER 'jbendroot'@'%.kanetempleton.com' IDENTIFIED BY 'password;
GRANT ALL ON jbend.* TO 'jbendroot'@'localhost';


CREATE TABLE users(id INTEGER PRIMARY KEY AUTO_INCREMENT, username TEXT NOT NULL, password TEXT NOT NULL, email TEXT NOT NULL, privileges INTEGER NOT NULL);
INSERT INTO users(username,password,email,privileges) VALUES("admin","jbend","jbendmailer@gmail.com",3);

